package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.actions.ExportAction;
import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.BaseXType;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * @author Markus on 27.10.2016.
 */
class ArgonTreeTransferable implements Transferable {

    private static final Logger logger = LogManager.getLogger(ArgonTreeTransferable.class);
    private static DataFlavor treePathFlavor = getTreePathFlavor();
    private static DataFlavor uriListFlavor = getURIListFlavor();
    private static DataFlavor[] flavors = initFlavors();
    private TreePath path;

    ArgonTreeTransferable(TreePath path) {
        this.path = path;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.equals(treePathFlavor) || flavor.equals(uriListFlavor));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(treePathFlavor)) {
            if ((path.getPathCount() < 3) || TreeUtils.isDB(path)) {
                throw new IOException("Drag error: Cannot copy/move databases or root branches.");
            }
            return path;
        }
        if (flavor.equals(uriListFlavor)) {
            if ((path.getPathCount() < 3) || TreeUtils.isDB(path)) {
                throw new IOException("Drag error: Cannot open databases or root branches.");
            }
            return getURIList();
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private static DataFlavor[] initFlavors() {
        DataFlavor[] flavors = new DataFlavor[2];
        flavors[0] = getTreePathDataFlavor();
        flavors[1] = getURIListFlavor();
        return flavors;
    }

    private static DataFlavor getTreePathFlavor() {
        String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" +
                TreePath.class.getName() + "\"";
        try {
        return new DataFlavor(mimeType);
        } catch (ClassNotFoundException cn) {
            logger.debug("Class not found creating DataFlavor");
            return new DataFlavor();
        }
    }

    private static DataFlavor getURIListFlavor() {
        return new DataFlavor("text/uri-list;class=java.lang.String", null);
    }

    static DataFlavor getTreePathDataFlavor() {
        return treePathFlavor;
    }

    private String getURIList() throws IOException {
        BaseXSource source = TreeUtils.sourceFromTreePath(path);
        List<BaseXResource> resourceList = ExportAction.getExportResourceList(source, path, TreeUtils.resourceFromTreePath(path));
        StringBuilder uriListBuilder = new StringBuilder("");
        for (BaseXResource resource : resourceList) {
            if (resource.getType().equals(BaseXType.RESOURCE)) {
                String fullResourceName = ExportAction.getFullResource(path, source, resource);
                String resourceURL = CustomProtocolURLHandlerExtension.protocolFromSource(source) + ":" +
                        fullResourceName;
                uriListBuilder.append(resourceURL).append("\n");
            }
        }
        return uriListBuilder.toString();
    }

}
