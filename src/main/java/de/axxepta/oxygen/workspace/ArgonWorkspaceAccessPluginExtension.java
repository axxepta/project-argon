package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.CheckCheckedOutFilesAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.tree.*;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.versioncontrol.VersionHistoryPanel;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.standalone.*;

import java.util.*;

/**
 * Main plugin class, defining tree panel, version history panel, and toolbar
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    @java.lang.Override
    public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
        pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);

        // init language pack
        if (pluginWorkspaceAccess.getUserInterfaceLanguage().equals("de_DE"))
            Lang.init(Locale.GERMAN);
        else
            Lang.init(Locale.UK);

        // init icon map
        ImageUtils.init();

        // init connection
        BaseXConnectionWrapper.refreshFromOptions(false);

        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HOST));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HTTP_PORT));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_TCP_PORT));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_USERNAME));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_PASSWORD));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_CONNECTION));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_LOGFILE));

        ArgonEditorsWatchMap.init();

        pluginWorkspaceAccess.addViewComponentCustomizer(new BaseXViewComponentCustomizer());

        ArgonToolbarComponentCustomizer toolbarCustomizer = new ArgonToolbarComponentCustomizer(pluginWorkspaceAccess);
        pluginWorkspaceAccess.addToolbarComponentsCustomizer(toolbarCustomizer);

        pluginWorkspaceAccess.addEditorChangeListener(
                new ArgonEditorChangeListener(pluginWorkspaceAccess, toolbarCustomizer),
                PluginWorkspace.MAIN_EDITING_AREA);
    }

    @java.lang.Override
    public boolean applicationClosing() {
        new CheckCheckedOutFilesAction().actionPerformed(null);
        return true;
    }


    private class BaseXViewComponentCustomizer implements ViewComponentCustomizer {
        /**
         * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
         */
        @Override
        public void customizeView(ViewInfo viewInfo) {

            if ("ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {
                //The view ID defined in the "plugin.xml"
                viewInfo.setComponent(new TreePane(pluginWorkspaceAccess));
                viewInfo.setTitle("Argon DB Connection");
            } else if ("ArgonWorkspaceAccessOutputID".equals(viewInfo.getViewID())) {
                viewInfo.setComponent(new VersionHistoryPanel());
                viewInfo.setTitle("Argon Version History");
            }
        }
    }

}
