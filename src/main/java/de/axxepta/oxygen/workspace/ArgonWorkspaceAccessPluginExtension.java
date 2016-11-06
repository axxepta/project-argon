package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.*;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.ArgonInputURLChooserCustomizer;
import de.axxepta.oxygen.tree.*;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.versioncontrol.VersionHistoryPanel;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.standalone.*;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

import javax.swing.*;
import java.util.*;

/**
 * Main plugin class, defining tree panel, version history panel, and toolbar
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    private ToolbarButton runQueryButton;
    private ToolbarButton newVersionButton;
    private ToolbarButton saveToArgonButton;
    private ToolbarButton replyCommentButton;

    @java.lang.Override
    public void applicationStarted(final StandalonePluginWorkspace wsa) {

        wsa.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);

        // init language pack
        if (wsa.getUserInterfaceLanguage().equals("de_DE"))
            Lang.init(Locale.GERMAN);
        else
            Lang.init(Locale.UK);

        // init icon map
        ImageUtils.init();

        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HOST));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HTTP_PORT));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_TCP_PORT));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_USERNAME));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_PASSWORD));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_CONNECTION));
        wsa.getOptionsStorage().addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_LOGFILE));

        ArgonEditorsWatchMap.getInstance().init();

        wsa.addViewComponentCustomizer(new BaseXViewComponentCustomizer());

        initButtons();
        final ArgonToolbarComponentCustomizer toolbarCustomizer =
                new ArgonToolbarComponentCustomizer(runQueryButton, newVersionButton, saveToArgonButton, replyCommentButton);
        wsa.addToolbarComponentsCustomizer(toolbarCustomizer);

        wsa.addEditorChangeListener(new ArgonEditorChangeListener(wsa, runQueryButton, newVersionButton, saveToArgonButton),
                PluginWorkspace.MAIN_EDITING_AREA);
        wsa.addEditorChangeListener(new DitaMapManagerChangeListener(wsa), PluginWorkspace.DITA_MAPS_EDITING_AREA);

        wsa.addInputURLChooserCustomizer(new ArgonInputURLChooserCustomizer());
    }

    @java.lang.Override
    public boolean applicationClosing() {
        new CheckedOutFilesAction().actionPerformed(null);
        return true;
    }

    private void initButtons() {
        Action runBaseXQueryAction = new BaseXRunQueryAction(Lang.get(Lang.Keys.cm_runquery),
                ImageUtils.createImageIcon("/images/RunQuery.png"));
        Action newVersionAction = new NewVersionAction(Lang.get(Lang.Keys.cm_newversion),
                ImageUtils.createImageIcon("/images/IncVersion.png"));
        Action replyToAuthorComment = new ReplyAuthorCommentAction(Lang.get(Lang.Keys.cm_replycomment),
                ImageUtils.createImageIcon("/images/ReplyComment.png"));
        Action saveToArgonAction = new SaveFileToArgonAction(Lang.get(Lang.Keys.cm_saveas),
                ImageUtils.getIcon(ImageUtils.BASEX24ADD));
        runQueryButton = new ToolbarButton(runBaseXQueryAction, true);
        runQueryButton.setText("");
        newVersionButton = new ToolbarButton(newVersionAction, true);
        newVersionButton.setText("");
        saveToArgonButton = new ToolbarButton(saveToArgonAction, true);
        saveToArgonButton.setText("");
        replyCommentButton = new ToolbarButton(replyToAuthorComment, true);
        replyCommentButton.setText("");
    }


    private class BaseXViewComponentCustomizer implements ViewComponentCustomizer {
        /**
         * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
         */
        @Override
        public void customizeView(ViewInfo viewInfo) {

            if ("ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {
                //The view ID defined in the "plugin.xml"
                viewInfo.setComponent(new TreePane());
                viewInfo.setTitle(Lang.get(Lang.Keys.title_connection));
            } else if ("ArgonWorkspaceAccessOutputID".equals(viewInfo.getViewID())) {
                viewInfo.setComponent(new VersionHistoryPanel());
                viewInfo.setTitle(Lang.get(Lang.Keys.title_history));
            }
        }
    }

}
