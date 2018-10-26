package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.utils.ImageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.standalone.ui.TreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ArgonTreeCellRenderer extends TreeCellRenderer {

    private static final Logger logger = LogManager.getLogger(ArgonTreeCellRenderer.class);

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
                                                  boolean aSelected, boolean aExpanded, boolean aLeaf, int aRow,
                                                  boolean aHasFocus) {
//        if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode)) {
            if (aLeaf) {
                super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, true, aRow, aHasFocus);
                String thisLeafFileType = fileType(aValue.toString());
                setIcon(ImageUtils.getIcon(thisLeafFileType));
                return this;
            } else if (isRoot(aTree, aValue)) {
                super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, true, aRow, aHasFocus);
                setIcon(ImageUtils.getIcon(ImageUtils.DB_CONNECTION));
                return this;
            } else if (isDatabase(aTree, aValue)) {
                super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, true, aRow, aHasFocus);
                setIcon(ImageUtils.getIcon(ImageUtils.DB_CATALOG));
                return this;
            } else if (isDBSource(aTree, aValue)) {
                super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, true, aRow, aHasFocus);
                setIcon(ImageUtils.getIcon(ImageUtils.DB_HTTP));
                return this;
//            } else if (isSourceDir(aTree, aValue)) {
//                super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, true, aRow, aHasFocus);
//                setIcon(ImageUtils.getIcon(ImageUtils.DB_FOLDER));
//                return this;
            }
//        }
        // For everything else use default renderer.
        return super.getTreeCellRendererComponent(aTree, aValue, aSelected, aExpanded, aLeaf, aRow, aHasFocus);
    }

    public static String fileType(String leafStr) {
        if (leafStr.contains(".")) {
            return leafStr.substring(leafStr.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private boolean isRoot(JTree tree, Object value) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        return node.equals(root);
    }

    private boolean isDatabase(JTree tree, Object value) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        final DefaultMutableTreeNode db = (DefaultMutableTreeNode) tree.getModel().getChild(root, 0);
        return node.getParent().equals(db);
    }

    private boolean isDBSource(JTree tree, Object value) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        final DefaultMutableTreeNode db = (DefaultMutableTreeNode) tree.getModel().getChild(root, 0);
        return node.equals(db);
    }

    private boolean isSourceDir(JTree tree, Object value) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        final DefaultMutableTreeNode restxq = (DefaultMutableTreeNode) tree.getModel().getChild(root, 1);
        final DefaultMutableTreeNode repo = (DefaultMutableTreeNode) tree.getModel().getChild(root, 2);
        return (node.equals(restxq) || node.equals(repo));
    }

}
