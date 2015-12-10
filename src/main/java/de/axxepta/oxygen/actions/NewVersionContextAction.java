package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.URLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus on 04.12.2015.
 * This action should be called from a context menu in the database tzee.
 * It initiates an update of the version (and revision) number and storage of the updated file to BaseX.
 * If the selected file is opened in an editor window (not necessarily the current one), the
 */
public class NewVersionContextAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(NewVersionContextAction.class);
    private StandalonePluginWorkspace pluginWorkspaceAccess;

    final TreeListener treeListener;

    public NewVersionContextAction(String name, Icon icon, TreeListener treeListener,
                                   final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);

        this.treeListener = treeListener;
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // ToDo: proper exception handling
        TreePath path = treeListener.getPath();
        BaseXSource source = TreeUtils.sourceFromTreePath(path);
        String urlString = TreeUtils.urlStringFromTreePath(path);
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e1) {
            logger.error(e1);
        }
        if (URLUtils.isXML(url) || URLUtils.isQuery(url)) {
            String fileType = URLUtils.isXML(url) ? "XML" : "XQUERY";
            String protocol = CustomProtocolURLHandlerExtension.protocolFromSource(source);
            CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();

            if (handlerExtension.canCheckReadOnly(protocol) && !handlerExtension.isReadOnly(url)) {
                // ToDo: lock, if not in editor
                VersionRevisionUpdater updater;
                boolean editorInAuthorMode = false;
                WSEditorPage editorPage = null;
                int currentAuthorOnset = 0;
                int currentAuthorOffset = 0;
                WSEditor editorAccess =
                        pluginWorkspaceAccess.getEditorAccess(url, StandalonePluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess == null) {     // get data from file
                    //ByteArrayInputStream inputStream;
                    byte[] isByte;
                    try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(connection.get(source,
                                CustomProtocolURLHandlerExtension.pathFromURL(url)))) {
                            int l = inputStream.available();
                            isByte = new byte[l];
                            //noinspection ResultOfMethodCallIgnored
                            inputStream.read(isByte);
                        } catch (IOException er) {
                            logger.error(er);
                            isByte = new byte[0];
                        }
                    } catch (IOException ex) {
                        logger.error(ex);
                        isByte = new byte[0];
                    }
                    updater = new VersionRevisionUpdater(isByte, fileType);
                } else {                        // get data from editor window

                    if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
                        editorInAuthorMode = true;
                        editorPage = editorAccess.getCurrentPage();
                        AuthorAccess authorAccess = ((WSAuthorEditorPage) editorPage).getAuthorAccess();
                        // store current cursor position for later reset
                        currentAuthorOnset = authorAccess.getEditorAccess().getSelectionStart();
                        currentAuthorOffset = authorAccess.getEditorAccess().getCaretOffset();
                        // change to Text mode
                        editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
                    }
                    WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
                    Document doc = textPage.getDocument();
                    updater = new VersionRevisionUpdater(doc, fileType);

                }

                byte[] outputArray = updater.updateVersion();

                try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url, true)) {
                    os.write(outputArray);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Couldn't write updated version of file\n" + url.toString(),
                            "Update Version Error", JOptionPane.PLAIN_MESSAGE);
                }
                // [write changed document back into editor window if open] executed in updater
                // if necessary, change back to previously selected editor window
                //  -- no setCurrentEditor method available

                if (editorInAuthorMode) {
                    editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
                    ((WSAuthorEditorPage) editorPage).select(currentAuthorOnset, currentAuthorOffset);
                }
                // ToDo: unlock, if not in editor
            }
        }
    }

}
