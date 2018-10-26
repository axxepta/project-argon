package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.MsgTopic;
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLUtils;
import de.axxepta.oxygen.utils.URLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 31.01.2016.
 */

public class VersionHistoryUpdater implements ObserverInterface<MsgTopic> {

    private final Logger logger = LogManager.getLogger(VersionHistoryUpdater.class);

    private List<VersionHistoryEntry> historyList = new ArrayList<>();
    private final JTable versionHistoryTable;

    VersionHistoryUpdater(JTable versionHistoryTable) {
        this.versionHistoryTable = versionHistoryTable;
    }

    public void update(MsgTopic type, Object... msg) {
        // ToDo: store data permanently in editor watch map to avoid repeated traffic--refresh historyList if editor is saved
        if ((msg[0] instanceof String) && !(msg[0]).equals("")) {
            String urlString = (String) msg[0];
            String resource = CustomProtocolURLUtils.pathFromURLString(urlString);

            if (urlString.startsWith(ArgonConst.ARGON + ":")) {
                try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                    historyList = connection.getHistory(resource);
                } catch (IOException ioe) {
                    logger.error("Argon connection error while getting meta data from resource " + resource + ": " + ioe.getMessage(), ioe);
                }
            } else {
                historyList = new ArrayList<>();
            }
        }
        updateVersionHistory();
    }

    public static String checkVersionHistory(URL editorLocation) {
        if ((editorLocation != null) && (URLUtils.isArgon(editorLocation)))
            return editorLocation.toString();
        else
            return "";
    }

    private void updateVersionHistory() {
        ((VersionHistoryTableModel) versionHistoryTable.getModel()).setNewContent(historyList);
        versionHistoryTable.setFillsViewportHeight(true);
        versionHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new DateTableCellRenderer());
    }

}
