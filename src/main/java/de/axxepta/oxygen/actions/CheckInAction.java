package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLUtils;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static ro.sync.exml.workspace.api.PluginWorkspace.MAIN_EDITING_AREA;


/**
 * @author Markus on 07.06.2016
 */
public class CheckInAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckInAction.class);

    private final TreeListener treeListener;
    private URL urlString = null;

    public CheckInAction(String name, Icon icon, String urlString, TreeListener treeListener) {
        super(name, icon);

        try {
            this.urlString = urlString != null ? new URL(urlString) : null;
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        this.treeListener = treeListener;
    }

    public CheckInAction(String name, Icon icon, TreeListener treeListener) {
        this(name, icon, null, treeListener);
    }

    public CheckInAction(String name, Icon icon, String urlString) {
        this(name, icon, urlString, null);
    }

    public CheckInAction(String name, Icon icon) {
        this(name, icon, null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.urlString != null) {
            checkIn(this.urlString);
        } else if (treeListener == null) {
            final URL url = PluginWorkspaceProvider.getPluginWorkspace().
                    getCurrentEditorAccess(MAIN_EDITING_AREA).getEditorLocation();
            checkIn(url);
        } else if (!treeListener.getNode().getAllowsChildren()) {
            final String urlString = TreeUtils.urlStringFromTreePath(treeListener.getPath());
            try {
                checkIn(new URL(urlString));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    static void checkIn(URL url) {
        final BaseXSource source = CustomProtocolURLUtils.sourceFromURL(url);
        final String path = CustomProtocolURLUtils.pathFromURL(url);
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            if (connection.lockedByUser(source, path)) {
                ArgonEditorsWatchMap.getInstance().setAskedForCheckIn(url, true);
                if (false) {
                    final WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                            getEditorAccess(url, MAIN_EDITING_AREA);
                    if (editorAccess != null) {
                        editorAccess.close(true);
                    }
                }
                connection.unlock(source, path);
            }
        } catch (IOException ex) {
            logger.debug(ex);
        }
    }

}
