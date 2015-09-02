package de.axxepta.oxygen.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.Timer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import de.axxepta.oxygen.rest.BasexWrapper;
import de.axxepta.oxygen.rest.ListDBEntries;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class TreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener{
	
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
	private StandalonePluginWorkspace wsa;

    public TreeListener(BasexTree tree, DefaultTreeModel treeModel, StandalonePluginWorkspace workspaceAccess, BasexWrapper bxWrapper)
    {
    	this.wsa = workspaceAccess;
        this._Tree = tree;
        this._treeModel = treeModel;
        this.newExpandEvent = true;
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

            if (!((this.path.getPathCount()==2) && (this.path.getPathComponent(1).toString().equals("Databases")))){

                ArrayList<String> newNodes = new ArrayList<>();
                ArrayList<String> newTypes = new ArrayList<>();
                ArrayList<String> newValues = new ArrayList<>();
                String db;
                String queryType;
                int folderDepth;

                if (this.path.getPathComponent(1).toString().equals("Databases")){
                    db = this.path.getPathComponent(2).toString();
                    queryType = "db";
                    folderDepth = 3;
                } else {
                    db = "";
                    queryType = "restxq";
                    folderDepth = 2;
                }

                String db_path = "/";
                for (int i = folderDepth; i < this.path.getPathCount(); i++) {
                    db_path = db_path + this.path.getPathComponent(i).toString() + '/';
                }
                //JOptionPane.showMessageDialog(null, db+"\r\n"+ db_path, "doubleClickHandler", JOptionPane.PLAIN_MESSAGE);
                try {
                    ListDBEntries newEntries = new ListDBEntries(queryType, db, db_path);
                    newNodes = newEntries.getResult();
                } catch (Exception e1) {
                    logger.error(e1);
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
        String db_path = "argon:";
        for (int i = 2; i < this.path.getPathCount(); i++) {
            db_path = db_path + '/' + this.path.getPathComponent(i).toString();
        }
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
        for (int i=0; i<children.size(); i++){
            if (!oldChildren.contains(children.get(i))) {
                newChild = new DefaultMutableTreeNode(children.get(i));
                if (chTypes.get(i).equals("directory")) newChild.setAllowsChildren(true);
                else newChild.setAllowsChildren(false);
                //ToDo: adapt insert pos lexically (and directories before files)
                this._treeModel.insertNodeInto(newChild, node, node.getChildCount());
                treeChanged = true;
            }
        }

        return treeChanged;
    }
}
