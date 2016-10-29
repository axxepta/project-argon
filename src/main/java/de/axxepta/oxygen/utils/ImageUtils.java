package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.tree.ArgonTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ui.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus on 04.11.2015.
 */
public class ImageUtils {

    private static final Logger logger = LogManager.getLogger(ImageUtils.class);

    static private Map<String, Icon> iconMap;

    // CAVE: String constants as Icon keys must be lowercase because the get function makes lowercase of requested keys!
    public static final String DB_CONNECTION = "dbconnection16";
    public static final String DB_CATALOG = "dbcatalog16";
    public static final String DB_HTTP = "dbhttp16";
    public static final String DB_FOLDER = "dbfolder16";
    public static final String FOLDER = "folderclosed16";
    public static final String URL_OPEN = "openurl16";
    public static final String FILE_ADD = "addfile16";
    public static final String DB_ADD = "addb16";
    public static final String ADD_DIR = "add_dir16";
    public static final String EXPORT = "export16";
    public static final String REMOVE = "remove16";
    public static final String RENAME = "rename16";
    public static final String INC_VER = "incversion16";
    public static final String VER_HIST = "verhistory";
    public static final String REFRESH = "refresh16";
    public static final String SEARCH_PATH = "searchinpath16";
    public static final String SEARCH = "search16";
    public static final String TEAM_LIBRARY = "teamlibrary";
    public static final String SHARED = "shared";
    public static final String S_DATABASE = "s_db";
    public static final String F_DATABASE = "f_db";
    public static final String T_DATABASE = "t_db";
    public static final String P_DATABASE = "p_db";
    public static final String BASEX = "basex16";
    public static final String BASEX_LOCKED = "basex16locked";
    public static final String BASEX24 = "basex24";
    public static final String BASEX24ADD = "basex24add";
    public static final String UNLOCK = "unlock";
    public static final String SNIPPET = "snippet";
    public static final String INSERT_TEMPLATE = "inserttemplate";

    public static void init() {
        iconMap = new HashMap<>();
        iconMap.put("xml", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("dita", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("ditaval", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("ditacontrol", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("xhtml", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("opf", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("ncx", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("xlf", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("ditamap", ro.sync.ui.Icons.getIcon(Icons.DITAMAP_TEMPLATE));
        iconMap.put("epub", ro.sync.ui.Icons.getIcon(Icons.EPUB_TEMPLATE));
        iconMap.put("cat", ro.sync.ui.Icons.getIcon(Icons.XML_TEMPLATE));
        iconMap.put("wsdl", ro.sync.ui.Icons.getIcon(Icons.WSDL_TEMPLATE));
        iconMap.put("xsl", ro.sync.ui.Icons.getIcon(Icons.XSL_TEMPLATE));
        iconMap.put("xsd", ro.sync.ui.Icons.getIcon(Icons.XSD_TEMPLATE));
        iconMap.put("rng", ro.sync.ui.Icons.getIcon(Icons.RNG_TEMPLATE));
        iconMap.put("rnc", ro.sync.ui.Icons.getIcon(Icons.RNC_TEMPLATE));
        iconMap.put("nvdl", ro.sync.ui.Icons.getIcon(Icons.NVDL_TEMPLATE));
        iconMap.put("dtd", ro.sync.ui.Icons.getIcon(Icons.DTD_TEMPLATE));
        iconMap.put("mod", ro.sync.ui.Icons.getIcon(Icons.DTD_TEMPLATE));
        iconMap.put("ent", ro.sync.ui.Icons.getIcon(Icons.DTD_TEMPLATE));
        iconMap.put("xpl", ro.sync.ui.Icons.getIcon(Icons.XPROC_TEMPLATE));
        iconMap.put("xpr", ro.sync.ui.Icons.getIcon(Icons.XPR_TEMPLATE));
        iconMap.put("css", ro.sync.ui.Icons.getIcon(Icons.CSS_TEMPLATE));
        iconMap.put("xquery", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("xq", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("xql", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("xqm", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("xqy", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("xu", ro.sync.ui.Icons.getIcon(Icons.XQUERY_TEMPLATE));
        iconMap.put("html", ro.sync.ui.Icons.getIcon(Icons.HTML_TEMPLATE));
        iconMap.put("sch", ro.sync.ui.Icons.getIcon(Icons.SCH_TEMPLATE));
        iconMap.put("fo", ro.sync.ui.Icons.getIcon(Icons.FO_TEMPLATE));
        iconMap.put("txt", ro.sync.ui.Icons.getIcon(Icons.TXT_TEMPLATE));
        iconMap.put("json", ro.sync.ui.Icons.getIcon(Icons.JSON_TEMPLATE));
        iconMap.put("js", ro.sync.ui.Icons.getIcon(Icons.JS_TEMPLATE));
        iconMap.put("sql", ro.sync.ui.Icons.getIcon(Icons.SQL_TEMPLATE));
        iconMap.put("php", ro.sync.ui.Icons.getIcon(Icons.PHP_TEMPLATE));
        iconMap.put("xspec", ro.sync.ui.Icons.getIcon(Icons.XSPEC_TEMPLATE));
        iconMap.put("file", ImageUtils.createImageIcon("/images/file16.png"));
        iconMap.put(DB_CONNECTION, ImageUtils.createImageIcon("/images/DbConnection16.gif"));
        iconMap.put(DB_CATALOG, ImageUtils.createImageIcon("/images/DbCatalog16.gif"));
        iconMap.put(DB_HTTP, ImageUtils.createImageIcon("/images/DBHttp16.png"));
        iconMap.put(DB_FOLDER, ImageUtils.createImageIcon("/images/DbFolder16.png"));
        iconMap.put(FOLDER, ImageUtils.createImageIcon("/images/FolderClosed16.png"));
        iconMap.put(URL_OPEN, ImageUtils.createImageIcon("/images/OpenURL16.gif"));
        iconMap.put(FILE_ADD, ImageUtils.createImageIcon("/images/AddFile16.png"));
        iconMap.put(INC_VER, ImageUtils.createImageIcon("/images/IncVersion16.png"));
        iconMap.put(VER_HIST, ImageUtils.createImageIcon("/images/VerHistory.png"));
        iconMap.put(DB_ADD, ImageUtils.createImageIcon("/images/AddDb.png"));
        iconMap.put(ADD_DIR, ImageUtils.createImageIcon("/images/AddDir.png"));
        iconMap.put(EXPORT, ImageUtils.createImageIcon("/images/Export16.png"));
        iconMap.put(REMOVE, ImageUtils.createImageIcon("/images/Remove16.png"));
        iconMap.put(RENAME, ImageUtils.createImageIcon("/images/Rename16.png"));
        iconMap.put(REFRESH, ImageUtils.createImageIcon("/images/Refresh16.png"));
        iconMap.put(SEARCH_PATH, ImageUtils.createImageIcon("/images/SearchInPath16.png"));
        iconMap.put(SEARCH, ImageUtils.createImageIcon("/images/SearchInFiles16.png"));
        iconMap.put(SHARED, ImageUtils.createImageIcon("/images/DbCatalogShared16.png"));
        iconMap.put(TEAM_LIBRARY, ImageUtils.createImageIcon("/images/DbCatalogTeam16.png"));
        iconMap.put(S_DATABASE, ImageUtils.createImageIcon("/images/DbCatalogS16.png"));
        iconMap.put(F_DATABASE, ImageUtils.createImageIcon("/images/DbCatalogF16.png"));
        iconMap.put(T_DATABASE, ImageUtils.createImageIcon("/images/DbCatalogT16.png"));
        iconMap.put(P_DATABASE, ImageUtils.createImageIcon("/images/DbCatalogP16.png"));
        iconMap.put(BASEX, ImageUtils.createImageIcon("/images/BaseX_16_tr.png"));
        iconMap.put(BASEX_LOCKED, ImageUtils.createImageIcon("/images/BaseXlocked.png"));
        iconMap.put(BASEX24, ImageUtils.createImageIcon("/images/BaseX24.png"));
        iconMap.put(BASEX24ADD, ImageUtils.createImageIcon("/images/BaseX24add.png"));
        iconMap.put(UNLOCK, ImageUtils.createImageIcon("/images/unlock.png"));
        iconMap.put(SNIPPET, ImageUtils.createImageIcon("/images/snippet.png"));
        iconMap.put(INSERT_TEMPLATE, ImageUtils.createImageIcon("/images/InsertTemplate16.png"));
    }

    public static Icon getIcon(String extension) {
        if (iconMap == null) {
            init();
        }
        if (iconMap.get(extension.toLowerCase()) == null) {
            return iconMap.get("file");
        } else {
            return iconMap.get(extension.toLowerCase());
        }
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
        java.net.URL imgURL = ArgonTree.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.error("Couldn't find file: " + path);
            return null;
        }
    }

}
