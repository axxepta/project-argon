package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.Lang;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Utility class for tree related operations
 */
public class TreeUtils {

    public static void insertStrAsNodeLexi(DefaultTreeModel treeModel, String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
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

    public static boolean isNodeAsStrChild(DefaultMutableTreeNode parent, String child) {
        for (int i=0; i<parent.getChildCount(); i++) {
            if (((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject().toString().equals(child)) {
                return true;
            }
        }
        return false;
    }

    public static TreePath pathByAddingChildAsStr(TreePath currPath, String child) {
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
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_DB)))
                return BaseXSource.DATABASE;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_restxq)))
                return BaseXSource.RESTXQ;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_repo)))
                return BaseXSource.REPO;
            return null;
        } else {
            return null;
        }
    }

    public static String protocolFromTreePath(TreePath path) {
        if (path.getPathCount() > 1) {
            String sourceStr = path.getPathComponent(1).toString();
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_DB)))
                return CustomProtocolURLHandlerExtension.ARGON;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_restxq)))
                return CustomProtocolURLHandlerExtension.ARGON_XQ;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_repo)))
                return CustomProtocolURLHandlerExtension.ARGON_REPO;
            return null;
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
        StringBuilder db_path;
        if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq)))
            db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_XQ + ":");
        else if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_repo)))
            db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_REPO + ":");
        else
            db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON + ":");
        for (int i = 2; i < path.getPathCount(); i++) {
            db_path.append('/');
            db_path.append(path.getPathComponent(i).toString());
        }
        return db_path.toString();
    }

    public static String treeStringFromTreePath(TreePath path) {
        StringBuilder db_path = new StringBuilder(Lang.get(Lang.Keys.tree_root));
        for (int i = 1; i < path.getPathCount(); i++) {
            db_path.append("/").append(path.getPathComponent(i).toString());
        }
        return db_path.toString();
    }

    public static String urlStringFromTreeString(String treeString) {
        String[] components = treeString.split("/");
        StringBuilder db_path;

        if (components.length > 2) {
            if (components[1].equals(Lang.get(Lang.Keys.tree_restxq)))
                db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_XQ + ":");
            else if (components[1].equals(Lang.get(Lang.Keys.tree_repo)))
                db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON_REPO + ":");
            else
                db_path = new StringBuilder(CustomProtocolURLHandlerExtension.ARGON + ":");
            db_path.append(treeString.substring(components[0].length()+components[1].length()+1));
        } else {
            db_path = new StringBuilder("");
        }
        return db_path.toString();
    }

    public static String fileStringFromPathString(String path) {
        if (path.equals("") || path.endsWith("/") || path.endsWith("\\")) {
            return "";
        } else {
            String[] nodes = path.split("\\\\|/");
            return nodes[nodes.length-1];
        }
    }

    public static TreePath pathToDepth(TreePath path, int depth) {
        TreePath returnPath = path;
        if (path.getPathCount() < depth)
            return new TreePath(new Object[0]);
        else {
            for (int i=path.getPathCount(); i>(depth+1); i--) {
                returnPath = returnPath.getParentPath();
            }
        }
        return returnPath;
    }

    public static boolean isFile(TreePath path) {
        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (!clickedNode.getAllowsChildren());
    }

    public static boolean isDir(TreePath path) {
        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        int pathCount = path.getPathCount();
        return (clickedNode.getAllowsChildren() &&
                ( ((pathCount > 3) &&
                        (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)))) ||
                        ((pathCount > 2) &&
                                (!path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)))) ) );
    }

    public static boolean isDB(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount == 3) &&
                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isInDB(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount > 3) &&
                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isInRestXY(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount > 2) &&
                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq)));
    }

    public static boolean isInRepo(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount > 2) &&
                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_repo)));
    }

    public static boolean isSource(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount == 2);
    }

    public static boolean isRoot(TreePath path) {
        int pathCount = path.getPathCount();
        return (pathCount == 1);
    }

    public static boolean isDbSource(TreePath path) {
        int pathCount = path.getPathCount();
        return ((pathCount == 2) && path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isFileSource(TreePath path) {
        return (TreeUtils.isSource(path) && !path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isWEBINF(TreePath path) {
        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        int pathCount = path.getPathCount();
        return (TreeUtils.isDir(path) && clickedNode.getUserObject().toString().equals("WEB-INF") &&
                ( (pathCount == 3) && (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq)))
                    || ((pathCount == 5) && (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB))))));
    }

}
