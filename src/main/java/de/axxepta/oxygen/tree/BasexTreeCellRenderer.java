package de.axxepta.oxygen.tree;

/**
 * Created by daltiparmak on 14.04.15.
 */

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

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
	// Logger instance named "CustomProtocolHandler".
	private static final Logger logger = LogManager.getLogger(BasexTreeCellRenderer.class);
	TreeCellRenderer defaultRenderer = new TreeCellRenderer();
	//DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	@Override
	public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
			boolean aSelected, boolean aExpanded, boolean aLeaf, int aRow,
			boolean aHasFocus) {

		if ((aValue != null) && (aValue instanceof DefaultMutableTreeNode)
				&& aLeaf) {
			/*
			 * JPanel panel = new JPanel(); // Create a new panel where we will
			 * show the data. String text =
			 * (String)((DefaultMutableTreeNode)aValue).getUserObject();
			 * 
			 * String thisLeafFileType = fileType(aValue); switch
			 * (thisLeafFileType) { case "xml": panel.add(new JLabel(text,
			 * this.xmlIcon, LEFT)); break; case "txt": panel.add(new
			 * JLabel(text, this.txtIcon, LEFT)); break; default: panel.add(new
			 * JLabel(text, this.fileIcon, LEFT)); } if( aSelected ) {
			 * panel.setBackground( Color.RED ); } else { if( aRow % 2 == 0 ) {
			 * panel.setBackground( Color.WHITE ); } else { panel.setBackground(
			 * new Color( 230, 230, 230 ) ); } } return panel;
			 */

			super.getTreeCellRendererComponent(aTree, aValue, aSelected,
					aExpanded, aLeaf, aRow, aHasFocus);
			String thisLeafFileType = fileType(aValue);
			setIcon(getOxygenIcon(thisLeafFileType));
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

	private static javax.swing.Icon getOxygenIcon(String extension) {
		javax.swing.Icon icon = null;
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

	private static String getIconFile(String fileType) {
		switch (fileType) {
			case "XML":
				return "/xml16.png";
			case "TXT":
				return "/txt16.png";
			case "FILE":
				return "/file16.png";
			default:
				return "/file16.png";
		}

	}

	// ToDo: move to separate class if also used in Workspace
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
