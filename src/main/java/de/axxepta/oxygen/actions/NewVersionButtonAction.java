package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import de.axxepta.oxygen.workspace.ArgonWorkspaceAccessPluginExtension;
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
 * and initiates an update of the version (and revision) number and storage of the file to BaseX.
 */
public class NewVersionButtonAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public NewVersionButtonAction(String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        URL url = editorAccess.getEditorLocation();
        if (url.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON)) {
            BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
            String protocol = CustomProtocolURLHandlerExtension.protocolFromSource(source);
            CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();
            if (handlerExtension.canCheckReadOnly(protocol) && !handlerExtension.isReadOnly(url)) {
                byte[] outputArray = WorkspaceUtils.getEditorByteContent(editorAccess);
                updateFile(source, url, outputArray);
            } else {
                JOptionPane.showMessageDialog(null, "Couldn't update version of file\n" + url.toString() +
                        ".\n File is locked by other user.", "Update Version Message", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    static void updateFile(BaseXSource source, URL url, byte[] outputArray) {

        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url, true)) {
            os.write(outputArray);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't write updated version of file\n" + url.toString(),
                    "Update Version Error", JOptionPane.PLAIN_MESSAGE);
        }
        ArgonWorkspaceAccessPluginExtension.checkVersionHistory(url);
    }

}
