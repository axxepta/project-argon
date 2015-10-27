package de.axxepta.oxygen.tree;

/**
 * TreeCellRenderer using special icons
 */

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.standalone.ui.TreeCellRenderer;
import ro.sync.ui.Icons;

/**
 * TreeCellRenderer with alternating colored rows.
 */

//public class BasexTreeCellRenderer extends DefaultTreeCellRenderer {
public class BasexTreeCellRenderer extends TreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Define a static logger variable so that it references the
	// Logger instance named "BasexTreeCellRenderer".
	private static final Logger logger = LogManager.getLogger(BasexTreeCellRenderer.class);
	TreeCellRenderer defaultRenderer = new TreeCellRenderer();
	//DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	@Override
	public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
			boolean aSelected, boolean aExpanded, boolean aLeaf, int aRow,
			boolean aHasFocus) {

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && aLeaf) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			String thisLeafFileType = fileType(aValue);
			setIcon(getOxygenIcon(thisLeafFileType));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isRoot(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(createImageIcon("/images/DbConnection16.gif"));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isDatabase(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(createImageIcon("/images/DbCatalog16.gif"));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isDBSource(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(createImageIcon("/images/DBHttp16.png"));
			return this;
		}

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode) && isSourceDir(aTree, aValue)) {
			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, true, aRow, aHasFocus);
			setIcon(createImageIcon("/images/DbFolder16.png"));
			return this;
		}

		/*
		 * if (!aLeaf) { CustomTreeNode node = (CustomTreeNode) aValue;
		 * //System.out.println(((Employee) node.getUserObject()).name);
		 * 
		 * if (node.getIcon() != null) {
		 * System.out.println(node.getIcon().toString());
		 * setClosedIcon(node.getIcon()); setOpenIcon(node.getIcon()); } else {
		 * setClosedIcon(getDefaultClosedIcon());
		 * setClosedIcon(getDefaultOpenIcon());
		 * setOpenIcon(getDefaultOpenIcon()); } }
		 */
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

	// ToDo: move to separate class
	public static javax.swing.Icon getOxygenIcon(String extension) {
		javax.swing.Icon icon;
		if (extension.equalsIgnoreCase("xml")
				|| extension.equalsIgnoreCase("dita")
				|| extension.equalsIgnoreCase("ditaval")
				|| extension.equalsIgnoreCase("ditacontrol")
				|| extension.equalsIgnoreCase("xhtml")
				|| extension.equalsIgnoreCase("opf")
				|| extension.equalsIgnoreCase("ncx")
				|| extension.equalsIgnoreCase("xlf")
				) {
			icon = ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE);
		} else if (extension.equalsIgnoreCase("ditamap")) {
			icon = Icons.getIcon(Icons.DITAMAP_TEMPLATE);
		} else if (extension.equalsIgnoreCase("epub")) {
			icon = Icons.getIcon(Icons.EPUB_TEMPLATE);
		} else if (extension.equalsIgnoreCase("cat")) {
			icon = Icons.getIcon(Icons.XML_TEMPLATE);
		} else if(extension.equalsIgnoreCase("wsdl")) {
			icon = Icons.getIcon(Icons.WSDL_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xsl")) {
			icon = Icons.getIcon(Icons.XSL_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xsd")) {
			icon = Icons.getIcon(Icons.XSD_TEMPLATE);
		} else if (extension.equalsIgnoreCase("rng")) {
			icon = Icons.getIcon(Icons.RNG_TEMPLATE);
		} else if (extension.equalsIgnoreCase("rnc")) {
			icon = Icons.getIcon(Icons.RNC_TEMPLATE);
		} else if (extension.equalsIgnoreCase("nvdl")) {
			icon = Icons.getIcon(Icons.NVDL_TEMPLATE);
		} else if (extension.equalsIgnoreCase("dtd")) {
			icon = Icons.getIcon(Icons.DTD_TEMPLATE);
		} else if (extension.equalsIgnoreCase("mod")) {
			icon = Icons.getIcon(Icons.DTD_TEMPLATE);
		} else if (extension.equalsIgnoreCase("ent")) {
			icon = Icons.getIcon(Icons.DTD_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xpl")) {
			icon = Icons.getIcon(Icons.XPROC_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xpr")) {
			icon = Icons.getIcon(Icons.XPR_TEMPLATE);
		} else if (extension.equalsIgnoreCase("css")) {
			icon = Icons.getIcon(Icons.CSS_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xquery")
				|| extension.equalsIgnoreCase("xq")
				|| extension.equalsIgnoreCase("xql")
				|| extension.equalsIgnoreCase("xqm")
				|| extension.equalsIgnoreCase("xqy")
				|| extension.equalsIgnoreCase("xu")) {
			icon = Icons.getIcon(Icons.XQUERY_TEMPLATE);
		} else if (extension.equalsIgnoreCase("html")) {
			icon = Icons.getIcon(Icons.HTML_TEMPLATE);
		} else if (extension.equalsIgnoreCase("sch")) {
			icon = Icons.getIcon(Icons.SCH_TEMPLATE);
		} else if (extension.equalsIgnoreCase("fo")) {
			icon = Icons.getIcon(Icons.FO_TEMPLATE);
		} else if (extension.equalsIgnoreCase("txt")) {
			icon = Icons.getIcon(Icons.TXT_TEMPLATE);
		} else if (extension.equalsIgnoreCase("json")) {
			icon = Icons.getIcon(Icons.JSON_TEMPLATE);
		} else if (extension.equalsIgnoreCase("js")) {
			icon = Icons.getIcon(Icons.JS_TEMPLATE);
		} else if (extension.equalsIgnoreCase("sql")) {
			icon = Icons.getIcon(Icons.SQL_TEMPLATE);
		} else if (extension.equalsIgnoreCase("php")) {
			icon = Icons.getIcon(Icons.PHP_TEMPLATE);
		} else if (extension.equalsIgnoreCase("xspec")) {
			icon = Icons.getIcon(Icons.XSPEC_TEMPLATE);
		} else {
			icon = createImageIcon(getIconFile(extension));
		}
		return icon;
	}

	// ToDo: move to separate class
	private static String getIconFile(String fileType) {
		switch (fileType) {
			case "XML":
				return "/images/xml16.png";
			case "TXT":
				return "/images/txt16.png";
			case "FILE":
				return "/images/file16.png";
			default:
				return "/images/file16.png";
		}
	}

	// ToDo: move to separate class (same as getOxygenIcon)
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = BasexTree.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			logger.error("Couldn't find file: " + path);
			return null;
		}
	}

}
