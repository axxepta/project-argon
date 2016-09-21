package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Markus on 04.11.2015.
 */
public class ArgonTreeTransferHandler extends TransferHandler {
// ToDo: change all calls of JOptionPane.showMessageDialog to Oxygen dialogs
    private static final Logger logger = LogManager.getLogger(ArgonTreeTransferHandler.class);

    private final ArgonTree tree;

    public ArgonTreeTransferHandler(ArgonTree tree) {
        super();
        this.tree = tree;
    }

    // for expansion to drag handling via AspectJ
    @SuppressWarnings("all")
    public void setTreeDragEnabled(boolean isEnabled) {
        tree.setDragEnabled(isEnabled);
    }

    // for expansion to drag handling via AspectJ
    @SuppressWarnings("all")
    public TreePath[] getTreeSelection() {
        return tree.getSelectionPaths();
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

/*    Transferable createTransferable(JComponent c) {
        InputStream inputStream = (new ArgonProtocolHandler(BaseXSource.DATABASE)).ArgonConnection().getInputStream();
        return new StringSelection(c.getSelection());
    }*/

    public boolean canImport(TransferHandler.TransferSupport info) {
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)  || !info.isDrop()) {
            return false;
        }

        TreePath path = ((JTree.DropLocation) info.getDropLocation()).getPath();
        //DefaultTreeModel model = (DefaultTreeModel) ((JTree) info.getComponent()).getModel();
        return (!TreeUtils.isRoot(path) && !TreeUtils.isDbSource(path));
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            return false;

        JTree.DropLocation dropLocation = (JTree.DropLocation) info.getDropLocation();
        TreePath path = dropLocation.getPath();
        if (!((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren())
            path = path.getParentPath();

        if (((ArgonTreeNode) path.getLastPathComponent()).getTag() instanceof String) {
            String pathURLString = (String) ((ArgonTreeNode) path.getLastPathComponent()).getTag();
            Transferable transferable = info.getTransferable();
            try {
                List<File> transferList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                ArrayList<File> transferData = new ArrayList<>(transferList.size());
                transferData.addAll(transferList);
                if (transferData.size() > 0) {
                    transferFiles(transferData, path, pathURLString);
                }
            } catch (Exception e1) {
                logger.info(e1.getMessage());
                java.awt.Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, "Couldn't access transferred objects,\n see log file for details.",
                        "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private void transferFiles(ArrayList<File> transferData, TreePath path, String pathURLString) {
        BaseXSource source = TreeUtils.sourceFromTreePath(path);

        ArrayList<String> pathList = new ArrayList<>();
        // consider: transferred objects could be deep directories!
        for (File file : transferData) {
            pathList.add(pathURLString + "/" + file.getName());
        }
        WorkspaceUtils.OverwriteChecker overwriteChecker = new WorkspaceUtils.OverwriteChecker();
        int i = 0;
        List<String> lockedFiles = new ArrayList<>();
        boolean isLocked;
        while (i < transferData.size()) {
            File file = transferData.get(i);
            if (transferData.get(i).isFile()) {
                URL url;
                try {
                    url = new URL(pathList.get(i));
                    String newPath = CustomProtocolURLHandlerExtension.pathFromURL(url);

                    isLocked = ConnectionWrapper.isLocked(source, newPath);
                    if (isLocked) {
                        lockedFiles.add(url.toString());
                    } else {
                        if (overwriteChecker.newResourceOrOverwrite(source, newPath)) {
                            // ToDo: proper locking while storage process (transaction)
                            copyFile(file, url);
                        }
                    }

                } catch (MalformedURLException e1) {
                    logger.error(e1);
                }
            } else {    // expand subdirectories
                File[] dirFiles = file.listFiles();
                if ((dirFiles != null) && (dirFiles.length != 0)) {
                    for (File subFile : dirFiles) {
                        pathList.add(pathList.get(i) + "/" + subFile.getName());
                        transferData.add(subFile);
                    }
                }
            }
            i++;
        }
        if (lockedFiles.size() > 0) {
            JOptionPane.showMessageDialog(null, "The following target URLs are locked by another user and could not be overwritten:\n" + lockedFiles,
                    "Drag&Drop Message", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void copyFile(File file, URL url) throws MalformedURLException {
        byte[] isByte;
        try (InputStream is = new FileInputStream(file)) {
            int l = is.available();
            isByte = new byte[l];
            //noinspection ResultOfMethodCallIgnored
            is.read(isByte);
            try {
                WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
                // check for XML, check file content only if not enough info by file extension
                if (!URLUtils.isXML(url) && (URLUtils.isBinary(url) || !IOUtils.isXML(isByte))) {
                    ConnectionWrapper.save(true, url, isByte);
                } else {
                    String encoding = XMLUtils.encodingFromBytes(isByte);
                    if (!encoding.equals("UTF-8") && !encoding.equals(""))
                        isByte = IOUtils.convertToUTF8(isByte, encoding);
                    ConnectionWrapper.save(url, isByte, encoding);
                }
                logger.info("Dropped file " + file.toString() + " to " + url.toString());
                WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
            } catch (IOException ex) {
                WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                JOptionPane.showMessageDialog(null, "Couldn't store transferred object\n" + file.toString()
                        + "\nto database: " + ex.getMessage(), "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (IOException es) {
            logger.error(es);
            JOptionPane.showMessageDialog(null, "Couldn't read transferred object\n" + file.toString()
                    + ".", "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private byte[] readTextFile(URL url) throws IOException {
        List<Integer> integerList = new ArrayList<>();
        PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

        try (Reader urlReader = workspace.getUtilAccess().createReader(url, "UTF-8")) {
            while(urlReader.ready()) {
                integerList.add(urlReader.read());
            }
        }
        byte[] bytes = new byte[integerList.size()];
        Iterator<Integer> iterator = integerList.iterator();
        int index = 0;
        while(iterator.hasNext()) {
            Integer i = iterator.next();
            bytes[index] = i.byteValue();
            index++;
        }
        return bytes;
    }

}
