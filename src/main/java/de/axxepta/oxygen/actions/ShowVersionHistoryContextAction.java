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
import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Markus on 28.01.2016.
 */
public class ShowVersionHistoryContextAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(ShowVersionHistoryContextAction.class);
    private StandalonePluginWorkspace pluginWorkspaceAccess;

    final TreeListener treeListener;

    public ShowVersionHistoryContextAction(String name, Icon icon, TreeListener treeListener,
                                   final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);

        this.treeListener = treeListener;
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        String urlString = TreeUtils.urlStringFromTreePath(path);

        if (URLUtils.isXML(urlString) || URLUtils.isQuery(urlString)) {

            BaseXSource source = BaseXSource.DATABASE;
            String resource = TreeUtils.resourceFromTreePath(path);
            StringBuilder pathStr;
            if (TreeUtils.isInDB(path)) {
                pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupDBBase);
            } else if (TreeUtils.isInRepo(path)) {
                pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRepoBase);
            } else {
                pathStr = new StringBuilder(BaseXByteArrayOutputStream.backupRESTXYBase);
            }
            if (resource.lastIndexOf("/") != -1)
                pathStr.append(resource.substring(0, resource.lastIndexOf("/")));
            else
                pathStr.append(resource.substring(0, resource.lastIndexOf("/")));
            JOptionPane.showMessageDialog(null, pathStr,
                    "Version History", JOptionPane.PLAIN_MESSAGE);

            String fileName = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.lastIndexOf("."));
            String extension = urlString.substring(urlString.lastIndexOf("."));

            String filter = fileName + "_([0-9]{4})-([0-1][0-9])-([0-3][0-9])_v([0-9]+)r([0-9]+)" + extension;
            ArrayList<String> allVersions = SearchInPathAction.searchResourcesInPathString(source,
                    pathStr.toString(), filter);

            JOptionPane.showMessageDialog(null, allVersions,
                    "Version History", JOptionPane.PLAIN_MESSAGE);

        }
    }

}
