package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.StoreSnippetSelectionAction;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.TextPopupMenuCustomizer;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;


class ArgonEditorChangeListener extends WSEditorChangeListener {

    private static final Logger logger = LogManager.getLogger(ArgonEditorChangeListener.class);

    private StandalonePluginWorkspace pluginWorkspaceAccess;
    private final ArgonToolbarComponentCustomizer toolbarCustomizer;

    private final Action snippetAction = new StoreSnippetSelectionAction();

    private WSAuthorEditorPage currentCustomizedAuthorPageAccess;
    private WSTextEditorPage currentCustomizedTextPageAccess;
    private AuthorPopupMenuCustomizer authorPopupMenuCustomizer;
    private TextPopupMenuCustomizer textPopupMenuCustomizer;

    ArgonEditorChangeListener(StandalonePluginWorkspace pluginWorkspace, final ArgonToolbarComponentCustomizer toolbarCustomizer) {
        super();
        this.pluginWorkspaceAccess = pluginWorkspace;
        this.toolbarCustomizer = toolbarCustomizer;
    }

    @Override
    public void editorPageChanged(URL editorLocation) {
        customizeEditorPopupMenu();
        toolbarCustomizer.checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
    }

    @Override
    public void editorSelected(URL editorLocation) {
        toolbarCustomizer.checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
        customizeEditorPopupMenu();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

    @Override
    public void editorActivated(URL editorLocation) {
        toolbarCustomizer.checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
        customizeEditorPopupMenu();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

    @Override
    public void editorClosed(URL editorLocation) {
        if (editorLocation.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON)) {
            ArgonEditorsWatchMap.removeURL(editorLocation);
            //toolbarCustomizer.checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(editorLocation);
                String path = CustomProtocolURLHandlerExtension.pathFromURL(editorLocation);
                if (connection.lockedByUser(source, path)) {
                    int checkInFile = JOptionPane.showConfirmDialog(null, "You just closed a checked out file.\n" +
                            "Do you want to check it in?", "Closed checked out file", JOptionPane.YES_NO_OPTION);
                    if (checkInFile == JOptionPane.YES_OPTION) {
                        connection.unlock(source, path);
                    }
                }
            } catch (IOException ioe) {
                logger.debug(ioe.getMessage());
            }
        }
    }

    @Override
    public void editorOpened(URL editorLocation) {
        logger.debug("editor opened: " + editorLocation.toString());
        if (editorLocation.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON))
            ArgonEditorsWatchMap.addURL(editorLocation);
        toolbarCustomizer.checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
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

    private JMenuItem createEditorPopUpAddition() {
        final JMenuItem storeSnippetItem = new JMenuItem(snippetAction);
        storeSnippetItem.setText("Store selected Snippet");
        return storeSnippetItem;
    }


    private class ArgonAuthorPopupMenuCustomizer implements AuthorPopupMenuCustomizer {

        @Override
        public void customizePopUpMenu(Object popUp, AuthorAccess authorAccess) {
            final String selectedText = authorAccess.getEditorAccess().getSelectedText();
            if ((selectedText != null) && (!selectedText.equals(""))) {
                JMenuItem editorSelectionMenu = createEditorPopUpAddition();
                ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
            }
        }
    }


    private class ArgonTextPopupMenuCustomizer extends TextPopupMenuCustomizer {

        @Override
        public void customizePopUpMenu(Object popUp, WSTextEditorPage textAccess) {
            final String selectedText = textAccess.getSelectedText();
            if ((selectedText != null) && (!selectedText.equals(""))) {
                JMenuItem editorSelectionMenu = createEditorPopUpAddition();
                ((JPopupMenu) popUp).add(editorSelectionMenu, 0);
            }
        }
    }

}
