package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.workspace.ArgonWorkspaceAccessPluginExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Markus on 31.01.2016.
 */

// ToDo: completely static instead of singleton? access to instance necessary?

public final class VersionHistory {
    private static final VersionHistory ourInstance = new VersionHistory();
    private static final Logger logger = LogManager.getLogger(VersionHistory.class);
    private List<HistoryEntry> historyList;

    private static String[] columnNames = {"Version", "Revision", "Date"};
    private JTable table;
    private ArgonWorkspaceAccessPluginExtension pluginWSAExtension;

    public static VersionHistory getInstance() {
        return ourInstance;
    }

    private VersionHistory() {
        historyList = new ArrayList<>();
    }

//    public void update(String path, List<String> strEntries, ArgonWorkspaceAccessPluginExtension pluginWSAExtension) {
    public void update(String path, List<String> strEntries) {
        this.historyList = new ArrayList<>();
        //this.pluginWSAExtension = pluginWSAExtension;
        for (String strEntry : strEntries) {
            URL url = null;
            try {
                url = new URL(CustomProtocolURLHandlerExtension.ARGON + "://" + path + "/" + strEntry);
            } catch (MalformedURLException e1) {
                logger.error(e1);
            }
            int dotPos = strEntry.lastIndexOf(".");
            int revPos = strEntry.lastIndexOf("r", dotPos);
            int verPos = strEntry.lastIndexOf("v", dotPos);
            int version = Integer.parseInt(strEntry.substring(verPos, revPos));
            int revision = Integer.parseInt(strEntry.substring(revPos, dotPos));
            Date changeDate = parseDate(strEntry.substring(verPos - 17, verPos - 1));
            HistoryEntry historyEntry = new HistoryEntry(url, version, revision, changeDate);
            historyList.add(historyEntry);
        }
        show();
    }

    private void show() {
        Object[][] data = new Object[historyList.size()][];
        for (int i=0; i<historyList.size(); i++) {
            data[i] = historyList.get(i).getDisplayVector();
        }
        table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);
        //pluginWSAExtension.setVersionHistoryTable(table);
        StandalonePluginWorkspace pluginWorkspace = (StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace();
        pluginWorkspace.showView("ArgonWorkspaceAccessOutputID", true);
    }

    private Date parseDate(String dateStr) {
        int year = Integer.parseInt(dateStr.substring(0,4));
        int month = Integer.parseInt(dateStr.substring(5,7));
        int day = Integer.parseInt(dateStr.substring(8,10));
        int hour = Integer.parseInt(dateStr.substring(11,13));
        int min = Integer.parseInt(dateStr.substring(14));
        Date date = new Date(year, month, day, hour, min);
        return date;
    }

    private class HistoryEntry {
        private URL url;
        private int version;
        private int revision;
        private Date changeDate;

        private HistoryEntry(URL url, int version, int revision, Date changeDate) {
            this.url = url;
            this.version = version;
            this.revision = revision;
            this.changeDate = changeDate;
        }

        private Object[] getDisplayVector() {
            Object[] displayVector = {version, revision, changeDate};
            return displayVector;
        }

    }
}
