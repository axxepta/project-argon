package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.URLUtils;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author Markus on 04.12.2015.
 * This action should be called from a toolbar button only if an Argon protocol file is opened in the current editor
 * and initiates an update of the version (and revision) number and storage of the updated file to BaseX.
 * As SIDE EFFECT the document in the current editor is also updated.
 */
public class NewVersionButtonAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public NewVersionButtonAction(String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WSEditor editorAccess =
                pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        URL url = editorAccess.getEditorLocation();
        if (URLUtils.isXML(url) || URLUtils.isQuery(url)) {
            String fileType = URLUtils.isXML(url) ? VersionRevisionUpdater.XML : VersionRevisionUpdater.XQUERY;
            BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
            String protocol = CustomProtocolURLHandlerExtension.protocolFromSource(source);
            CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();
            if (handlerExtension.canCheckReadOnly(protocol) && !handlerExtension.isReadOnly(url)) {
                VersionRevisionUpdater updater = new VersionRevisionUpdater(editorAccess, fileType);
                updateFile(updater, source, url);
            } else {
                JOptionPane.showMessageDialog(null, "Couldn't update version of file\n" + url.toString() +
                        ".\n File is locked by other user.", "Update Version Message", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    protected static void updateFile(VersionRevisionUpdater updater, BaseXSource source, URL url) {
        byte[] outputArray = updater.updateVersion();
        long[] verRev = updater.getVersionAndRevision();

        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url, true, verRev)) {
            os.write(outputArray);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't write updated version of file\n" + url.toString(),
                    "Update Version Error", JOptionPane.PLAIN_MESSAGE);
        }
    }

}
