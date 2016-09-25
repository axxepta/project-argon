package de.axxepta.oxygen.tree;

import java.awt.event.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

import de.axxepta.oxygen.actions.AddDatabaseAction;
import de.axxepta.oxygen.actions.AddNewFileAction;
import de.axxepta.oxygen.actions.DeleteAction;
import de.axxepta.oxygen.actions.RefreshTreeAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Listener class observing all tree-related events
 */
public class TreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener,
        KeyListener, ObserverInterface{

    private static final Logger logger = LogManager.getLogger(TreeListener.class);
    
    private ArgonTree tree;
    private TreeModel treeModel;
    private TreePath path;
    private TreeNode node;
    private boolean newExpandEvent;
    private boolean singleClick = true;
    private boolean doubleClickExpandEnabled = true;
    private Timer timer;
    private final ArgonPopupMenu contextMenu;

    public TreeListener(ArgonTree tree, TreeModel treeModel, ArgonPopupMenu contextMenu)
    {
        this.tree = tree;
        this.treeModel = treeModel;
        this.newExpandEvent = true;
        this.contextMenu = contextMenu;
        ActionListener actionListener = e -> {
                timer.stop();
                if (singleClick) {
                    singleClickHandler(e);
                } else {
                    try {
                        doubleClickHandler(e);
                    } catch (ParseException ex) {
                        logger.error(ex);
                    }
                }
        };
        int doubleClickDelay = 300;
        timer = new javax.swing.Timer(doubleClickDelay, actionListener);
        timer.setRepeats(false);
    }


    /*
     * methods of MouseAdapter
     */

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            singleClick = true;
            timer.start();
        } else {
            singleClick = false;
        }
    }

    @Override public void mouseReleased( MouseEvent e ) {
        path = tree.getPathForLocation(e.getX(), e.getY());
        try {
            if (path != null)
                node = (TreeNode) path.getLastPathComponent();
        } catch (NullPointerException er) {er.printStackTrace();}
        if ( e.isPopupTrigger() )
            contextMenu.show(e.getComponent(), e.getX(), e.getY(), path);
    }


    /*
     * methods of interface TreeSelectionListener
     */

    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        path = e.getNewLeadSelectionPath();
        node = (TreeNode) tree.getLastSelectedPathComponent();
    }


    /*
     * methods of interface TreeWillExpandListener
     */

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        // method is called twice, if new data is loaded--prevent database check in 2nd call
        boolean newTreeExpandEvent = this.newExpandEvent;

        path = event.getPath();
        node = (TreeNode) path.getLastPathComponent();
        int depth = path.getPathCount();

        if ((depth > 1) && (node.getAllowsChildren()) && this.newExpandEvent) {

            List<BaseXResource> childList;
            List<String> newValues = new ArrayList<>();
            BaseXSource source;
            String db_path;

            source = TreeUtils.sourceFromTreePath(path);

            if (depth > 2)      // get path in source
                db_path = (( (String) ((ArgonTreeNode) node).getTag() ).split("//"))[1] + "/";
            else
                db_path = "";

            try {
                childList = ConnectionWrapper.list(source, db_path);
            } catch (Exception er) {
                childList = new ArrayList<>();
                logger.debug(er);
                String error = er.getMessage();
                if ((error == null) || error.equals(""))
                    error = "Database connection could not be established.";
                JOptionPane.showMessageDialog(null, "Failed to get resource list from BaseX:\n" + error,
                        "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
            }
            for (BaseXResource child : childList) {
                newValues.add(child.getName());
            }
            if (updateExpandedNode(node, childList, newValues)) {
                this.newExpandEvent = false;
                tree.expandPath(path);
            }

        }
        if (!newTreeExpandEvent) this.newExpandEvent = true;
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}


    /*
     * methods for MouseAdapter
     */

    private void singleClickHandler(ActionEvent e) { logger.debug("-- single click --"); }

    private void doubleClickHandler(ActionEvent e) throws ParseException {
        logger.debug("-- double click --");
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                if (((TreeNode) path.getLastPathComponent()).getAllowsChildren()) {
                    if (doubleClickExpandEnabled) {
                        try {
                            treeWillExpand(new TreeExpansionEvent(this, path));
                        } catch (ExpandVetoException eve) {
                            logger.debug("Expand Veto: ", eve.getMessage());
                        }
                    }
                } else {
                    doubleClickAction(path);
                }
            }
        }
    }

    // made public for access with AspectJ
    @SuppressWarnings("all")
    public static void doubleClickAction(TreePath path) {
        //String db_path = ((ArgonTreeNode) path.getLastPathComponent()).getTag();
        String dbPath = TreeUtils.urlStringFromTreePath(path);
        logger.info("DbPath: " + dbPath);
        WorkspaceUtils.openURLString(dbPath);
    }


    /*
     * method for interface Observer
     */

    @Override
    public void update(String type, Object... message) {
        // is notified as observer when changes have been made to the database file structure
        // updates the tree if necessary
        TreeNode currNode;
        TreePath currPath;

        logger.info("Tree needs to update: " + message[0]);

        if (type.equals("SAVE_FILE") || type.equals("NEW_DIR")) {
            String[] protocol = ((String) message[0]).split(":/*");
            String[] path = protocol[1].split("/");
            currPath = new TreePath(treeModel.getRoot());
            switch (protocol[0]) {
                case ArgonConst.ARGON_REPO:
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, Lang.get(Lang.Keys.tree_repo));
                    break;
                case ArgonConst.ARGON_XQ:
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, Lang.get(Lang.Keys.tree_restxq));
                    break;
                default: currPath = TreeUtils.pathByAddingChildAsStr(currPath, Lang.get(Lang.Keys.tree_DB));
            }
            currNode = (TreeNode) currPath.getLastPathComponent();
            boolean expanded = false;
            Boolean isFile;
            for (int i = 0; i < path.length; i++) {
                if (tree.isExpanded(currPath)) expanded = true;
                if (expanded || (i == path.length - 1)) { // update tree now only if file is in visible path
                    if (TreeUtils.isNodeAsStrChild(currNode, path[i]) == -1) {
                        isFile = (i + 1 == path.length) && type.equals("SAVE_FILE");
                        TreeUtils.insertStrAsNodeLexi(treeModel, path[i], (MutableTreeNode) currNode, isFile);
                        ((DefaultTreeModel) treeModel).reload(currNode);
                    }
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, path[i]);
                    currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
                } else {
                    break;
                }
            }
        }
    }


    /*
     * other methods
     */

    public void setDoubleClickExpand(boolean expand) {
        doubleClickExpandEnabled = expand;
    }

    private boolean updateExpandedNode(TreeNode node, List<BaseXResource> newChildrenList, List<String> childrenValues){
        DefaultMutableTreeNode newChild;
        List<String> oldChildren = new ArrayList<>();
        String oldChild;
        boolean treeChanged = false;

        // check whether old children are in new list and vice versa
        if (node.getChildCount() > 0) {
            boolean[] inNewList = new boolean[node.getChildCount()];
            if (newChildrenList.size() > 0){
                for (int i=0; i<node.getChildCount(); i++){
                    DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)node.getChildAt(i);
                    oldChild = currNode.getUserObject().toString();
                    oldChildren.add(oldChild);
                    if (childrenValues.contains(oldChild))
                        inNewList[i] = true;
                }
            }
            for (int i=node.getChildCount()-1; i>-1; i--){
                if (!inNewList[i]) {
                    ((DefaultTreeModel) treeModel).removeNodeFromParent((MutableTreeNode) node.getChildAt(i));
                    ((DefaultTreeModel) treeModel).nodeChanged(node);
                    treeChanged = true;
                }
            }
        }
        if (node.getChildCount() == 0) {  // if old list was empty skip lexicographic insert (faster)
            for (BaseXResource newPossibleChild : newChildrenList){
                newChild = ClassFactory.getInstance().getTreeNode(newPossibleChild.getName(),
                        ((ArgonTreeNode) node).getTag().toString() + "/" + newPossibleChild.getName());
                if (newPossibleChild.getType().equals(BaseXType.DIRECTORY))
                    newChild.setAllowsChildren(true);
                else
                    newChild.setAllowsChildren(false);
                ((DefaultTreeModel) treeModel).insertNodeInto(newChild, (MutableTreeNode) node, node.getChildCount());
                treeChanged = true;
            }
        } else {
            for (BaseXResource newPossibleChild : newChildrenList){
                if (!oldChildren.contains(newPossibleChild.getName())) {
                    TreeUtils.insertStrAsNodeLexi(treeModel, newPossibleChild.getName(), (MutableTreeNode) node,
                            !(newPossibleChild.getType().equals(BaseXType.DIRECTORY)));
                    treeChanged = true;
                }
            }
        }
        return treeChanged;
    }

    public TreePath getPath() {
        return this.path;
    }

    public TreeNode getNode() {
        return this.node;
    }


    /*
     * methods for interface KeyListener
     */

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_DELETE: new DeleteAction(tree).actionPerformed(null); break;
            case KeyEvent.VK_F5: new RefreshTreeAction(tree).actionPerformed(null); break;
            // if URL raises exception, just ignore
            case KeyEvent.VK_ENTER: try {doubleClickHandler(null);} catch(ParseException pe) {} break;
            case KeyEvent.VK_INSERT: if (TreeUtils.isDir(path) || TreeUtils.isDB(path) || TreeUtils.isFileSource(path)) {
                                        new AddNewFileAction(tree).actionPerformed(null); break;
                                    }
                                    if (TreeUtils.isDbSource(path)) {
                                        new AddDatabaseAction(treeModel, this).actionPerformed(null); break;
                                    }
                                    break;
            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
