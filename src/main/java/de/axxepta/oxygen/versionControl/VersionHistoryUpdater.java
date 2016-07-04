package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.URLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
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


    public VersionHistoryUpdater(JTable versionHistoryTable) {
        this.versionHistoryTable = versionHistoryTable;
        historyList = new ArrayList<>();
    }

    public void update(String type, String urlString) {

        historyList = new ArrayList<>();
        if (!urlString.equals("")) {

            String resource = CustomProtocolURLHandlerExtension.pathFromURLString(urlString);
            String pathStr = obtainHistoryPath(resource, urlString);
            String fileName = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.lastIndexOf("."));
            String extension = urlString.substring(urlString.lastIndexOf("."));

            List<String> allVersions = obtainFileVersions(pathStr, fileName, extension);

            for (String strEntry : allVersions) {
                URL url = null;
                try {
                    url = new URL(CustomProtocolURLHandlerExtension.ARGON + "://" + pathStr + "/" + strEntry);
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
        }
        updateVersionHistory();
    }

    private static Date parseDate(String dateStr) {
        int year = Integer.parseInt(dateStr.substring(0,4)) - 1900;
        int month = Integer.parseInt(dateStr.substring(5,7)) - 1;
        int day = Integer.parseInt(dateStr.substring(8,10));
        int hour = Integer.parseInt(dateStr.substring(11,13));
        int min = Integer.parseInt(dateStr.substring(14));
        return new Date(year, month, day, hour, min);
    }

    private static String obtainHistoryPath(String resource, String urlString) {
        StringBuilder pathStr;
        if (urlString.startsWith(CustomProtocolURLHandlerExtension.ARGON_XQ)) {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRESTXYBase);
        } else if (urlString.startsWith(CustomProtocolURLHandlerExtension.ARGON_REPO)) {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRepoBase);
        } else {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupDBBase);
        }
        if (resource.lastIndexOf("/") != -1)
            pathStr.append(resource.substring(0, resource.lastIndexOf("/")));
        return pathStr.toString();
    }

    private static List<String> obtainFileVersions(String pathStr, String fileName, String extension) {
        //ToDo: exchange by query to metadata file -- maybe store data permanently in editor watch map to avoid repeated traffic
        //      then the editor watch map needed to be notified about save processes!
        List<String> allVersions = new ArrayList<>();
        BaseXSource source = BaseXSource.DATABASE;

        List<BaseXResource> allOldVersions;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            allOldVersions = connection.list(source, pathStr);
        } catch (IOException ioe) {
            allOldVersions = new ArrayList<>();
        }

        String filter = fileName + "_[0-9]{4}-[0-1][0-9]-[0-3][0-9]_[0-2][0-9]-[0-5][0-9]_v[0-9]+r[0-9]+" + extension;

        for (int i=allOldVersions.size()-1; i>=0; i--) {
            if (!allOldVersions.get(i).getType().equals(BaseXType.DIRECTORY) && allOldVersions.get(i).getName().matches(filter))
                allVersions.add(allOldVersions.get(i).getName());
        }
        return allVersions;
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
