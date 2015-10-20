package de.axxepta.oxygen.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Listener class observing all tree-related events
 */
public class TreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener, ObserverInterface{
	
	 // Define a static logger variable so that it references the
    // Logger instance named "TreeListener".
    private static final Logger logger = LogManager.getLogger(TreeListener.class);
    
    private BasexTree _Tree;
    private DefaultTreeModel _treeModel;
    private TreePath path;
    private DefaultMutableTreeNode node;
    private boolean newExpandEvent;
    private boolean singleClick  = true;
    private Timer timer;
    private final BaseXPopupMenu contextMenu;
	private StandalonePluginWorkspace wsa;

    public TreeListener(BasexTree tree, DefaultTreeModel treeModel, BaseXPopupMenu contextMenu, StandalonePluginWorkspace workspaceAccess)
    {
    	this.wsa = workspaceAccess;
        this._Tree = tree;
        this._treeModel = treeModel;
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

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            singleClick = true;
            timer.start();
        } else {
            singleClick = false;
        }
    }

    @Override public void mouseReleased( MouseEvent e ) {
        this.path = this._Tree.getPathForLocation(e.getX(), e.getY());
        try {
            this.node = (DefaultMutableTreeNode) this.path.getLastPathComponent();
        } catch (NullPointerException er) {er.printStackTrace();}
        if ( e.isPopupTrigger() )
            contextMenu.show(e.getComponent(), e.getX(), e.getY(), this.path);
    }

    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        this.path = e.getNewLeadSelectionPath();
        this.node = (DefaultMutableTreeNode)this._Tree.getLastSelectedPathComponent();
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        // method is called twice, if new data is loaded--prevent database check in 2nd call
        boolean newTreeExpandEvent = this.newExpandEvent;

        this.path = event.getPath();
        this.node = (DefaultMutableTreeNode) this.path.getLastPathComponent();

        logger.info("-- tree expansion -- id=");

        if ((this.path.getPathCount() > 1) && (this.node.getAllowsChildren()) && this.newExpandEvent) {

            ArrayList<String> newNodes;
            ArrayList<String> newTypes = new ArrayList<>();
            ArrayList<String> newValues = new ArrayList<>();
            BaseXSource queryType;
            StringBuilder db_path = new StringBuilder("");

            if (this.path.getPathComponent(1).toString().equals("Databases")){
                queryType = BaseXSource.DATABASE;
            } else if (this.path.getPathComponent(1).toString().equals("Query Folder")) {
                queryType = BaseXSource.RESTXQ;
            } else {
                queryType = BaseXSource.REPO;
            }

            for (int i = 2; i < this.path.getPathCount(); i++) {
                db_path.append(this.path.getPathComponent(i).toString());
                db_path.append('/');
            }
            //JOptionPane.showMessageDialog(null, db+"\r\n"+ db_path, "doubleClickHandler", JOptionPane.PLAIN_MESSAGE);
            try {
                newNodes = (new BaseXRequest("list", queryType, db_path.toString())).getResult();
            } catch (Exception er) {
                newNodes = new ArrayList<>();
                JOptionPane.showMessageDialog(null, "Failed to get resource list from BaseX.\n Check whether server is still running!",
                        "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
            }

            if (newNodes.size() > 0) {
                newTypes.addAll(newNodes.subList(newNodes.size() / 2, newNodes.size()));
                newValues.addAll(newNodes.subList(0, newNodes.size() / 2));
            }

            if (updateExpandedNode(this.node, newValues, newTypes)) {
                this.newExpandEvent = false;
                this._Tree.expandPath(this.path);
            }

        }
        if (!newTreeExpandEvent) this.newExpandEvent = true;
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    private void singleClickHandler(ActionEvent e) {
        logger.debug("-- single click --");
    }

    private void doubleClickHandler(ActionEvent e) throws ParseException {
        logger.debug("-- double click --");
        // open file
        String db_path = TreeUtils.urlStringFromTreePath(this.path);
        logger.info("DbPath: " + db_path);
        if (!this.node.getAllowsChildren()) {
            URL argonURL = null;
            try {
                argonURL = new URL(db_path);
            } catch (MalformedURLException e1) {
                logger.error(e1);
            }
            this.wsa.open(argonURL);
        }
    }

    private boolean updateExpandedNode(DefaultMutableTreeNode node, ArrayList<String> children, ArrayList<String> chTypes){
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
                    this._treeModel.removeNodeFromParent((DefaultMutableTreeNode) node.getChildAt(i));
                    this._treeModel.nodeChanged(node);
                    treeChanged = true;
                }
            }
        }
        if (node.getChildCount() == 0) {  // if old list was empty skip lexicographic insert (faster)
            for (int i=0; i<children.size(); i++){
                newChild = new DefaultMutableTreeNode(children.get(i));
                if (chTypes.get(i).equals("directory")) newChild.setAllowsChildren(true);
                else newChild.setAllowsChildren(false);
                this._treeModel.insertNodeInto(newChild, node, node.getChildCount());
                treeChanged = true;
            }
        } else {
            for (int i=0; i<children.size(); i++){
                if (!oldChildren.contains(children.get(i))) {
                    TreeUtils.insertStrAsNodeLexi(this._treeModel , children.get(i), node, !(chTypes.get(i).equals("directory")));
                    treeChanged = true;
                }
            }
        }

        return treeChanged;
    }

    @Override
    public void update(String type, String message) {
        // is notified as observer when changes have been made to the database file structure
        // updates the tree if necessary
        DefaultMutableTreeNode currNode;
        TreePath currPath;

        logger.info("Tree needs to update: " + message);

        if (type.equals("SAVE_FILE")) {
            String[] protocol = message.split(":");
            String[] path;
            if (protocol[1].substring(0,1).equals("/"))
                path = protocol[1].substring(1).split("/");
            else
                path = protocol[1].split("/");
            currPath = new TreePath(this._treeModel.getRoot());
            switch (protocol[0]) {
                case CustomProtocolURLHandlerExtension.ARGON_REPO:
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, "Repo Folder");
                    break;
                case CustomProtocolURLHandlerExtension.ARGON_XQ:
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, "Query Folder");
                    break;
                default: currPath = TreeUtils.pathByAddingChildAsStr(currPath, "Databases");
            }
            currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
            boolean expanded = false;
            Boolean isFile;
            for (int i = 0; i < path.length; i++) {
                if (this._Tree.isExpanded(currPath)) expanded = true;
                if (expanded || (i == path.length - 1)) { // update tree now only if file is in visible path
                    if (!TreeUtils.isNodeAsStrChild(currNode, path[i])) {
                        isFile = (i + 1 == path.length);
                        TreeUtils.insertStrAsNodeLexi(this._treeModel, path[i], currNode, isFile);
                        this._treeModel.reload(currNode);
                    }
                    currPath = TreeUtils.pathByAddingChildAsStr(currPath, path[i]);
                    currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
                } else {
                    break;
                }
            }
        }
    }

    // ToDo:  implement this method as part of a listener class that is added to the contextMenu
    public static void prepareContextMenu(BaseXPopupMenu contextMenu, TreePath path){

        // at what kind of node was the context menu invoked?
        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        int pathCount = path.getPathCount();
        // ToDo: use constant string class
        boolean isFile = !clickedNode.getAllowsChildren();
        boolean isDir = (clickedNode.getAllowsChildren() &&
                            ( ((pathCount > 3) &&
                                    (path.getPathComponent(1).toString().equals("Databases"))) ||
                              ((pathCount > 2) &&
                                      (!path.getPathComponent(1).toString().equals("Databases"))) ) );
        boolean isDB = (pathCount == 3) &&
                        (path.getPathComponent(1).toString().equals("Databases"));
        boolean isSource = (pathCount == 2);
        boolean isRoot = (pathCount == 1);
        boolean isFileSource = (isSource && !path.getPathComponent(1).toString().equals("Databases"));

        // check whether items apply to node
        int itemCount = contextMenu.getItemCount();
        // ToDo: use constant string class
        for (int i=0; i<itemCount; i++){

            if ( contextMenu.getItemName(i).equals("Check Out")) {
                if (isFile)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals("Check In")) {
                if ((isDir) || (isDB) || isFileSource)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals("Delete")) {
                if (isFile || isDir)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals("Add")) {
                if (isDir || isDB || isFileSource)
                    contextMenu.setItemEnabled(i, true);
                else
                    contextMenu.setItemEnabled(i, false);
            }

            if ( contextMenu.getItemName(i).equals("Search in Path")) {
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

    public DefaultMutableTreeNode getNode() {
        return this.node;
    }

}
