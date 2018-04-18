package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.api.BaseXSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Markus on 30.06.2016.
 */
public class WorkspaceUtils {

    @SuppressWarnings("all")   // keep public for access by AspectJ
    public static final Logger logger = LogManager.getLogger(WorkspaceUtils.class);

    private static PluginWorkspace workspaceAccess = PluginWorkspaceProvider.getPluginWorkspace();

    private static final int OVERWRITE_ALL = 2;
    private static final int OVERWRITE_YES = 1;
    private static final int OVERWRITE_ASK = 0;
    private static final int OVERWRITE_NO = -1;
    private static final int OVERWRITE_NONE = -2;

    private static JPanel treePanel;

    public static Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    public static Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    private WorkspaceUtils(){}

    public static byte[] getEditorByteContent(WSEditor editorAccess) {
        Document doc = getDocumentFromEditor(editorAccess);
        return doc.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getEditorContent(WSEditor editorAccess) throws IOException {
        byte[] content;
        try (InputStream contentStream = editorAccess.createContentInputStream()) {
            content = IOUtils.getBytesFromInputStream(contentStream);
        } catch (IOException ie) {
            logger.error(ie);
            content = new byte[0];
        }
        return content;
    }

    public static void setTreePanel(JPanel panel) {
        treePanel = panel;
    }

    /**
     * Extracts encoding from XML prologue in editor string content and the String content as second element,
     * returns empty string as first element if no prologue is found.
     * @param editorAccess editor handle
     * @return encoding encoding and editor content as String array, empty if no encoding could be extracted
     */
    private static String[] editorStringEncoding(WSEditor editorAccess) {
        String encodingString[] = new String[2];
        String pageID = editorAccess.getCurrentPageID();
        if (!pageID.equals(EditorPageConstants.PAGE_TEXT))
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
        WSTextEditorPage textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
        Document doc = textPage.getDocument();
        if (!pageID.equals(EditorPageConstants.PAGE_TEXT))
            editorAccess.changePage(pageID);
        try {
            encodingString[1] = doc.getText(0, doc.getLength());
            encodingString[0] = XMLUtils.encodingFromPrologue(encodingString[1]);
        } catch (BadLocationException ex) {
            logger.error(ex);
            encodingString[0] = "";
        }
        return encodingString;
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
        if (treePanel != null)
            treePanel.setCursor(cursor);
    }

    private static boolean checkOverwrite() {
        int save = workspaceAccess.showConfirmDialog(Lang.get(Lang.Keys.dlg_overwrite), Lang.get(Lang.Keys.lbl_overwrite),
                new String[] {Lang.get(Lang.Keys.cm_yes), Lang.get(Lang.Keys.cm_no)}, new int[] { OVERWRITE_YES, OVERWRITE_NO }, 0);
        return (save == OVERWRITE_YES);
    }

    private static int checkOverwriteAll() {
        return workspaceAccess.showConfirmDialog(Lang.get(Lang.Keys.dlg_overwrite), Lang.get(Lang.Keys.lbl_overwrite),
                new String[] {Lang.get(Lang.Keys.cm_yes), Lang.get(Lang.Keys.cm_always), Lang.get(Lang.Keys.cm_no), Lang.get(Lang.Keys.cm_never)},
                new int[] { OVERWRITE_YES, OVERWRITE_ALL, OVERWRITE_NO, OVERWRITE_NONE }, 2);
    }

    /**
     * Check for existence of a BaseX resource and show overwrite dialog if necessary
     * @param source source of storage target
     * @param path resource path of storage target
     * @return true if resource does not yet exist or user agreed to overwrite
     * @see OverwriteChecker
     */
    public static boolean newResourceOrOverwrite(BaseXSource source, String path) {
        boolean freeToSave = true;
        if (ConnectionWrapper.resourceExists(source, path)) {
            freeToSave = WorkspaceUtils.checkOverwrite();
        }
        return freeToSave;
    }

    /**
     * Store the content of an editor editorAccess to a BaseX resource url. Checks for encoding in prologue and byte code,
     * if none can be obtained assumes UTF-8 encoding.
     * @param editorAccess editor handle
     * @param url BaseX target url
     * @throws IOException BaseX connection can return exception
     */
    public static void saveEditorToBaseXURL(WSEditor editorAccess, URL url) throws IOException {
        byte[] content = WorkspaceUtils.getEditorContent(editorAccess);
        String[] encodingString = WorkspaceUtils.editorStringEncoding(editorAccess);
        if (encodingString[0].equals(""))
            encodingString[0] = XMLUtils.encodingFromBytes(content);
        if (!URLUtils.isXML(url) && (URLUtils.isBinary(url) || !IOUtils.isXML(content))) {
            ConnectionWrapper.save(true, url, content);
        } else {
            switch (encodingString[0]) {
                case "": {
                    ConnectionWrapper.save(url, IOUtils.returnUTF8Array(encodingString[1]), "UTF-8");
                    break;
                }
                case "UTF-8": {
                    ConnectionWrapper.save(url, content, "UTF-8");
                    break;
                }
                default:
                    ConnectionWrapper.save(url, IOUtils.convertToUTF8(content, encodingString[0]), encodingString[0]);
            }
        }
    }

    /**
     * The OverwriteChecker implements a way to check for multiple files to store for their existence and ask the user
     * whether existing files should be overwritten with "Always" and "Never" options.
     * <p>
     * For storing single files use the static method
     * {@link WorkspaceUtils#newResourceOrOverwrite(BaseXSource, String) newResourceOrOverwrite}
     */
    public static class OverwriteChecker {

        private int checkFlag;

        public OverwriteChecker() {
            checkFlag = OVERWRITE_ASK;
        }

        public boolean newResourceOrOverwrite(BaseXSource source, String path) {
            if (checkFlag == OVERWRITE_ALL)
                return true;
            if (ConnectionWrapper.resourceExists(source, path)) {
                if (checkFlag == OVERWRITE_NONE)
                    return false;
                int check;
                check = WorkspaceUtils.checkOverwriteAll();
                if ((check == OVERWRITE_ALL) || (check == OVERWRITE_NONE))
                    checkFlag = check;
                return ((check == OVERWRITE_YES) || (check == OVERWRITE_ALL));
            } else {
                return true;
            }

        }
    }

}
