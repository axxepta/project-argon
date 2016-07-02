package de.axxepta.oxygen.tree;

import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

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
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Listener class observing all tree-related events
 */
public class TreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener,
        KeyListener, ObserverInterface{
	
	 // Define a static logger variable so that it references the
    // Logger instance named "TreeListener".
    private static final Logger logger = LogManager.getLogger(TreeListener.class);
    
    private BasexTree tree;
    private TreeModel treeModel;
    private TreePath path;
    private TreeNode node;
    private boolean newExpandEvent;
    private boolean singleClick  = true;
    private Timer timer;
    private final BaseXPopupMenu contextMenu;
	private StandalonePluginWorkspace wsa;

    public TreeListener(BasexTree tree, TreeModel treeModel, BaseXPopupMenu contextMenu,
                        StandalonePluginWorkspace workspaceAccess)
    {
    	this.wsa = workspaceAccess;
        this.tree = tree;
        this.treeModel = treeModel;
        this.newExpandEvent = true;
        this.contextMenu = contextMenu;
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e ) {
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

        logger.info("-- tree expansion -- id=");

        if ((path.getPathCount() > 1) && (node.getAllowsChildren()) && this.newExpandEvent) {

            ArrayList<String> newNodes;
            ArrayList<String> newTypes = new ArrayList<>();
            ArrayList<String> newValues = new ArrayList<>();
            BaseXSource queryType;
            StringBuilder db_path = new StringBuilder("");

            if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_DB))){
                queryType = BaseXSource.DATABASE;
            } else if (path.getPathComponent(1).toString().equals(Lang.get(Lang.Keys.tree_restxq))) {
                queryType = BaseXSource.RESTXQ;
            } else {
                queryType = BaseXSource.REPO;
            }

            for (int i = 2; i < path.getPathCount(); i++) {
                db_path.append(path.getPathComponent(i).toString());
                db_path.append('/');
            }
            try {
                newNodes = (new BaseXRequest("list", queryType, db_path.toString())).getResult();
            } catch (Exception er) {
                newNodes = new ArrayList<>();
                logger.debug(er);
                JOptionPane.showMessageDialog(null, "Failed to get resource list from BaseX.\n Check whether server is still running!",
                        "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
            }

            if (newNodes.size() > 0) {
                newTypes.addAll(newNodes.subList(newNodes.size() / 2, newNodes.size()));
                newValues.addAll(newNodes.subList(0, newNodes.size() / 2));
            }

            if (updateExpandedNode(node, newValues, newTypes)) {
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
        // open file(s)
        TreePath[] paths = tree.getSelectionPaths();
        for (TreePath path : paths) {
            if (((TreeNode)path.getLastPathComponent()).getAllowsChildren()) {
                try {
                    treeWillExpand(new TreeExpansionEvent(this, path));
                } catch (ExpandVetoException eve) {}
            } else {
                //String db_path = ((ArgonTreeNode) path.getLastPathComponent()).getUrl();
                String db_path = TreeUtils.urlStringFromTreePath(path);
                logger.info("DbPath: " + db_path);
                if (!node.getAllowsChildren()) {
                    URL argonURL = null;
                    try {
                        argonURL = new URL(db_path);
                    } catch (MalformedURLException e1) {
                        logger.error(e1);
                    }
                    wsa.open(argonURL);
                }
            }
        }
    }


    /*
     * method for interface Observer
     */

    @Override
    public void update(String type, String message) {
        // is notified as observer when changes have been made to the database file structure
        // updates the tree if necessary
        TreeNode currNode;
        TreePath currPath;

        logger.info("Tree needs to update: " + message);

        if (type.equals("SAVE_FILE")) {
            String[] protocol = message.split(":/*");
            String[] path = protocol[1].split("/");
            currPath = new TreePath(treeModel.getRoot());
            switch (protocol[0]) {
                case CustomProtocolURLHandlerExtension.ARGON_REPO:
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, Lang.get(Lang.Keys.tree_repo));
                    break;
                case CustomProtocolURLHandlerExtension.ARGON_XQ:
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
                        isFile = (i + 1 == path.length);
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

    private boolean updateExpandedNode(TreeNode node, ArrayList<String> children, ArrayList<String> chTypes){
        DefaultMutableTreeNode newChild;
        ArrayList<String> oldChildren = new ArrayList<>();
        String oldChild;
        boolean treeChanged = false;

        // check whether old children are in new list and vice versa
        if (node.getChildCount() > 0) {
            boolean[] inNewList = new boolean[node.getChildCount()];
            if (children.size() > 0){
                for (int i=0; i<node.getChildCount(); i++){
                    DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)node.getChildAt(i);
                    oldChild = currNode.getUserObject().toString();
                    oldChildren.add(oldChild);
                    if (children.contains(oldChild)) inNewList[i] = true;
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
            for (int i=0; i<children.size(); i++){
                newChild = ClassFactory.getInstance().getTreeNode(children.get(i),
                        ((ArgonTreeNode) node).getUrl() + "/" + children.get(i));
                if (chTypes.get(i).equals("directory")) newChild.setAllowsChildren(true);
                else newChild.setAllowsChildren(false);
                ((DefaultTreeModel) treeModel).insertNodeInto(newChild, (MutableTreeNode) node, node.getChildCount());
                treeChanged = true;
            }
        } else {
            for (int i=0; i<children.size(); i++){
                if (!oldChildren.contains(children.get(i))) {
                    TreeUtils.insertStrAsNodeLexi(treeModel, children.get(i), (MutableTreeNode) node,
                            !(chTypes.get(i).equals("directory")));
                    treeChanged = true;
                }
            }
        }

        return treeChanged;
    }

    // ToDo:  implement this method as part of a listener class that is added to the contextMenu
    public static void prepareContextMenu(BaseXPopupMenu contextMenu, TreePath path){

        // at what kind of node was the context menu invoked?
        boolean isFile = TreeUtils.isFile(path);
        boolean isDir = TreeUtils.isDir(path);
        boolean isDB = TreeUtils.isDB(path);
        boolean isFileSource = TreeUtils.isFileSource(path);

        // check whether items apply to node
        int itemCount = contextMenu.getItemCount();
        for (int i=0; i<itemCount; i++){

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_checkout))) {
                if (isFile)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_checkin))) {
                if (isDir || isDB || isFileSource)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_adddb))) {
                if (TreeUtils.isDbSource(path))
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_delete))) {
                if (isFile || isDir)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_rename))) {
                if (isFile || (isDir && !TreeUtils.isWEBINF(path)))  // never! try to change the name of a WEB-INF folder
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_add))) {
                if (isDir || isDB || isFileSource)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_newversion))) {
                if (isFile)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }


            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_showversion))) {
                if (isFile)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals(Lang.get(Lang.Keys.cm_search))) {
                if (isDir || isDB || isFileSource)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

        }
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
            case KeyEvent.VK_DELETE: new DeleteAction(tree, this).actionPerformed(null); break;
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
