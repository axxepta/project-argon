package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus on 07.07.2016.
 */
public class CheckOutAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckOutAction.class);

    private TreeListener treeListener;

    public CheckOutAction(String name, Icon icon, TreeListener treeListener) {
        super(name, icon);
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!treeListener.getNode().getAllowsChildren()) {
            String db_path = TreeUtils.urlStringFromTreePath(treeListener.getPath());
            checkOut(db_path);
        }
    }

    @SuppressWarnings("all")
    public static void checkOut(String db_path) {
        try {
            URL url = new URL(db_path);
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                connection.lock(CustomProtocolURLHandlerExtension.sourceFromURL(url),
                        CustomProtocolURLHandlerExtension.pathFromURL(url));
            } catch (IOException ex) {
                logger.debug(ex);
            }
            PluginWorkspaceProvider.getPluginWorkspace().open(url);
        } catch (MalformedURLException e1) {
            logger.error(e1);
        }
    }
}
