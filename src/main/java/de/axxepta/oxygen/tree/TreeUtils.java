package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLUtils;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.tree.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for tree related operations
 */
public class TreeUtils {

    private static final Logger logger = LogManager.getLogger(TreeUtils.class);

    static final int DEPTH_ROOT = 1;
    static final int DEPTH_SOURCE = 2;
    static final int DEPTH_DB = 3;

    private static TreeModel model;

    public static void init(TreeModel treeModel) {
        model = treeModel;
    }

    public static void insertStrAsNodeLexi(TreeModel treeModel, String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
        final DefaultMutableTreeNode childNode = newChild(child, parent, childIsFile);
        boolean inserted = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            final DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) parent.getChildAt(i);
            final Boolean nextIsFile = !currNode.getAllowsChildren();
            if (((currNode.getUserObject().toString().compareTo(child) > 0) && (nextIsFile.compareTo(childIsFile) == 0)) ||
                    (nextIsFile.compareTo(childIsFile) > 0)) {    // dirs before files
                ((DefaultTreeModel) treeModel).insertNodeInto(childNode, parent, i);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            ((DefaultTreeModel) treeModel).insertNodeInto(childNode, parent, parent.getChildCount());
        }
    }

    public static DefaultMutableTreeNode insertStrAsNodeLexi(String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
        final DefaultMutableTreeNode childNode = newChild(child, parent, childIsFile);
        boolean inserted = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            final DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) parent.getChildAt(i);
            final AtomicReference<Boolean> nextIsFile = new AtomicReference<>();
            nextIsFile.set(!currNode.getAllowsChildren());
            if (((currNode.getUserObject().toString().compareTo(child) > 0) && (nextIsFile.get().compareTo(childIsFile) == 0)) ||
                    (nextIsFile.get().compareTo(childIsFile) > 0)) {    // dirs before files
                parent.insert(childNode, i);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            parent.insert(childNode, parent.getChildCount());
        }
        return childNode;
    }

    private static DefaultMutableTreeNode newChild(String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
        final String delim = (parent.getLevel() < DEPTH_SOURCE) ? "" : "/";
        final DefaultMutableTreeNode childNode = ClassFactory.getInstance().getTreeNode(child,
                ((ArgonTreeNode) parent).getTag().toString() + delim + child);
        if (childIsFile) {
            childNode.setAllowsChildren(false);
        } else {
            childNode.setAllowsChildren(true);
        }
        return childNode;
    }

    public static int isNodeAsStrChild(TreeNode parent, String child) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject().toString().equals(child)) {
                return i;
            }
        }
        return -DEPTH_ROOT;
    }

    public static TreePath pathByAddingChildAsStr(TreePath currPath, String child) {
        // returns TreePath to child given by String, if child doesn't exist returns null!
        final DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
        int childNodeIndex = isNodeAsStrChild(currNode, child);
        if (childNodeIndex != -DEPTH_ROOT) {
            return new TreePath(((DefaultMutableTreeNode) currNode.getChildAt(childNodeIndex)).getPath());
        }
        return null;
    }

    public static TreePath pathFromURLString(String urlString) {
        TreePath path = new TreePath(model.getRoot());
        final BaseXSource source = CustomProtocolURLUtils.sourceFromURLString(urlString);
        switch (source) {
//            case REPO:
//                path = pathByAddingChildAsStr(path, Lang.get(Lang.Keys.tree_repo));
//                break;
//            case RESTXQ:
//                path = pathByAddingChildAsStr(path, Lang.get(Lang.Keys.tree_restxq));
//                break;
            default:
                path = pathByAddingChildAsStr(path, Lang.get(Lang.Keys.tree_DB));
        }
        final String[] protocolResource = urlString.split(":/*");
        if (protocolResource.length > DEPTH_ROOT) {
            String[] pathParts = protocolResource[DEPTH_ROOT].split("/");
            for (String res : pathParts) {
                path = pathByAddingChildAsStr(path, res);
            }
        }
        return path;
    }

    public static BaseXSource sourceFromTreePath(TreePath path) {
        if (path.getPathCount() > DEPTH_ROOT) {
            final String sourceStr = path.getPathComponent(DEPTH_ROOT).toString();
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_DB))) {
                return BaseXSource.DATABASE;
            }
           if (sourceStr.equals(Lang.get(Lang.Keys.tree_restxq)))
               return BaseXSource.RESTXQ;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_repo))) {
                return BaseXSource.REPO;
            }
            return null;
        } else {
            return null;
        }
    }

    public static String protocolFromTreePath(TreePath path) {
        if (path.getPathCount() > DEPTH_ROOT) {
            final String sourceStr = path.getPathComponent(DEPTH_ROOT).toString();
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_DB))) {
                return ArgonConst.ARGON;
            }
//            if (sourceStr.equals(Lang.get(Lang.Keys.tree_restxq)))
//                return ArgonConst.ARGON_XQ;
            if (sourceStr.equals(Lang.get(Lang.Keys.tree_repo))) {
                return ArgonConst.ARGON_REPO;
            }
            return null;
        } else {
            return null;
        }
    }

    public static String resourceFromTreePath(TreePath path) {
        final StringBuilder resource = new StringBuilder();
        if (path.getPathCount() > DEPTH_ROOT) {
            for (int i = DEPTH_SOURCE; i < path.getPathCount(); i++) {
                if (i > DEPTH_SOURCE) {
                    resource.append('/');
                }
                resource.append(path.getPathComponent(i).toString());
            }
        }
        return resource.toString();
    }

    public static String urlStringFromTreePath(TreePath path) {
        final StringBuilder db_path;
//        if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq)))
//            db_path = new StringBuilder(ArgonConst.ARGON_XQ + ":");
//        else
//        if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_repo)))
//            db_path = new StringBuilder(ArgonConst.ARGON_REPO + ":");
//        else
            db_path = new StringBuilder(ArgonConst.ARGON + ":");
        for (int i = DEPTH_SOURCE; i < path.getPathCount(); i++) {
            if (i > DEPTH_SOURCE) {
                db_path.append('/');
            }
            db_path.append(path.getPathComponent(i).toString());
        }
        return db_path.toString();
    }

    public static String treeStringFromTreePath(TreePath path) {
        final StringBuilder db_path = new StringBuilder(Lang.get(Lang.Keys.tree_root));
        for (int i = DEPTH_ROOT; i < path.getPathCount(); i++) {
            db_path.append("/").append(path.getPathComponent(i).toString());
        }
        return db_path.toString();
    }

    public static String urlStringFromTreeString(String treeString) {
        final String[] components = treeString.split("/");
        final StringBuilder db_path;

        if (components.length > DEPTH_SOURCE) {
//            if (components[1].equals(Lang.get(Lang.Keys.tree_restxq)))
//                db_path = new StringBuilder(ArgonConst.ARGON_XQ + ":");
//            else
//            if (components[1].equals(Lang.get(Lang.Keys.tree_repo)))
//                db_path = new StringBuilder(ArgonConst.ARGON_REPO + ":");
//            else
                db_path = new StringBuilder(ArgonConst.ARGON + ":");
            db_path.append(treeString.substring(components[0].length() + components[DEPTH_ROOT].length() + DEPTH_SOURCE));
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
            return nodes[nodes.length - DEPTH_ROOT];
        }
    }

    public static TreePath pathToDepth(TreePath path, int depth) {
        TreePath returnPath = path;
        if (path.getPathCount() < depth) {
            return new TreePath(new Object[0]);
        } else {
            for (int i = path.getPathCount(); i > (depth + DEPTH_ROOT); i--) {
                returnPath = returnPath.getParentPath();
            }
        }
        return returnPath;
    }

    public static boolean isFile(TreePath path) {
//        logger.info("isFile " + path);
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (!node.getAllowsChildren());
    }

    public static boolean isDir(TreePath path) {
//        logger.info("isDir " + path);
        final DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        final int pathCount = path.getPathCount();
        return (clickedNode.getAllowsChildren() &&
                (((pathCount > DEPTH_DB) &&
                        (path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)))) ||
                        ((pathCount > DEPTH_SOURCE) &&
                                (!path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB))))));
    }

    public static boolean isDB(TreePath path) {
//        logger.info("isDB " + path);
        final int pathCount = path.getPathCount();
        return (pathCount == DEPTH_DB) &&
                (path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isInDB(TreePath path) {
//        logger.info("isInDB " + path);
        final int pathCount = path.getPathCount();
        return (pathCount > DEPTH_DB) &&
                (path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

//    public static boolean isInRestXQ(TreePath path) {
//        int pathCount = path.getPathCount();
//        return (pathCount > 2) &&
//                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq)));
//    }

//    public static boolean isInRepo(TreePath path) {
//        int pathCount = path.getPathCount();
//        return (pathCount > 2) &&
//                (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_repo)));
//    }

    public static boolean isSource(TreePath path) {
//        logger.info("isSource " + path);
        final int pathCount = path.getPathCount();
        return (pathCount == DEPTH_SOURCE);
    }

    public static boolean isRoot(TreePath path) {
//        logger.info("isRoot " + path);
        final int pathCount = path.getPathCount();
        return (pathCount == DEPTH_ROOT);
    }

    public static boolean isDbSource(TreePath path) {
//        logger.info("isDBSource " + path);
        final int pathCount = path.getPathCount();
        return ((pathCount == DEPTH_SOURCE) && path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isFileSource(TreePath path) {
//        logger.info("isFileSource " + path);
        return (TreeUtils.isSource(path) && !path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)));
    }

    public static boolean isWEBINF(TreePath path) {
        final DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        final int pathCount = path.getPathCount();
        return TreeUtils.isDir(path)
                && clickedNode.getUserObject().toString().equals("WEB-INF")
                && (
//                    ((pathCount == DEPTH_DB) && path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq))) ||
                ((pathCount == 5) && path.getPathComponent(DEPTH_ROOT).toString().equals(Lang.get(Lang.Keys.tree_DB)))
                );
    }

}
