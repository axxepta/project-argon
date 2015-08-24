package de.axxepta.oxygen.tree;

/**
 * Created by daltiparmak on 14.04.15.
 */

import javax.swing.*;
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
    Icon xmlIcon, txtIcon, fileIcon;

    public BasexTreeCellRenderer(Icon xIcon, Icon tIcon, Icon fIcon){
        xmlIcon = xIcon;
        txtIcon = tIcon;
        fileIcon = fIcon;
    }

    @Override
    public Component getTreeCellRendererComponent( JTree aTree, Object aValue, boolean aSelected,
                                                   boolean aExpanded, boolean aLeaf, int aRow, boolean aHasFocus )
    {

        if( (aValue != null) && (aValue instanceof DefaultMutableTreeNode) && aLeaf ) {
            JPanel panel = new JPanel(); // Create a new panel where we will show the data.
            String text = (String)((DefaultMutableTreeNode)aValue).getUserObject();

            String thisLeafFileType = fileType(aValue);
            switch (thisLeafFileType) {
                case "xml": panel.add(new JLabel(text, this.xmlIcon, LEFT));
                    break;
                case "txt": panel.add(new JLabel(text, this.txtIcon, LEFT));
                    //setIcon(this.txtIcon);
                    break;
                default: //setIcon(this.fileIcon);
                    panel.add(new JLabel(text, this.fileIcon, LEFT));
            }
            //panel.add( new JLabel( text ) );
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

    protected String fileType(Object value) {
        DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)value;
        String leafStr = leaf.toString();
        if (leafStr.contains(".")) {
            return leafStr.substring(leafStr.lastIndexOf(".")+1);
        } else return "";
    }
}
