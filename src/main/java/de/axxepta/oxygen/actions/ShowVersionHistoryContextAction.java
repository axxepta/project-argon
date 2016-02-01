package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistory;
import de.axxepta.oxygen.workspace.ArgonWorkspaceAccessPluginExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 28.01.2016.
 */
public class ShowVersionHistoryContextAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(ShowVersionHistoryContextAction.class);
    private ArgonWorkspaceAccessPluginExtension pluginWorkspaceAccessExtension;

    final TreeListener treeListener;

    public ShowVersionHistoryContextAction(String name, Icon icon, TreeListener treeListener,
                                   ArgonWorkspaceAccessPluginExtension pluginWorkspaceAccessExtension){
        super(name, icon);

        this.treeListener = treeListener;
        this.pluginWorkspaceAccessExtension = pluginWorkspaceAccessExtension;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        String urlString = TreeUtils.urlStringFromTreePath(path);

        if (URLUtils.isXML(urlString) || URLUtils.isQuery(urlString)) {

            String resource = TreeUtils.resourceFromTreePath(path);
            String pathStr = obtainHistoryPath(resource, path);
            String fileName = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.lastIndexOf("."));
            String extension = urlString.substring(urlString.lastIndexOf("."));

            List<String> allVersions = obtainFileVersions(pathStr, fileName, extension);

            if (allVersions.size() > 0) {
                VersionHistory history = VersionHistory.getInstance();
                history.update(pathStr, allVersions, pluginWorkspaceAccessExtension);
            } else {
                JOptionPane.showMessageDialog(null, "For this file no entries were found in version control.",
                        "Version History", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    private String obtainHistoryPath(String resource, TreePath path) {
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
        return pathStr.toString();
    }

    private List<String> obtainFileVersions(String pathStr, String fileName, String extension) {
        List<String> allVersions;
        BaseXSource source = BaseXSource.DATABASE;
        try {
            allVersions = (new BaseXRequest("list", source, pathStr)).getResult();
        } catch (Exception er) {
            allVersions = new ArrayList<>();
            JOptionPane.showMessageDialog(null, "Failed to get resource list from BaseX.\n Check whether server is still running!",
                    "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
        }

        String filter = fileName + "_[0-9]{4}-[0-1][0-9]-[0-3][0-9]_[0-2][0-9]-[0-5][0-9]_v[0-9]+r[0-9]+" + extension;

        allVersions = allVersions.subList(0, allVersions.size() / 2);
        for (int i=allVersions.size()-1; i>=0; i--) {
            if (!allVersions.get(i).matches(filter))
                allVersions.remove(i);
        }
        return allVersions;
    }

}
