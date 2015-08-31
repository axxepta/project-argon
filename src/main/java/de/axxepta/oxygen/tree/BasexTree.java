package de.axxepta.oxygen.tree;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ui.Icons;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class BasexTree extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BasexTree(TreeNode root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(new BasexTreeCellRenderer());
		// Use our custom tree UI.
		this.setUI(new BasexTreeUI());
	}

	public BasexTree(DefaultTreeModel root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(new BasexTreeCellRenderer());
		// Use our custom tree UI.
		this.setUI(new BasexTreeUI());
	}

}
