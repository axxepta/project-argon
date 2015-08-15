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
        //JOptionPane.showMessageDialog(null, this.path, "Nachricht", JOptionPane.PLAIN_MESSAGE);
        //JOptionPane.showMessageDialog(null, this.node, "Nachricht", JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    private void singleClickHandler(ActionEvent e) {
        System.out.println("-- single click --");
        URL argonURL = null;
		try {
			argonURL = new URL("argon:/tmp/tmp.xml");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        this.wsa.open(argonURL);
    }

    private void doubleClickHandler(ActionEvent e) throws ParseException {
        ArrayList<String> newNodes = new ArrayList<String>();
        String[] dbReq = new String[2];
        dbReq[0] = this.path.getPathComponent(1).toString();
        dbReq[1] = "/";

        System.out.println("-- double click -- id=");
        if (this.path.getPathCount() > 1){
            try {
                newNodes = this._basexWrapper.ListDBEntries("db-entries",dbReq);
            } catch (Exception er){}
        }
        JOptionPane.showMessageDialog(null, this.path, "Nachricht", JOptionPane.PLAIN_MESSAGE);
        JOptionPane.showMessageDialog(null, this.node, "Nachricht", JOptionPane.PLAIN_MESSAGE);
        JOptionPane.showMessageDialog(null, newNodes, "Nachricht", JOptionPane.PLAIN_MESSAGE);
    }
}
