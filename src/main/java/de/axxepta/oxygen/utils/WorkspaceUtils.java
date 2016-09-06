package de.axxepta.oxygen.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

import javax.swing.text.Document;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Markus on 30.06.2016.
 */
public class WorkspaceUtils {

    private static final Logger logger = LogManager.getLogger(WorkspaceUtils.class);

    private static PluginWorkspace workspaceAccess = PluginWorkspaceProvider.getPluginWorkspace();

    public static Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    public static Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    private WorkspaceUtils(){}

    public static byte[] getEditorByteContent(WSEditor editorAccess) {
        Document doc = getDocumentFromEditor(editorAccess);
        return doc.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Document getDocumentFromEditor(WSEditor editorAccess) {
        boolean editorInAuthorMode = false;
        if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
            editorInAuthorMode = true;
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
        }
        WSTextEditorPage  textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
        Document doc = textPage.getDocument();
        if (editorInAuthorMode) {
            editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
        }
        return doc;
    }

    public static void openURLString(String urlString) {
        try {
            URL argonURL = new URL(urlString);
            setCursor(WAIT_CURSOR);
            PluginWorkspaceProvider.getPluginWorkspace().open(argonURL);
            setCursor(DEFAULT_CURSOR);
        } catch (MalformedURLException e1) {
            logger.error(e1);
        }
    }

    public static void setCursor(Cursor cursor) {
        Component oxygenFrame = (Frame)workspaceAccess.getParentFrame();
        oxygenFrame.setCursor(cursor);
    }

}
