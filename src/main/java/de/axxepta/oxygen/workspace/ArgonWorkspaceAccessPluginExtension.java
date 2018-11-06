package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.*;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.ArgonInputURLChooserCustomizer;
import de.axxepta.oxygen.tree.TreePane;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.versioncontrol.VersionHistoryPanel;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.*;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

import javax.swing.*;
import java.util.List;
import java.util.Locale;

/**
 * Main plugin class, defining tree panel, version history panel, and toolbar
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    private ToolbarButton runQueryButton;
    private ToolbarButton newVersionButton;
    private ToolbarButton saveToArgonButton;
    private ToolbarButton replyCommentButton;

    @Override
    public void applicationStarted(final StandalonePluginWorkspace wsa) {
        wsa.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);

        Lang.init(wsa);
        ImageUtils.init();

        final WSOptionsStorage optionsStorage = wsa.getOptionsStorage();
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HOST));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_HTTP_PORT));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_TCP_PORT));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_USERNAME));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_PASSWORD));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_CONNECTION));
        optionsStorage.addOptionListener(new ArgonOptionListener(ArgonOptionPage.KEY_BASEX_LOGFILE));

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

    @Override
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

        static final String ARGON_WORKSPACE_A_CCESS_ID = "ArgonWorkspaceAccessID";
        static final String ARGON_WORKSPACE_ACCESS_OUTPUT_ID = "ArgonWorkspaceAccessOutputID";

        @Override
        public void customizeView(ViewInfo viewInfo) {
            final String viewID = viewInfo.getViewID();
            if (ARGON_WORKSPACE_A_CCESS_ID.equals(viewID)) {
                viewInfo.setComponent(new TreePane());
                viewInfo.setTitle(Lang.get(Lang.Keys.title_connection));
            } else if (ARGON_WORKSPACE_ACCESS_OUTPUT_ID.equals(viewID)) {
                viewInfo.setComponent(new VersionHistoryPanel());
                viewInfo.setTitle(Lang.get(Lang.Keys.title_history));
            }
        }
    }

}
