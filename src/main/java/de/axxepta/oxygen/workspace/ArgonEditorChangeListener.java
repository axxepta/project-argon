package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.CheckInAction;
import de.axxepta.oxygen.actions.CheckOutAction;
import de.axxepta.oxygen.actions.StoreSnippetSelectionAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.TextPopupMenuCustomizer;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;


class ArgonEditorChangeListener extends WSEditorChangeListener {

    private static final Logger logger = LogManager.getLogger(ArgonEditorChangeListener.class);

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    private final Action snippetAction = new StoreSnippetSelectionAction();
    private final Action checkInAction = new CheckInAction(Lang.get(Lang.Keys.cm_checkin), ImageUtils.getIcon(ImageUtils.UNLOCK));
    private final Action checkOutAction = new CheckOutAction(Lang.get(Lang.Keys.cm_checkout), ImageUtils.getIcon(ImageUtils.BASEX));

    private WSAuthorEditorPage currentCustomizedAuthorPageAccess;
    private WSTextEditorPage currentCustomizedTextPageAccess;
    private AuthorPopupMenuCustomizer authorPopupMenuCustomizer;
    private TextPopupMenuCustomizer textPopupMenuCustomizer;

    private ToolbarButton runQueryButton;
    private ToolbarButton newVersionButton;
    private ToolbarButton saveToArgonButton;

    ArgonEditorChangeListener(StandalonePluginWorkspace pluginWorkspace, final ToolbarButton runQueryButton,
                              final ToolbarButton newVersionButton, final ToolbarButton saveToArgonButton) {
        super();
        this.runQueryButton = runQueryButton;
        this.newVersionButton = newVersionButton;
        this.saveToArgonButton = saveToArgonButton;
        this.pluginWorkspaceAccess = pluginWorkspace;
    }

    @Override
    public void editorPageChanged(URL editorLocation) {
        customizeEditorPopupMenu();
        checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
    }

    @Override
    public void editorSelected(URL editorLocation) {
        checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
        customizeEditorPopupMenu();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

    @Override
    public void editorActivated(URL editorLocation) {
        checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
        customizeEditorPopupMenu();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

    @Override
    public void editorClosed(URL editorLocation) {
        if (editorLocation.toString().startsWith(ArgonConst.ARGON)) {
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(editorLocation);
                String path = CustomProtocolURLHandlerExtension.pathFromURL(editorLocation);
                if (connection.lockedByUser(source, path) && !ArgonEditorsWatchMap.getInstance().askedForCheckIn(editorLocation)) {

                    int checkInFile = pluginWorkspaceAccess.showConfirmDialog(
                            "Closed checked out file",
                            "You just closed a checked out file. Do you want to check it in?",
                            new String[]{"Yes", "No"},
                            new int[]{0, 1}, 0);
                    if (checkInFile == 0) {
                        connection.unlock(source, path);
                    }

                }
            } catch (IOException ioe) {
                logger.debug(ioe.getMessage());
            }
            ArgonEditorsWatchMap.getInstance().removeURL(editorLocation);
        }
    }

    @Override
    public void editorOpened(URL editorLocation) {
        logger.debug("editor opened: " + editorLocation.toString());
        if (editorLocation.toString().startsWith(ArgonConst.ARGON))
            ArgonEditorsWatchMap.getInstance().addURL(editorLocation);
        checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));

        customizeEditorPopupMenu();

        final WSEditor editorAccess = pluginWorkspaceAccess.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);
        boolean isArgon = URLUtils.isArgon(editorLocation);

        if (isArgon)
            editorAccess.addEditorListener(new ArgonEditorListener(pluginWorkspaceAccess));

        if (isArgon && URLUtils.isQuery(editorLocation))
            editorAccess.addValidationProblemsFilter(new ArgonValidationProblemsFilter(editorAccess));
    }

    private void customizeEditorPopupMenu() {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess != null) {
            if (EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())) {
                if ((currentCustomizedAuthorPageAccess != null) && (authorPopupMenuCustomizer != null)) {
                    currentCustomizedAuthorPageAccess.removePopUpMenuCustomizer(authorPopupMenuCustomizer);
                }
                currentCustomizedAuthorPageAccess = (WSAuthorEditorPage) editorAccess.getCurrentPage();
                authorPopupMenuCustomizer = new ArgonAuthorPopupMenuCustomizer();
                currentCustomizedAuthorPageAccess.addPopUpMenuCustomizer(authorPopupMenuCustomizer);
            }
            if (EditorPageConstants.PAGE_TEXT.equals(editorAccess.getCurrentPageID())) {
                if ((currentCustomizedTextPageAccess != null) && (textPopupMenuCustomizer != null)) {
                    currentCustomizedTextPageAccess.removePopUpMenuCustomizer(textPopupMenuCustomizer);
                }
                currentCustomizedTextPageAccess = (WSTextEditorPage) editorAccess.getCurrentPage();
                textPopupMenuCustomizer = new ArgonTextPopupMenuCustomizer();
                currentCustomizedTextPageAccess.addPopUpMenuCustomizer(textPopupMenuCustomizer);
            }
        }
    }

    private JMenuItem createSnippetEditorPopUpAddition() {
        final JMenuItem storeSnippetItem = new JMenuItem(snippetAction);
        storeSnippetItem.setText("Store selected Snippet");
        storeSnippetItem.setIcon(ImageUtils.getIcon(ImageUtils.SNIPPET));
        return storeSnippetItem;
    }

    private JMenuItem createCheckInEditorPopUpAddition() {
        return new JMenuItem(checkInAction);
    }

    private JMenuItem createCheckOutEditorPopUpAddition() {
        return new JMenuItem(checkOutAction);
    }

    private void checkEditorDependentMenuButtonStatus(PluginWorkspace pluginWorkspaceAccess){
        WSEditor currentEditor = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if(currentEditor == null) {
            runQueryButton.setEnabled(false);
            newVersionButton.setEnabled(false);
            saveToArgonButton.setEnabled(false);
        } else {
            saveToArgonButton.setEnabled(true);
            URL url = currentEditor.getEditorLocation();
            if (URLUtils.isArgon(url)) {
                newVersionButton.setEnabled(true);
            } else {
                newVersionButton.setEnabled(false);
            }
            if (URLUtils.isQuery(currentEditor.getEditorLocation())) {
                runQueryButton.setEnabled(true);
            } else {
                runQueryButton.setEnabled(false);
            }
        }
    }


    private class ArgonAuthorPopupMenuCustomizer implements AuthorPopupMenuCustomizer {

        @Override
        public void customizePopUpMenu(Object popUp, AuthorAccess authorAccess) {
            String editorURLString = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA).getEditorLocation().toString();
            final String selectedText = authorAccess.getEditorAccess().getSelectedText();
            if ((selectedText != null) && (!selectedText.equals(""))) {
                JMenuItem editorSelectionMenu = createSnippetEditorPopUpAddition();
                ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
            }
            if (editorURLString.toLowerCase().startsWith("argon")) {
                if (authorAccess.getEditorAccess().isEditable()) {
                    JMenuItem editorSelectionMenu = createCheckInEditorPopUpAddition();
                    ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
                } else {
                    JMenuItem editorSelectionMenu = createCheckOutEditorPopUpAddition();
                    ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
                }
            }
        }
    }


    private class ArgonTextPopupMenuCustomizer extends TextPopupMenuCustomizer {

        @Override
        public void customizePopUpMenu(Object popUp, WSTextEditorPage textAccess) {
            final String selectedText = textAccess.getSelectedText();
            String editorURLString = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA).getEditorLocation().toString();
            if ((selectedText != null) && (!selectedText.equals(""))) {
                JMenuItem editorSelectionMenu = createSnippetEditorPopUpAddition();
                ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
            }
            if (editorURLString.toLowerCase().startsWith("argon")) {
                if (textAccess.isEditable()) {
                    JMenuItem editorSelectionMenu = createCheckInEditorPopUpAddition();
                    ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
                } else {
                    JMenuItem editorSelectionMenu = createCheckOutEditorPopUpAddition();
                    ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
                }
            }
        }
    }

}
