package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.ArgonChooserDialog;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;

/**
 * Store the current editor's content to BaseX. The URL can be chosen from a dialog. If no encoding can be obtained from
 * the editor content, the default UTF-8 will be used.
 */
public class SaveFileToArgonAction extends AbstractAction {

    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    public SaveFileToArgonAction(String name, Icon icon) {
        super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArgonChooserDialog urlChooser = new ArgonChooserDialog((Frame)workspace.getParentFrame(),
                "Save File via BaseX Database Connection", ArgonChooserDialog.Type.SAVE);
        URL[] url =  urlChooser.selectURLs();

        WSEditor editorAccess = workspace.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if (url != null) {
            BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url[0]);
            String path = CustomProtocolURLHandlerExtension.pathFromURL(url[0]);
            if (WorkspaceUtils.newResourceOrOverwrite(source, path)) {
                if (!ConnectionWrapper.isLocked(source, path)) {
                    try {
                        WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
                        ConnectionWrapper.lock(source, path);
                        WorkspaceUtils.saveEditorToBaseXURL(editorAccess, url[0]);
                        editorAccess.close(false);
                        workspace.open(url[0]);
                        WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                    } catch (IOException ioe) {
                        WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                        workspace.showErrorMessage("Resource " + url[0].toString() +
                                " could not be stored to BaseX connection: " + ioe.getMessage());
                    }
                } else {
                    workspace.showInformationMessage("Resource " + url[0].toString() +
                            " already exists and is locked by another user");
                }
            }
        }
    }

}
