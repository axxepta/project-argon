package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.IOUtils;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

/**
 * @author Markus on 04.12.2015.
 * This action should be called from a toolbar button only if an Argon protocol file is opened in the current editor
 * and initiates an update of the version (and revision) number and storage of the file to BaseX.
 */
public class NewVersionAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public NewVersionAction(String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        URL url = editorAccess.getEditorLocation();
        if (url.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON)) {
            String protocol = CustomProtocolURLHandlerExtension.protocolFromURL(url);
            CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();
            if (handlerExtension.canCheckReadOnly(protocol) && !handlerExtension.isReadOnly(url)) {
                byte[] outputArray = WorkspaceUtils.getEditorByteContent(editorAccess);
                String encoding = ArgonEditorsWatchMap.getEncoding(url);
                if (!encoding.equals("UTF-8"))
                    outputArray = IOUtils.convertToUTF8(outputArray, encoding);
                updateFile(url, outputArray, encoding);
            } else {
                JOptionPane.showMessageDialog(null, "Couldn't update version of file\n" + url.toString() +
                        ".\n File is locked by other user.", "Update Version Message", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    private static void updateFile(URL url, byte[] outputArray, String encoding) {
        try {
            ConnectionWrapper.save(url, outputArray, encoding, true);
        } catch (IOException ex) {
            // ToDo: exchange by Oxygen dialog
            JOptionPane.showMessageDialog(null, "Couldn't write updated version of file\n" + url.toString(),
                    "Update Version Error", JOptionPane.PLAIN_MESSAGE);
        }
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(url));
    }

}
