package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.rest.BasexWrapper;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CryptoPrimitive;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class TreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener{
    private BasexTree _Tree;
    private BasexWrapper _basexWrapper;
    private TreePath path;
    private DefaultMutableTreeNode node;
    private boolean singleClick  = true;
    private int doubleClickDelay = 300;
    private Timer timer;
	private StandalonePluginWorkspace wsa;

    public TreeListener(BasexTree tree, StandalonePluginWorkspace workspaceAccess, BasexWrapper bxWrapper)
    {
    	this.wsa = workspaceAccess;
        this._Tree = tree;
        this._basexWrapper = bxWrapper;
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e ) {
                timer.stop();
                if (singleClick) {
                    singleClickHandler(e);
                } else {
                    try {
                        doubleClickHandler(e);
                    } catch (ParseException ex) {
                        Logger.getLogger(ex.getMessage());
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
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    private void singleClickHandler(ActionEvent e) {
        System.out.println("-- single click --");
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
//        JOptionPane.showMessageDialog(null, this.path, "doubleClickHandler", JOptionPane.PLAIN_MESSAGE);
 //       JOptionPane.showMessageDialog(null, this.node, "doubleClickHandler", JOptionPane.PLAIN_MESSAGE);

        System.out.println("-- double click -- id=");
        if (this.path.getPathCount() > 1) {
            ArrayList<String> newNodes;
            String db = this.path.getPathComponent(1).toString();
            String db_path = "/";
            for (int i = 2; i < this.path.getPathCount(); i++) {
                db_path = db_path + this.path.getPathComponent(i).toString() + '/';
            }
            JOptionPane.showMessageDialog(null, db+"\r\n"+ db_path, "doubleClickHandler", JOptionPane.PLAIN_MESSAGE);
            try {
                newNodes = this._basexWrapper.ListDBEntries("db-entries", db, db_path);
            } catch (Exception e1){
                e1.printStackTrace();
                newNodes = new ArrayList<String>();
                newNodes.add("Blatt1");
                newNodes.add("Blatt2");
            }
            if (updateExpandedNode(this.node, newNodes)) this._Tree.expandPath(this.path);
        }
    }

    private boolean updateExpandedNode(DefaultMutableTreeNode node, ArrayList<String> children){
        DefaultMutableTreeNode newChild;
        boolean treeChanged = false;

        if ((children.size() > 0) && (children.size() != node.getChildCount())) {
            for (int i=0; i<children.size(); i++){
                newChild = new DefaultMutableTreeNode(children.get(i));
                node.add(newChild);
            }
            treeChanged = true;
        }

/*        // check whether old children are in new list and vice versa
        if (node.getChildCount() > 0) {
            boolean[] inNewList = new boolean[node.getChildCount()];
            if (children.size() > 0){
                ArrayList<String> oldChildren = new ArrayList<>();
                for (int i=0; i<node.getChildCount(); i++){
                    DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)node.getChildAt(i);
                    oldChildren.add((String)currChild.getUserObject());
                    for (int j=0; i<children.size(); i++){
                        if (currChild.getUserObject().equals(children.get(j))) {
                            inNewList[j] = true;
                            break;
                        }
                    }
                }
                if children.removeAll(oldChildren) {}
            }
            for (int i=node.getChildCount()-1; i>-1; i--){
                if (!inNewList[i]) {
                    node.remove(i);
                    treeChanged = true;
                }
            }
        }
        for (int i=0; i<children.size(); i++){
            newChild = new DefaultMutableTreeNode(children.get(i));
            node.add(newChild);
            treeChanged = true;
        }*/

        return treeChanged;
    }
}
