package de.axxepta.oxygen.workspace;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import java.net.URL;

/**
 * @author Markus on 02.07.2016.
 */
public class ArgonEditorListener extends WSEditorListener {

    StandalonePluginWorkspace pluginWorkspaceAccess;

    ArgonEditorListener(StandalonePluginWorkspace pluginWorkspaceAccess) {
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void editorSaved(int operationType) {
        URL editorLocation = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA).getEditorLocation();
        ArgonWorkspaceAccessPluginExtension.checkVersionHistory(editorLocation);
    }

}
