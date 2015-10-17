package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Created by Markus on 11.10.2015.
 */
public class TreeUtils {

    protected static void insertStrAsNodeLexi(DefaultTreeModel treeModel, String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
        DefaultMutableTreeNode currNode;
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
        if (childIsFile) childNode.setAllowsChildren(false);
        else childNode.setAllowsChildren(true);
        Boolean parentIsFile;
        boolean inserted = false;
        for (int i=0; i<parent.getChildCount(); i++) {
            currNode = (DefaultMutableTreeNode) parent.getChildAt(i);
            parentIsFile = !currNode.getAllowsChildren();
            if ((currNode.getUserObject().toString().compareTo(child) > 0) &&
                    (parentIsFile.compareTo(childIsFile) >= 0)) {    // dirs before files
                treeModel.insertNodeInto(childNode, parent, i);
                inserted = true;
                break;
            }
        }
        if (!inserted) treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
    }

    protected static boolean isNodeAsStrChild(DefaultMutableTreeNode parent, String child) {
        for (int i=0; i<parent.getChildCount(); i++) {
            if (((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject().toString().equals(child)) {
                return true;
            }
        }
        return false;
    }

    protected static TreePath pathByAddingChildAsStr(TreePath currPath, String child) {
        // returns TreePath to child given by String, if child doesn't exist returns null!
        DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)currPath.getLastPathComponent();
        for (int i=0; i<currNode.getChildCount(); i++) {
            if (((DefaultMutableTreeNode)currNode.getChildAt(i)).getUserObject().toString().equals(child)) {
                return new TreePath(((DefaultMutableTreeNode) currNode.getChildAt(i)).getPath());
            }
        }
        return null;
    }

    public static BaseXSource sourceFromTreePath(TreePath path) {
        if (path.getPathCount() > 1) {
            String sourceStr = path.getPathComponent(1).toString();
            // ToDo: use extra class for constant strings
            switch (sourceStr) {
                case "Databases": return BaseXSource.DATABASE;
                case "Query Folder": return BaseXSource.RESTXQ;
                case "Repo Folder": return BaseXSource.REPO;
                default: return null;
            }
        } else {
            return null;
        }
    }

    public static String resourceFromTreePath(TreePath path) {
        StringBuilder resource = new StringBuilder("");
        if (path.getPathCount() > 1) {
            for (int i = 2; i < path.getPathCount(); i++) {
                if (i>2) {
                    resource.append('/');
                }
                resource.append(path.getPathComponent(i).toString());
            }
        }
        return resource.toString();
    }

    public static String urlStringFromTreePath(TreePath path) {
        String db_path;
        // ToDo: use extra class for constant strings
        switch (path.getPathComponent(1).toString()) {
            case "Query Folder": db_path = CustomProtocolURLHandlerExtension.ARGON_XQ + ":";
                    break;
            case "Repo Folder": db_path = CustomProtocolURLHandlerExtension.ARGON_REPO + ":";
                    break;
            default: db_path = CustomProtocolURLHandlerExtension.ARGON + ":";
        }
        for (int i = 2; i < path.getPathCount(); i++) {
            db_path = db_path + '/' + path.getPathComponent(i).toString();
        }
        return db_path;
    }

    public static String treeStringFromTreePath(TreePath path) {
        StringBuilder db_path = new StringBuilder("BaseX Server");
        for (int i = 1; i < path.getPathCount(); i++) {
            db_path.append("/").append(path.getPathComponent(i).toString());
        }
        return db_path.toString();
    }

    public static String urlStringFromTreeString(String treeString) {
        String[] components = treeString.split("/");
        StringBuilder db_path;

        if (components.length > 2) {
            // ToDo: use extra class for constant strings
            switch (components[1]) {
                case "Query Folder":
                    db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_XQ + ":");
                    break;
                case "Repo Folder":
                    db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_REPO + ":");
                    break;
                default:
                    db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON + ":");
            }
            db_path.append(treeString.substring(components[0].length()+components[1].length()+1));
/*            for (int i = 2; i < components.length; i++) {
                if (i>2) {
                    db_path.append('/');
                }
                db_path.append(components[i]);
            }*/
        } else {
            db_path = new StringBuilder("");
        }
        return db_path.toString();
    }

}
