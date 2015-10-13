package de.axxepta.oxygen.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
            String[] path = protocol[1].substring(1).split("/");
            currPath = new TreePath(this._treeModel.getRoot());
            // ToDo: define string constants static somewhere
            switch (protocol[0]) {
                case "argonrepo": currPath = TreeUtils.pathByAddingChildAsStr(currPath, "Repo Folder");
                    break;
                case "argonquery": currPath = TreeUtils.pathByAddingChildAsStr(currPath, "Query Folder");
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

    public TreePath getPath() {
        return this.path;
    }

    public DefaultMutableTreeNode getNode() {
        return this.node;
    }

}
