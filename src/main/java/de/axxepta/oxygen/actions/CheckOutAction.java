package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLUtils;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import static ro.sync.exml.workspace.api.PluginWorkspace.MAIN_EDITING_AREA;

/**
 * @author Markus on 07.07.2016.
 */
public class CheckOutAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckOutAction.class);

    private final TreeListener treeListener;
    private String urlString = null;

    public CheckOutAction(String name, Icon icon, String urlString, TreeListener treeListener) {
        super(name, icon);
        this.urlString = urlString;
        this.treeListener = treeListener;
    }

    public CheckOutAction(String name, Icon icon, TreeListener treeListener) {
        this(name, icon, null, treeListener);
//        super(name, icon);
//        this.treeListener = treeListener;
    }

    public CheckOutAction(String name, Icon icon) {
        this(name, icon, null, null);
//        super(name, icon);
//        treeListener = null;
    }

    public CheckOutAction(String name, Icon icon, String urlString) {
        this(name, icon, urlString, null);
//        super(name, icon);
//        treeListener = null;
//        this.urlString = urlString;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.urlString != null) {
            checkOut(this.urlString);
        } else if (treeListener == null) {
            final URL url = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(MAIN_EDITING_AREA).getEditorLocation();
            checkOut(url.toString());
        } else if (!treeListener.getNode().getAllowsChildren()) {
            final String urlString = TreeUtils.urlStringFromTreePath(treeListener.getPath());
            checkOut(urlString);
        }
    }

    //    @SuppressWarnings("all")
    public static void checkOut(String urlString) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(CustomProtocolURLUtils.sourceFromURLString(urlString),
                    CustomProtocolURLUtils.pathFromURLString(urlString));
            ArgonEditorsWatchMap.getInstance().addURL(new URL(urlString), true);
        } catch (IOException ex) {
            logger.debug(ex);
        }
        WorkspaceUtils.openURLString(urlString);
    }
}
