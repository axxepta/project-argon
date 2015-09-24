package de.axxepta.oxygen.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ui.Icons;

import java.awt.event.ActionEvent;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class BasexTree extends JTree {

	private TreePath path;
	private DefaultMutableTreeNode node;

	private static final long serialVersionUID = 1L;

	public BasexTree(DefaultTreeModel root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(new BasexTreeCellRenderer());
		// Use our custom tree UI.
		this.setUI(new BasexTreeUI());
		path = null;
	}

	public static String urlStringFromTreePath(TreePath path) {
		String db_path = "argon:";
		for (int i = 2; i < path.getPathCount(); i++) {
			db_path = db_path + '/' + path.getPathComponent(i).toString();
		}
		return db_path;
	}

	protected void setPath(TreePath path) {
		this.path = path;
	}

	public TreePath getPath() {
		return this.path;
	}

	protected void setNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	public DefaultMutableTreeNode getNode() {
		return this.node;
	}

}
