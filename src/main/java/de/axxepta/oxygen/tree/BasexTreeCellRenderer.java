package de.axxepta.oxygen.tree;

/**
 * TreeCellRenderer using special icons
 */

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import de.axxepta.oxygen.utils.ImageUtils;
import ro.sync.exml.workspace.api.standalone.ui.TreeCellRenderer;

public class BasexTreeCellRenderer extends TreeCellRenderer {

	private static final long serialVersionUID = 1L;
	TreeCellRenderer defaultRenderer = new TreeCellRenderer();

	@Override
	public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
			boolean aSelected, boolean aExpanded, boolean aLeaf, int aRow,
			boolean aHasFocus) {

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && aLeaf) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			String thisLeafFileType = fileType(aValue);
			setIcon(ImageUtils.getIcon(thisLeafFileType));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isRoot(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(ImageUtils.getIcon(ImageUtils.DB_CONNECTION));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isDatabase(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(ImageUtils.getIcon(ImageUtils.DB_CATALOG));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isDBSource(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(ImageUtils.getIcon(ImageUtils.DB_HTTP));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isSourceDir(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(ImageUtils.getIcon(ImageUtils.DB_FOLDER));
			return this;
		}

		// For everything else use default renderer.
		return defaultRenderer.getTreeCellRendererComponent(aTree, aValue,
				aSelected, aExpanded, aLeaf, aRow, aHasFocus);
	}

	protected String fileType(Object value) {
		DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) value;
		String leafStr = leaf.toString();
		if (leafStr.contains(".")) {
			return leafStr.substring(leafStr.lastIndexOf(".") + 1);
		} else
			return "";
	}

	protected boolean isRoot(JTree tree, Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		return node.equals(root);
	}

	protected boolean isDatabase(JTree tree, Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DefaultMutableTreeNode db = (DefaultMutableTreeNode) tree.getModel().getChild(root, 0);
		return node.getParent().equals(db);
	}

	protected boolean isDBSource(JTree tree, Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DefaultMutableTreeNode db = (DefaultMutableTreeNode) tree.getModel().getChild(root, 0);
		return node.equals(db);
	}

	protected boolean isSourceDir(JTree tree, Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DefaultMutableTreeNode restxq = (DefaultMutableTreeNode) tree.getModel().getChild(root, 1);
		DefaultMutableTreeNode repo = (DefaultMutableTreeNode) tree.getModel().getChild(root, 2);
		return (node.equals(restxq) || node.equals(repo));
	}

}
