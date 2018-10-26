package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.IOUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
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

    private static final Logger logger = LogManager.getLogger(NewVersionAction.class);

    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    public NewVersionAction(String name, Icon icon) {
        super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final WSEditor editorAccess = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        final URL url = editorAccess.getEditorLocation();
        if (url.toString().startsWith(ArgonConst.ARGON)) {
            final String protocol = url.getProtocol();
            final CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();
            if (handlerExtension.canCheckReadOnly(protocol) && !handlerExtension.isReadOnly(url)) {
                WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
                final String encoding = ArgonEditorsWatchMap.getInstance().getEncoding(url);
                byte[] outputArray = WorkspaceUtils.getEditorByteContent(editorAccess);
                if (!encoding.equals("UTF-8")) {
                    outputArray = IOUtils.convertToUTF8(outputArray, encoding);
                }
                updateFile(url, outputArray, encoding);
                WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
            } else {
                workspace.showInformationMessage(Lang.get(Lang.Keys.msg_noupdate1) + " " + url.toString() + ".\n" +
                        Lang.get(Lang.Keys.msg_noupdate2) + "\nActually, you have to check the file out first");
            }
        }
    }

    private static void updateFile(URL url, byte[] outputArray, String encoding) {
        try {
            if (IOUtils.isXML(outputArray)) {
                ConnectionWrapper.save(url, outputArray, encoding, true);
            } else {
                ConnectionWrapper.save(true, url, outputArray, true);
            }
        } catch (IOException ex) {
            WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
            workspace.showInformationMessage(Lang.get(Lang.Keys.warn_failednewversion) + "\n" + url.toString());
        }
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(url));
    }

}
