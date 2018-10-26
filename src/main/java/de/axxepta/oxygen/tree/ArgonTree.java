package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.core.ClassFactory;
import ro.sync.exml.workspace.api.standalone.ui.Tree;

import javax.swing.tree.TreeModel;


/**
 * Tree using custom TreeCellRenderer
 */

public class ArgonTree extends Tree {

    private static final long serialVersionUID = 1L;

    public ArgonTree(final TreeModel root) {
        super(root);
        setCellRenderer(ClassFactory.getInstance().getTreeCellRenderer());
    }

}
