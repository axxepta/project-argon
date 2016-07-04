package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import java.net.URL;

/**
 * @author Markus on 02.07.2016.
 */
class ArgonEditorListener extends WSEditorListener {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    ArgonEditorListener(StandalonePluginWorkspace pluginWorkspaceAccess) {
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void editorSaved(int operationType) {
        URL editorLocation = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA).getEditorLocation();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

}
