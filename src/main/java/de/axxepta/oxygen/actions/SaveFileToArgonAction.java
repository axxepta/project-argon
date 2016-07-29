package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.ArgonChooserDialog;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * @author Markus on 28.07.2016.
 */
public class SaveFileToArgonAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(SaveFileToArgonAction.class);
    private StandalonePluginWorkspace workspaceAccess;

    public SaveFileToArgonAction(String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess) {
        super(name, icon);
        workspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArgonChooserDialog urlChooser = new ArgonChooserDialog((Frame)workspaceAccess.getParentFrame(),
                "Open File via BaseX Database Connection", ArgonChooserDialog.SAVE);
        URL[] url =  urlChooser.selectURLs();

        WSEditor editorAccess = workspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if (url != null) {
            byte[] content = getEditorContent(editorAccess);
            // ToDo: ask for overwrite!
            BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url[0]);
            String path = CustomProtocolURLHandlerExtension.pathFromURL(url[0]);
            if (!isLocked(source, path)) {
                try {
                    lock(source, path);
                    saveFile(content, source, url[0]);
                } catch (IOException ioe) {
                    workspaceAccess.showErrorMessage("Resource " + url[0].toString() +
                            " could not be stored to BaseX connection: " + ioe.getMessage());
                }
            } else {
                workspaceAccess.showInformationMessage("Resource " + url[0].toString() +
                        " already exists and is locked by another user");
            }
        }
    }

    public static boolean isLocked(BaseXSource source, String path) {
        boolean isLocked = false;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            if (connection.locked(source, path))
                isLocked = true;
        } catch (IOException ie) {
            isLocked = true;
            logger.debug("Querying LOCKED returned: ", ie.getMessage());
        }
        return isLocked;
    }

    private void lock(BaseXSource source, String path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(source, path);
        } catch (IOException ioe) {
            logger.error("Failed to lock resource " + path + " in " + source.toString() + ": " + ioe.getMessage());
        }
    }

    private byte[] getEditorContent(WSEditor editorAccess) {
        byte[] content;
        String pageID = editorAccess.getCurrentPageID();
        if (!pageID.equals(EditorPageConstants.PAGE_TEXT))
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
        WSTextEditorPage textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
        Document doc = textPage.getDocument();
        if (!pageID.equals(EditorPageConstants.PAGE_TEXT))
            editorAccess.changePage(pageID);
        try {
            content = doc.getText(0, doc.getLength() - 1).getBytes("UTF-8");
        } catch (BadLocationException | UnsupportedEncodingException ex) {
            content = new byte[0];
            logger.error(ex);
        }
        return content;
    }

    public static void saveFile(byte[] isByte, BaseXSource source, URL url) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url)) {
            os.write(isByte);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
