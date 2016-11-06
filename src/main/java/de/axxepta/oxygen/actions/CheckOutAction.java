package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

/**
 * @author Markus on 07.07.2016.
 */
public class CheckOutAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckOutAction.class);

    private TreeListener treeListener;
    private String urlString = null;

    public CheckOutAction(String name, Icon icon, TreeListener treeListener) {
        super(name, icon);
        this.treeListener = treeListener;
    }

    public CheckOutAction(String name, Icon icon) {
        super(name, icon);
        treeListener = null;
    }

    public CheckOutAction(String name, Icon icon, String urlString) {
        super(name, icon);
        treeListener = null;
        this.urlString = urlString;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.urlString != null) {
            checkOut(this.urlString);
        } else if (treeListener == null) {
            URL url = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA).getEditorLocation();
            checkOut(url.toString());
        } else {
            if (!treeListener.getNode().getAllowsChildren()) {
                String urlString = TreeUtils.urlStringFromTreePath(treeListener.getPath());
                checkOut(urlString);
            }
        }
    }

    @SuppressWarnings("all")
    public static void checkOut(String urlString) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(CustomProtocolURLHandlerExtension.sourceFromURLString(urlString),
                    CustomProtocolURLHandlerExtension.pathFromURLString(urlString));
            ArgonEditorsWatchMap.getInstance().addURL(new URL(urlString), true);
        } catch (IOException ex) {
            logger.debug(ex);
        }
        WorkspaceUtils.openURLString(urlString);
    }
}
