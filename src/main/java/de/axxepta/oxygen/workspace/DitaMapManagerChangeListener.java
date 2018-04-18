package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.CheckOutAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.ditamap.DITAMapPopupMenuCustomizer;
import ro.sync.exml.workspace.api.editor.page.ditamap.WSDITAMapEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

/**
 * @author Markus on 05.11.2016.
 */
class DitaMapManagerChangeListener extends WSEditorChangeListener {


    private static final Logger logger = LogManager.getLogger(DitaMapManagerChangeListener.class);
    private StandalonePluginWorkspace pluginWorkspaceAccess;

    DitaMapManagerChangeListener(StandalonePluginWorkspace pluginWorkspace) {
        super();
        this.pluginWorkspaceAccess = pluginWorkspace;
    }

    @Override
    public void editorPageChanged(URL editorLocation) {
        customizeEditorPopupMenu();
    }

    @Override
    public void editorSelected(URL editorLocation) {
        customizeEditorPopupMenu();
        TopicHolder.changedEditorStatus.postMessage(VersionHistoryUpdater.checkVersionHistory(editorLocation));
    }

    @Override
    public void editorActivated(URL editorLocation) {
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

    private void customizeEditorPopupMenu() {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        if (editorAccess != null) {
            if (EditorPageConstants.PAGE_DITA_MAP.equals(editorAccess.getCurrentPageID())) {
                WSDITAMapEditorPage currentCustomizedDitaPageAccess;
                currentCustomizedDitaPageAccess = (WSDITAMapEditorPage) editorAccess.getCurrentPage();
                currentCustomizedDitaPageAccess.setPopUpMenuCustomizer(new ArgonDitaPopupMenuCustomizer(currentCustomizedDitaPageAccess));
            }
        }
    }

    private JMenuItem createCheckOutEditorPopUpAddition(String urlString) {
        return new JMenuItem(new CheckOutAction(Lang.get(Lang.Keys.cm_checkout), ImageUtils.getIcon(ImageUtils.BASEX), urlString));
    }

    private static String getAbsoluteURLString(String baseURL, String refName) throws IllegalArgumentException {
        String[] baseComponents = baseURL.split(":|/");
        String[] refComponents = refName.split("/");
        int refUp = 0;
        for (String refComponent : refComponents) {
            if (refComponent.equals(".."))
                refUp++;
        }
        if (refUp > (baseComponents.length - 2)) {
            throw new IllegalArgumentException("Cannot resolve DITAMap link URL.");
        }
        StringBuilder absoluteURL = (new StringBuilder(baseComponents[0])).append(":");
        for (int i = 1; i < baseComponents.length - 1 - refUp; i++)
            absoluteURL.append(baseComponents[i]).append("/");
        for (int i = refUp; i < refComponents.length; i++) {
            absoluteURL.append(refComponents[i]);
            if (i < (refComponents.length - 1))
                absoluteURL.append("/");
        }
        return absoluteURL.toString();
    }

    private class ArgonDitaPopupMenuCustomizer implements DITAMapPopupMenuCustomizer {

        private WSDITAMapEditorPage ditaMapEditorPage;

        ArgonDitaPopupMenuCustomizer(WSDITAMapEditorPage editorPage) {
            ditaMapEditorPage = editorPage;
        }

        @Override
        public void customizePopUpMenu(Object popUp, AuthorDocumentController authorDocumentController) {
            AuthorNode[] nodes = ditaMapEditorPage.getSelectedNodes(true);
            if (nodes.length > 0) {
                AuthorNode selectedNode = nodes[0];
                if (selectedNode instanceof AuthorElement) {
                    AttrValue attrValue = ((AuthorElement) selectedNode).getAttribute("href");
                    if (attrValue != null) {
                        String baseUrl = selectedNode.getXMLBaseURL().toString();
                        String refName = attrValue.getRawValue();
                        if (baseUrl.startsWith("argon") &&
                                (!refName.contains(":") || (refName.contains(":") && refName.startsWith("argon")))) {
                            try {
                                String urlString = getAbsoluteURLString(baseUrl, refName);
                                JMenuItem checkOutMenuItem = createCheckOutEditorPopUpAddition(urlString);
                                ((JPopupMenu) popUp).add(checkOutMenuItem, 0);
                            } catch (IllegalArgumentException iae) {
                                logger.debug("Cannot resolve DITAMap link URL.");
                            }
                        }
                    }
                }
            }
        }
    }
}
