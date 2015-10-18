package de.axxepta.oxygen.tree;

import javax.swing.tree.DefaultTreeModel;

import ro.sync.exml.workspace.api.standalone.ui.Tree;


/**
 * Tree using custom TreeCellRenderer
 */

//public class BasexTree extends JTree {
public class BasexTree extends Tree {

	private static final long serialVersionUID = 1L;

	public BasexTree(DefaultTreeModel root) {
		super(root);
		// Use our custom cell renderer.
		this.setCellRenderer(new BasexTreeCellRenderer());
		// Use our custom tree UI.
		//this.setUI(new BasexTreeUI());
	}

}
