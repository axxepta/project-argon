package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.actions.ExportAction;
import de.axxepta.oxygen.actions.RenameAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Markus on 04.11.2015.
 */
public class ArgonTreeTransferHandler extends TransferHandler {

    private static final Logger logger = LogManager.getLogger(ArgonTreeTransferHandler.class);
    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    private final ArgonTree tree;
    private final TreeModel model;

    private DataFlavor treePathFlavor;

    public ArgonTreeTransferHandler(ArgonTree tree) {
        super();
        this.tree = tree;
        model = tree.getModel();
        treePathFlavor = ArgonTreeTransferable.getTreePathDataFlavor();
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
        return COPY_OR_MOVE;
    }

    public Transferable createTransferable(JComponent c) {
/*        DefaultTreeModel model = (DefaultTreeModel) ((JTree) c).getModel();
        TreeListener listener = (TreeListener) model.getListeners(TreeModelListener.class)[0];
        return new ArgonTreeTransferable(listener.getPath()); */
        return new ArgonTreeTransferable(getTreeSelection()[0]);
    }

    public boolean canImport(TransferHandler.TransferSupport info) {
        if (!(info.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                info.isDataFlavorSupported(treePathFlavor)) || !info.isDrop()) {
            return false;
        }
        info.setShowDropLocation(true);
        TreePath path = ((JTree.DropLocation) info.getDropLocation()).getPath();
        //DefaultTreeModel model = (DefaultTreeModel) ((JTree) info.getComponent()).getModel();
        if (!TreeUtils.isRoot(path) && !TreeUtils.isDbSource(path)) {
            if (info.getDropAction() == COPY)
                return true;
            if (info.isDataFlavorSupported(treePathFlavor)) {
                try {
                    TreePath importPath = (TreePath) info.getTransferable().getTransferData(treePathFlavor);
                    return canMoveInTree(path.getPath(), importPath.getPath());
                } catch (IOException | UnsupportedFlavorException ex) {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    // drop from move only if in same source and same database (if source = DATABASE)
    private static boolean canMoveInTree(Object[] path, Object[] importPath) {
        if (importPath[1].equals(path[1])) {
            if (importPath[1].toString().equals(Lang.get(Lang.Keys.tree_DB))) {
                return importPath[2].equals(path[2]);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!(info.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                info.isDataFlavorSupported(treePathFlavor)) || !info.isDrop()) {
            return false;
        }

        JTree.DropLocation dropLocation = (JTree.DropLocation) info.getDropLocation();
        TreePath targetPath = dropLocation.getPath();
        if (!((DefaultMutableTreeNode) targetPath.getLastPathComponent()).getAllowsChildren())
            targetPath = targetPath.getParentPath();

        if (((ArgonTreeNode) targetPath.getLastPathComponent()).getTag() instanceof String) {
            String pathURLString = (String) ((ArgonTreeNode) targetPath.getLastPathComponent()).getTag();
            Transferable transferable = info.getTransferable();

            if (info.isDataFlavorSupported(treePathFlavor)) {
                try {
                    TreePath sourcePath = (TreePath) transferable.getTransferData(treePathFlavor);
                    BaseXSource source = TreeUtils.sourceFromTreePath(sourcePath);
                    String db_path = TreeUtils.resourceFromTreePath(sourcePath);

                    if (info.getDropAction() == COPY) {
                        copyInTree(source, db_path, sourcePath, pathURLString);
                    } else {
                        String newName = sourcePath.getLastPathComponent().toString();
                        String newPathString = CustomProtocolURLHandlerExtension.pathFromURLString(pathURLString) + "/" + newName;
                        RenameAction.rename(model, sourcePath, source, db_path, newPathString, newName, workspace);
                    }
                } catch (Exception ex) {
                    logger.debug("Error moving resources in tree: ", ex.getMessage());
                }

            } else {
                try {
                    List<File> transferList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    ArrayList<File> transferData = new ArrayList<>(transferList.size());
                    transferData.addAll(transferList);
                    if (transferData.size() > 0) {
                        transferFiles(transferData, targetPath, pathURLString);
                    }
                } catch (Exception e1) {
                    logger.info(e1.getMessage());
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    workspace.showInformationMessage("Couldn't access transferred objects,\n see log file for details.");
                    return false;
                }
            }
        }

        return true;
    }

    private static void copyInTree(BaseXSource source, String db_path, TreePath sourcePath, String pathURLString) throws Exception {
        List<BaseXResource> resourceList = ExportAction.getExportResourceList(source, sourcePath, db_path);
        for (BaseXResource resource : resourceList){
            if (resource.getType().equals(BaseXType.RESOURCE)) {
                String fullResourceName = ExportAction.getFullResource(sourcePath, source, resource);
                String resourceURL = CustomProtocolURLHandlerExtension.protocolFromSource(source) + ":" +
                        fullResourceName;
                String relativePath = ExportAction.getRelativePath(source, db_path, fullResourceName);
                URL sourceURL = new URL(resourceURL);
                String newURLString = pathURLString + "/" + relativePath;
                URL targetURL = new URL(newURLString);
                copyURLsInTree(sourceURL, targetURL);
            }
        }
    }

    private static void copyURLsInTree(URL sourceURL, URL targetURL) throws IOException {
        byte[] buffer = new byte[16];
        int len;
        try (InputStream is = ConnectionWrapper.getInputStream(sourceURL)) {
            len = is.read(buffer);
            if (len != -1) {
                // ToDo: check content type of input resource
                if (buffer[0] == (byte)0x3c) {
                    try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(targetURL, "UTF-8")) {
                        os.write(buffer, 0, len);
                        streamCopy(is, os);
                    }
                } else {
                    try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(true, targetURL)) {
                        os.write(buffer, 0, len);
                        streamCopy(is, os);
                    }
                }
            }
        }
    }

    private static void streamCopy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
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
                            ConnectionWrapper.lock(source, newPath);
                            copyFile(file, url);
                            ConnectionWrapper.unlock(source, newPath);
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
            workspace.showInformationMessage("The following target URLs are locked by another user and could not be overwritten:\n" + lockedFiles);
        }
    }

    private void copyFile(File file, URL url) throws MalformedURLException {
        byte[] isByte;
        try (InputStream is = new FileInputStream(file)) {
            int l = is.available();
            isByte = new byte[l];
            //noinspection ResultOfMethodCallIgnored
            is.read(isByte);
            String owner = Files.getOwner(file.toPath()).getName().replace("\\", "_");
            try {
                WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
                // check for XML, check file content only if not enough info by file extension
                if (!URLUtils.isXML(url) && (URLUtils.isBinary(url) || !IOUtils.isXML(isByte))) {
                    ConnectionWrapper.save(owner, true, url, isByte);
                } else {
                    String encoding = XMLUtils.encodingFromBytes(isByte);
                    if (!encoding.equals("UTF-8") && !encoding.equals(""))
                        isByte = IOUtils.convertToUTF8(isByte, encoding);
                    ConnectionWrapper.save(owner, url, isByte, encoding);
                }
                logger.info("Dropped file " + file.toString() + " to " + url.toString());
                WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
            } catch (IOException ex) {
                WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                workspace.showErrorMessage("Couldn't store transferred object\n" + file.toString() + "\nto database: " + ex.getMessage());
            }
        } catch (IOException es) {
            logger.error(es);
            workspace. showErrorMessage("Couldn't read transferred object\n" + file.toString() + ".");
        }
    }

    private byte[] readTextFile(URL url) throws IOException {
        List<Integer> integerList = new ArrayList<>();

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
