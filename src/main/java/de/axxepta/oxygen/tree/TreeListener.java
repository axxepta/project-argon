package de.axxepta.oxygen.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.core.SubjectInterface;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
//import javafx.scene.control.TreeCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import de.axxepta.oxygen.rest.BasexWrapper;
import de.axxepta.oxygen.rest.ListDBEntries;

/**
 * Created by daltiparmak on 14.04.15.
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
    private int doubleClickDelay = 300;
    private Timer timer;
    private final JPopupMenu contextMenu;
	private StandalonePluginWorkspace wsa;

    public TreeListener(BasexTree tree, DefaultTreeModel treeModel, JPopupMenu contextMenu, StandalonePluginWorkspace workspaceAccess)
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
        } catch (NullPointerException er) {}
        if ( e.isPopupTrigger() )
            contextMenu.show( e.getComponent(), e.getX(), e.getY() );
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
            newNodes = (new BaseXRequest("list", queryType, db_path.toString())).getResult();
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
/*        URL argonURL = null;
		try {
			argonURL = new URL("argon:/tmp/tmp.xml");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        this.wsa.open(argonURL);*/
    }

    private void doubleClickHandler(ActionEvent e) throws ParseException {
        logger.debug("-- double click --");
        String db_path = BasexTree.urlStringFromTreePath(this.path);
        logger.info("DbPath: " + db_path);
        if (!this.node.getAllowsChildren()) {
            // open file
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
                    this._treeModel.removeNodeFromParent(node);
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
                    insertStrAsNodeLexi(children.get(i), node, !(chTypes.get(i).equals("directory")));
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
            String[] path = message.split("/");
            currPath = new TreePath(this._treeModel.getRoot());
            // ToDo: define string constants static somewhere
            currPath = pathByAddingChildAsStr(currPath, "Databases");
            currPath = pathByAddingChildAsStr(currPath, path[0]);
            currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
            boolean expanded = false;
            Boolean isFile;
            for (int i = 0; i < path.length - 1; i++) {
                if (this._Tree.isExpanded(currPath)) expanded = true;
                if (expanded || (i == path.length - 2)) { // update tree now only if file is in visible path
                    if (isNodeAsStrChild(currNode, path[i + 1])) {
                    } else {
                        isFile = (i + 2 == path.length);
                        insertStrAsNodeLexi(path[i + 1], currNode, isFile);
                        this.newExpandEvent = false;
                        this._Tree.expandPath(currPath);
                        this._Tree.fireTreeExpanded(currPath);
                    }
                    currPath = pathByAddingChildAsStr(currPath, path[i + 1]);
                    currNode = (DefaultMutableTreeNode) currPath.getLastPathComponent();
                } else {
                    break;
                }
            }
        }

        // ToDo: if delete is called from someplace else than tree context menu, adjust path handling!
        if (type.equals("DELETE_FILE")) {
            // this is necessary because for some (inexplicable?) reason the parent node is first removed from tree???
            String parent = this.path.getParentPath().getLastPathComponent().toString();
            int depth = this.path.getPathCount();
            TreePath grannyPath = this.path.getParentPath().getParentPath();
            this._Tree.collapsePath(this.path.getParentPath());
            this._Tree.expandPath(this.path.getParentPath());
            this._Tree.collapsePath(grannyPath);
            if ((depth == 3) && (!this.path.getPathComponent(1).equals("Databases"))) {
                switch (parent) {
                    case "Query Folder": DefaultMutableTreeNode queryFolder = new DefaultMutableTreeNode("Query Folder");
                        queryFolder.setAllowsChildren(true);
                        this._treeModel.insertNodeInto(queryFolder, (DefaultMutableTreeNode)this._treeModel.getRoot(), 1);
                        break;
                    case "Repo Folder": DefaultMutableTreeNode repoFolder = new DefaultMutableTreeNode("Repo Folder");
                        repoFolder.setAllowsChildren(true);
                        this._treeModel.insertNodeInto(repoFolder, (DefaultMutableTreeNode)this._treeModel.getRoot(), 2);
                        break;
                }
            }
            this._Tree.expandPath(grannyPath);
            this._Tree.expandPath(TreeListener.pathByAddingChildAsStr(grannyPath, parent));
        }
    }

    public TreePath getPath() {
        return this.path;
    }

    public DefaultMutableTreeNode getNode() {
        return this.node;
    }

    //ToDo: Move to a BaseXTreeModel class
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

    //ToDo: Move to a BaseXTreeNode class
    private static boolean isNodeAsStrChild(DefaultMutableTreeNode parent, String child) {
        for (int i=0; i<parent.getChildCount(); i++) {
            if (((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject().toString().equals(child)) {
                return true;
            }
        }
        return false;
    }

    private void insertStrAsNodeLexi(String child, DefaultMutableTreeNode parent, Boolean childIsFile) {
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
                this._treeModel.insertNodeInto(childNode, parent, i);
                inserted = true;
                break;
            }
        }
        if (!inserted) this._treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
    }

}
