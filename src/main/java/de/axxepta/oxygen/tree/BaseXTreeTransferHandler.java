package de.axxepta.oxygen.tree;



import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 04.11.2015.
 */
public class BaseXTreeTransferHandler extends TransferHandler {

    private static final Logger logger = LogManager.getLogger(BaseXTreeTransferHandler.class);

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

        Transferable transferable = info.getTransferable();
        try {
            List<File> transferList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            ArrayList<File> transferData = new ArrayList<>(transferList.size());
            transferData.addAll(transferList);
            if (transferData.size() > 0) {
                BaseXSource source = TreeUtils.sourceFromTreePath(path);

                ArrayList<String> pathList = new ArrayList<>();
                // consider: transferred objects could be deep directories!
                for (File file : transferData) {
                    pathList.add(TreeUtils.urlStringFromTreePath(path) + "/" + file.getName());
                }
                int i = 0;
                while (i < transferData.size()) {
                    File file = transferData.get(i);
                    if (transferData.get(i).isFile()){
                        URL url = null;
                        try {
                            url = new URL(pathList.get(i));
                        } catch (MalformedURLException e1) {
                            logger.error(e1);
                        }
                        //copy file
                        byte[] isByte;
                        try (InputStream is = new FileInputStream(file)) {
                            int l = is.available();
                            isByte = new byte[l];
                            //noinspection ResultOfMethodCallIgnored
                            is.read(isByte);
                            try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url, true)) {
                                os.write(isByte);
                                logger.info("Dropped file " + file.toString() + " to " + pathList.get(i));
                            } catch (IOException ex) {
                                logger.error(ex.getMessage());
                                JOptionPane.showMessageDialog(null, "Couldn't store transferred object\n" + file.toString()
                                        + "\nto database.", "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
                            }
                        } catch (IOException es) {
                            logger.error(es);
                            JOptionPane.showMessageDialog(null, "Couldn't read transferred object\n" + file.toString()
                                    + ".", "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
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
            }
        } catch (Exception e1) {
            logger.info(e1.getMessage());
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Couldn't access transfered objects,\n see log file for details.",
                    "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
            return false;
        }

        return true;
    }

}
