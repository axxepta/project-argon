package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.utils.XMLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Markus on 31.01.2016.
 */

public class VersionHistoryUpdater implements ObserverInterface {

    private final Logger logger = LogManager.getLogger(VersionHistoryUpdater.class);

    private List<VersionHistoryEntry> historyList = new ArrayList<>();
    private JTable versionHistoryTable;

    VersionHistoryUpdater(JTable versionHistoryTable) {
        this.versionHistoryTable = versionHistoryTable;
        historyList = new ArrayList<>();
    }

    public void update(String type, Object... msg) {
        // ToDo: store data permanently in editor watch map to avoid repeated traffic--refresh historyList if editor is saved
        historyList = new ArrayList<>();
        if ((msg[0] instanceof String) && !(msg[0]).equals("")) {
            String urlString = (String) msg[0];
            String resource = CustomProtocolURLHandlerExtension.pathFromURLString(urlString);
            String historyPathStr = obtainHistoryRoot(resource, urlString);
            String metaPathStr = obtainMetaPath(resource, urlString);
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                getAndParseMetaData(connection, metaPathStr, historyPathStr);
            } catch (IOException ioe) {
                logger.warn("Argon connection error while getting meta data from resource " + resource + ": ", ioe.getMessage());
            } catch (Exception ex) {
                logger.debug("Error while parsing history meta data from resource " + resource + ": ", ex.getMessage());
            }
        }
        updateVersionHistory();
    }

    private void getAndParseMetaData(Connection connection, String resource, String historyPath) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException {
        byte[] metaData;
        metaData = connection.get(BaseXSource.DATABASE, resource, false);
        Document dom = XMLUtils.docFromByteArray(metaData);
        XPathExpression expression = XMLUtils.getXPathExpression("*//" + MetaInfoDefinition.HISTORY_FILE_TAG);
        NodeList historyFiles = (NodeList) expression.evaluate(dom, XPathConstants.NODESET);
        for (int i = 0; i < historyFiles.getLength(); i++) {
            String historyFile = historyFiles.item(i).getTextContent();
            parseHistoryEntry(historyFile, historyPath);
        }
    }

    private void parseHistoryEntry(String strEntry, String historyPath) {
        URL url = null;
        try {
            url = new URL(ArgonConst.ARGON + ":" + historyPath + "/" + strEntry);
        } catch (MalformedURLException e1) {
            logger.error(e1);
        }
        int dotPos = strEntry.lastIndexOf(".");
        int revPos = strEntry.lastIndexOf("r", dotPos);
        int verPos = strEntry.lastIndexOf("v", dotPos);
        int version = Integer.parseInt(strEntry.substring(verPos + 1, revPos));
        int revision = Integer.parseInt(strEntry.substring(revPos + 1, dotPos));
        Date changeDate = parseDate(strEntry.substring(verPos - 17, verPos - 1));
        VersionHistoryEntry versionHistoryEntry = new VersionHistoryEntry(url, version, revision, changeDate);
        historyList.add(versionHistoryEntry);
    }

    private static Date parseDate(String dateStr) {
        int year = Integer.parseInt(dateStr.substring(0,4)) - 1900;
        int month = Integer.parseInt(dateStr.substring(5,7)) - 1;
        int day = Integer.parseInt(dateStr.substring(8,10));
        int hour = Integer.parseInt(dateStr.substring(11,13));
        int min = Integer.parseInt(dateStr.substring(14));
        return new Date(year, month, day, hour, min);
    }

    private static String obtainMetaPath(String resource, String urlString) {
        StringBuilder pathStr;
        if (urlString.startsWith(ArgonConst.ARGON_XQ)) {
            pathStr = new StringBuilder(ArgonConst.META_RESTXQ_BASE);
        } else if (urlString.startsWith(ArgonConst.ARGON_REPO)) {
            pathStr = new StringBuilder(ArgonConst.META_REPO_BASE);
        } else {
            pathStr = new StringBuilder(ArgonConst.META_DB_BASE);
        }
        pathStr.append(resource).append(".xml");
        return pathStr.toString();
    }

    private static String obtainHistoryRoot(String resource, String urlString) {
        StringBuilder pathStr;
        if (urlString.startsWith(ArgonConst.ARGON_XQ)) {
            pathStr = new StringBuilder(ArgonConst.BACKUP_RESTXQ_BASE);
        } else if (urlString.startsWith(ArgonConst.ARGON_REPO)) {
            pathStr = new StringBuilder(ArgonConst.BACKUP_REPO_BASE);
        } else {
            pathStr = new StringBuilder(ArgonConst.BACKUP_DB_BASE);
            if (resource.contains("/"))
                pathStr.append(resource.substring(0, resource.indexOf("/")));
        }
        return pathStr.toString();
    }

    public static String checkVersionHistory(URL editorLocation) {
        if ((editorLocation != null) && (URLUtils.isArgon(editorLocation)))
            return editorLocation.toString();
        else
            return  "";
    }

    private void updateVersionHistory() {
        ((VersionHistoryTableModel) versionHistoryTable.getModel()).setNewContent(historyList);
        versionHistoryTable.setFillsViewportHeight(true);
        versionHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new DateTableCellRenderer());
    }

}
