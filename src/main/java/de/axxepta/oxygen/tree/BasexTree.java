package de.axxepta.oxygen.tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.ImageIcon;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class BasexTree extends JTree {


    public BasexTree(TreeNode root)
    {
        super(root);
        // load icon files
        Icon xIcon = createImageIcon(getIconFile("XML"));
        Icon tIcon = createImageIcon(getIconFile("TXT"));
        Icon fIcon = createImageIcon(getIconFile("FILE"));
        //Use our custom cell renderer.
        this.setCellRenderer(new BasexTreeCellRenderer(xIcon, tIcon, fIcon));
        //Use our custom tree UI.
        this.setUI( new BasexTreeUI() );
    }

    public BasexTree(DefaultTreeModel root)
    {
        super(root);
        // load icon files
        Icon xIcon = createImageIcon(getIconFile("XML"));
        Icon tIcon = createImageIcon(getIconFile("TXT"));
        Icon fIcon = createImageIcon(getIconFile("FILE"));
        //Use our custom cell renderer.
        this.setCellRenderer(new BasexTreeCellRenderer(xIcon, tIcon, fIcon));
        //Use our custom tree UI.
        this.setUI( new BasexTreeUI() );
    }
    private String getIconFile(String fileType){
        switch (fileType) {
            case "XML": return "/xml16.png";
            case "TXT": return "/txt16.png";
            case "FILE": return "/file16.png";
            default: return "/file16.png";
        }

    }

    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = BasexTree.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            JOptionPane.showMessageDialog(null, "Couldn't find file: " + path, "createImageIcon", JOptionPane.PLAIN_MESSAGE);
            return null;
        }
    }

}
