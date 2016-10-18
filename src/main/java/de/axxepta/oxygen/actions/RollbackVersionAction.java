package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
    private WSEditor editorAccess;

    public RollbackVersionAction(String name, JTable table) {
        super(name);
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        URL url = obtainURL();

        if (table.getSelectedRows()[0] == (table.getModel().getRowCount() - 1)) {
            // Reset to last saved revision? Use undo instead!
        } else {
            editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
            if (editorAccess.isModified()) {
                editorAccess.save();
            }

            String newDocumentString = getOldRevisionString(url);
            if (newDocumentString != null) {
                replaceDocument(newDocumentString);
            }
        }
    }

    private URL obtainURL() {
        return ((VersionHistoryTableModel) table.getModel()).getURL(table.getSelectedRows()[0]);
    }

    private String getOldRevisionString(URL url) {
        String oldDocumentString = null;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            InputStream oldRevisionStream = new ByteArrayInputStream(connection.get(BaseXSource.DATABASE,
                    CustomProtocolURLHandlerExtension.pathFromURL(url), false));
            oldDocumentString = new Scanner(oldRevisionStream,"UTF-8").useDelimiter("\\A").next();
        } catch (IOException ex) {
            logger.error("Couldn't access old file revision during Reset To");
        }
        return oldDocumentString;
    }

    private void replaceDocument(final String newDoc) {
        if (SwingUtilities.isEventDispatchThread()) {
            replaceWholeDocument(newDoc);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        replaceWholeDocument(newDoc);
                    }
                });
            } catch (InvocationTargetException ite) {
                logger.error(ite);
            } catch (InterruptedException ie) {
                logger.error(ie);
            }
        }
    }

    private void replaceWholeDocument(String newDoc) {
        Document doc = WorkspaceUtils.getDocumentFromEditor(editorAccess);
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException el) {
            logger.error(el);
        }
        try {
            doc.insertString(0, newDoc, null);
        } catch (BadLocationException el) {
            logger.error(el);
        }
    }

}
