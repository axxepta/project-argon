package de.axxepta.oxygen.tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class BasexTree extends JTree {


    public BasexTree(TreeNode root)
    {
        super(root);
        //Use our custom cell renderer.
        this.setCellRenderer( new BasexTreeCellRenderer() );
        //Use our custom tree UI.
        this.setUI( new BasexTreeUI() );
    }

    public BasexTree(DefaultTreeModel root)
    {
        super(root);
        //Use our custom cell renderer.
        this.setCellRenderer( new BasexTreeCellRenderer() );
        //Use our custom tree UI.
        this.setUI( new BasexTreeUI() );
    }

}
