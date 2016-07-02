package de.axxepta.oxygen.utils;

import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

import javax.swing.text.Document;
import java.nio.charset.StandardCharsets;

/**
 * @author Markus on 30.06.2016.
 */
public class WorkspaceUtils {

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

}
