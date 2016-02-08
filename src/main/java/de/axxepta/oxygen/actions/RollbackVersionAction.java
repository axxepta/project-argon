package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.versioncontrol.VersionHistoryTableModel;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * @author Markus on 07.02.2016.
 */
public class RollbackVersionAction extends AbstractAction {

    private JTable table;

    public RollbackVersionAction(String name, JTable table) {
        super(name);
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        URL[] urls = obtainURLs();
        if (table.getSelectedRows()[0] == (table.getModel().getRowCount() - 1)) {
            JOptionPane.showMessageDialog(null, "You seem to try to replace the current revision of this file " +
                    "with the current copy of itself.\nReload the version history if you have changed the source in the meantime.",
                    "Reset to older version", JOptionPane.PLAIN_MESSAGE);
        } else {
            if (ArgonEditorsWatchMap.isURLInMap(urls[1])) {
                WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                        getEditorAccess(urls[1], PluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess.isModified())
                    editorAccess.save();
            }
        }
    }

    private URL[] obtainURLs() {
        URL[] urls = new URL[2];
        urls[0] = ((VersionHistoryTableModel) table.getModel()).getURL(table.getSelectedRows()[0]);
        urls[1] = CompareVersionsAction.obtainCurrentURLFromHistoryURL(urls[0]);
        return urls;
    }

}
