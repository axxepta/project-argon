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

	/**
	returns database name of passed TreePath, empty string, if TreePath is in restxq branch or length <2
	 */
	public static String dbStringFromTreePath(TreePath path) {
		String db_path = "";
		// ToDo: use extra class for constant strings
		if ((path.getPathCount() > 2) && (path.getPathComponent(1).toString().equals("Databases"))) {
			db_path = path.getPathComponent(2).toString();
		}
		return db_path;
	}

	/**
	 returns path of file or dir in passed TreePath, empty string, if path doesn't point deep enough
	 */
	public static String pathStringFromTreePath(TreePath path) {
		String db_path = "";
		if (path.getPathCount() > 1) {
			// ToDo: use extra class for constant strings
			int rootInd;
			if (path.getPathComponent(1).toString().equals("Databases")) {
				rootInd = 3;
			} else {
				rootInd = 2;
			}
			for (int i = rootInd; i < path.getPathCount(); i++) {
				db_path = db_path + '/' + path.getPathComponent(i).toString();
			}
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
