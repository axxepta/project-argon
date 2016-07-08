package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.ArgonTreeNode;
import de.axxepta.oxygen.tree.TreeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author Markus on 07.06.2016
 */
public class CheckInAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckInAction.class);

    private TreeListener treeListener;

    public CheckInAction(String name, Icon icon, TreeListener treeListener) {
        super(name, icon);
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            URL url = new URL(((ArgonTreeNode) treeListener.getPath().getLastPathComponent()).getTag().toString());
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
                String path = CustomProtocolURLHandlerExtension.pathFromURL(url);
                if (connection.lockedByUser(source, path)) {
                    WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                            getEditorAccess(url, StandalonePluginWorkspace.MAIN_EDITING_AREA);
                    if (editorAccess != null)
                        editorAccess.close(true);
                    connection.unlock(source, path);
                }
            } catch (IOException ex) {
                logger.debug(ex);
            }
        } catch (MalformedURLException mue) {
            logger.debug(mue);
        }

    }

}
