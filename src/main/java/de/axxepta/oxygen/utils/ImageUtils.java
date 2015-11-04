package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.tree.BasexTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ui.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Markus on 04.11.2015.
 */
public class ImageUtils {

    private static final Logger logger = LogManager.getLogger(ImageUtils.class);

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
            icon = ImageUtils.createImageIcon(getIconFile(extension));
        }
        return icon;
    }


    public static Image createImage(String path) {
        Icon icon = createImageIcon(path);
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        }
        else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = BasexTree.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.error("Couldn't find file: " + path);
            return null;
        }
    }

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
}
