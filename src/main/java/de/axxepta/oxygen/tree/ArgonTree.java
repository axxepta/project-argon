package de.axxepta.oxygen.tree;

import javax.swing.tree.TreeModel;

import de.axxepta.oxygen.core.ClassFactory;
import ro.sync.exml.workspace.api.standalone.ui.Tree;


/**
 * Tree using custom TreeCellRenderer
 */

public class ArgonTree extends Tree {

	private static final long serialVersionUID = 1L;

	public ArgonTree(TreeModel root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(ClassFactory.getInstance().getTreeCellRenderer());
	}

}
