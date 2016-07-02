package de.axxepta.oxygen.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ro.sync.exml.workspace.api.standalone.ui.Tree;

import java.awt.event.MouseEvent;


/**
 * Tree using custom TreeCellRenderer
 */

public class BasexTree extends Tree {

	private static final long serialVersionUID = 1L;

	public BasexTree(TreeModel root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(new BasexTreeCellRenderer());
	}

}
