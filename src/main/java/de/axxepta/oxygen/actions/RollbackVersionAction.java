package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.ArgonProtocolHandler;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryTableModel;
import de.axxepta.oxygen.versioncontrol.VersionRevisionUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Markus on 07.02.2016.
 */
public class RollbackVersionAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(RollbackVersionAction.class);
    private JTable table;

    public RollbackVersionAction(String name, JTable table) {
        super(name);
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        URL[] urls = obtainURLs();

        if (table.getSelectedRows()[0] == (table.getModel().getRowCount() - 1)) {
            // Reset to last saved revision? Use undo instead!
        } else {
            int version = Integer.parseInt(table.getModel().
                    getValueAt(table.getModel().getRowCount() - 1, 0).toString());
            int revision = Integer.parseInt(table.getModel().
                    getValueAt(table.getModel().getRowCount() - 1, 1).toString());
            WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                    getEditorAccess(urls[1], PluginWorkspace.MAIN_EDITING_AREA);
            if (editorAccess.isModified()) {
                editorAccess.save();
                revision = revision + 1;
            }

            String newDocumentString = getOldRevisionString(urls[0]);

            if (newDocumentString != null) {
                String extension;
                if (URLUtils.isQuery(urls[0]))
                    extension = VersionRevisionUpdater.XQUERY;
                else
                    extension = VersionRevisionUpdater.XML;
                VersionRevisionUpdater updater = new VersionRevisionUpdater(editorAccess, extension);
                updater.updateEditorToOldRevision(newDocumentString, version, revision);
            }
        }
    }

    private URL[] obtainURLs() {
        URL[] urls = new URL[2];
        urls[0] = ((VersionHistoryTableModel) table.getModel()).getURL(table.getSelectedRows()[0]);
        urls[1] = CompareVersionsAction.obtainCurrentURLFromHistoryURL(urls[0]);
        return urls;
    }

    private String getOldRevisionString(URL url) {
        String oldDocumentString = null;
        try (Connection connection = BaseXConnectionWrapper.getConnection();) {
            InputStream oldRevisionStream = new ByteArrayInputStream(connection.get(BaseXSource.DATABASE,
                    CustomProtocolURLHandlerExtension.pathFromURL(url)));
            oldDocumentString = new Scanner(oldRevisionStream,"UTF-8").useDelimiter("\\A").next();
        } catch (IOException ex) {
            logger.error("Couldn't access old file revision during Reset To");
        }
        return  oldDocumentString;
    }

}
