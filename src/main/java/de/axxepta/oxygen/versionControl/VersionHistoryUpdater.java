package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.workspace.ArgonWorkspaceAccessPluginExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Markus on 31.01.2016.
 */

// ToDo: completely static instead of singleton? access to instance necessary?

public final class VersionHistoryUpdater {
    private static final VersionHistoryUpdater ourInstance = new VersionHistoryUpdater();
    private static final Logger logger = LogManager.getLogger(VersionHistoryUpdater.class);
    private static List<VersionHistoryEntry> historyList;

    private static ArgonWorkspaceAccessPluginExtension pluginWSAExtension;

/*
    public static VersionHistoryUpdater getInstance() {
        return ourInstance;
    }
*/

    private VersionHistoryUpdater() {
        historyList = new ArrayList<>();
    }

    public static void init(ArgonWorkspaceAccessPluginExtension plugin) {
        pluginWSAExtension = plugin;
        historyList = new ArrayList<>();
    }

    public static void updateAndShow(String urlString, TreePath path) {
        update(urlString, path);
        show();
    }

    public static void update(String urlString) {
        TreePath path = TreeUtils.pathFromURLString(urlString);
        update(urlString, path);
    }

    public static void update(String urlString, TreePath path) {
        historyList = new ArrayList<>();
        if (!TreeUtils.isDbSource(path) && (URLUtils.isXML(urlString) || URLUtils.isQuery(urlString))) {

            String resource = TreeUtils.resourceFromTreePath(path);
            String pathStr = obtainHistoryPath(resource, path);
            String fileName = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.lastIndexOf("."));
            String extension = urlString.substring(urlString.lastIndexOf("."));

            List<String> allVersions = obtainFileVersions(pathStr, fileName, extension);

            for (String strEntry : allVersions) {
                URL url = null;
                try {
                    url = new URL(CustomProtocolURLHandlerExtension.ARGON + ":/" + pathStr + "/" + strEntry);
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
        pluginWSAExtension.updateVersionHistory(historyList);
    }

    private static void show() {
        StandalonePluginWorkspace pluginWorkspace = (StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace();
        pluginWorkspace.showView("ArgonWorkspaceAccessOutputID", true);
    }

    private static Date parseDate(String dateStr) {
        int year = Integer.parseInt(dateStr.substring(0,4)) - 1900;
        int month = Integer.parseInt(dateStr.substring(5,7)) - 1;
        int day = Integer.parseInt(dateStr.substring(8,10));
        int hour = Integer.parseInt(dateStr.substring(11,13));
        int min = Integer.parseInt(dateStr.substring(14));
        return new Date(year, month, day, hour, min);
    }


    private static String obtainHistoryPath(String resource, TreePath path) {
        StringBuilder pathStr;
        if (TreeUtils.isInDB(path)) {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupDBBase);
        } else if (TreeUtils.isInRepo(path)) {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRepoBase);
        } else {
            pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRESTXYBase);
        }
        if (resource.lastIndexOf("/") != -1)
            pathStr.append(resource.substring(0, resource.lastIndexOf("/")));
        return pathStr.toString();
    }

    private static List<String> obtainFileVersions(String pathStr, String fileName, String extension) {
        List<String> allVersions;
        BaseXSource source = BaseXSource.DATABASE;
        try {
            allVersions = (new BaseXRequest("list", source, pathStr)).getResult();
        } catch (Exception er) {
            allVersions = new ArrayList<>();
            JOptionPane.showMessageDialog(null, "Failed to get resource list from BaseX.\n Check whether server is still running!",
                    "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
        }

        String filter = fileName + "_[0-9]{4}-[0-1][0-9]-[0-3][0-9]_[0-2][0-9]-[0-5][0-9]_v[0-9]+r[0-9]+" + extension;

        allVersions = allVersions.subList(0, allVersions.size() / 2);
        for (int i=allVersions.size()-1; i>=0; i--) {
            if (!allVersions.get(i).matches(filter))
                allVersions.remove(i);
        }
        return allVersions;
    }

}
