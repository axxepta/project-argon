package de.axxepta.oxygen.tree;

/**
 * Created by daltiparmak on 14.04.15.
 */

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * TreeCellRenderer with alternating colored rows.
 */
public class BasexTreeCellRenderer extends DefaultTreeCellRenderer
{
    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    @Override
    public Component getTreeCellRendererComponent( JTree aTree, Object aValue, boolean aSelected,
                                                   boolean aExpanded, boolean aLeaf, int aRow, boolean aHasFocus )
    {
        JPanel panel = new JPanel(); // Create a new panel where we will show the data.
        String text = (String)((DefaultMutableTreeNode)aValue).getUserObject();

        panel.add( new JLabel( text ) ); // Actually show the data.

        // If the value is not null and is a tree node and a leaf then paint it.
        if( (aValue != null) && (aValue instanceof DefaultMutableTreeNode) && aLeaf )
        {
            if( aSelected )
            {
                panel.setBackground( Color.RED );
            }
            else
            {
                if( aRow % 2 == 0 )
                {
                    panel.setBackground( Color.WHITE );
                }
                else
                {
                    panel.setBackground( new Color( 230, 230, 230 ) );
                }
            }
            panel.setEnabled( aTree.isEnabled() );
            return panel;
        }

        /*
        if (!aLeaf) {
            CustomTreeNode node = (CustomTreeNode) aValue;
            //System.out.println(((Employee) node.getUserObject()).name);

            if (node.getIcon() != null) {
                System.out.println(node.getIcon().toString());
                setClosedIcon(node.getIcon());
                setOpenIcon(node.getIcon());
            } else {
                setClosedIcon(getDefaultClosedIcon());
                setClosedIcon(getDefaultOpenIcon());
                setOpenIcon(getDefaultOpenIcon());
            }
        }
        */
        // For everything else use default renderer.
        return defaultRenderer.getTreeCellRendererComponent( aTree, aValue, aSelected, aExpanded, aLeaf,
                aRow, aHasFocus );
    }
}
