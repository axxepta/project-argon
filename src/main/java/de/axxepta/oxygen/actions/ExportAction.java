package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.BaseXType;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author Markus on 16.09.2016.
 */
public class ExportAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(ExportAction.class);

    private final TreeListener treeListener;
    private final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    public ExportAction(String name, Icon icon, TreeListener treeListener){
        super(name, icon);
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        BaseXSource source = TreeUtils.sourceFromTreePath(path);
        String db_path = TreeUtils.resourceFromTreePath(path);
        if ((source != null) && (!db_path.equals(""))) {
            try {
                List<BaseXResource> resourceList = ConnectionWrapper.listAll(source, db_path);
                // if complete database or special folder is to be exported, add empty resource to create folder
                if ((source.equals(BaseXSource.DATABASE) && (path.getPathCount() == 3)) ||
                        (!source.equals(BaseXSource.DATABASE) && (path.getPathCount() == 2))){
                    resourceList.add(0, new BaseXResource("", BaseXType.DIRECTORY, source));
                }
                File targetDirectory = workspace.chooseDirectory();
                if (targetDirectory != null) {
                    for (BaseXResource resource : resourceList) {
                        String fullResource = getFullResource(path, source, resource);
                        String relativePath = getRelativePath(source, db_path, fullResource);
                        String newFile = (targetDirectory.getAbsolutePath() + "\\" + relativePath).replace("/", "\\");
                        if (resource.getType().equals(BaseXType.DIRECTORY)) {
                            FileUtils.createDirectory(newFile);
                        } else {
                            FileUtils.copyFromBaseXToFile(CustomProtocolURLHandlerExtension.protocolFromSource(source) +
                                            "://" + fullResource, newFile);
                        }
                    }
                }
            } catch (IOException ioe) {
                logger.error("Failed to export resources from database", ioe.getMessage());
                workspace.showErrorMessage("Failed to export resources from database: " + ioe.getMessage());
            }
        }
    }

    private static String getFullResource(TreePath path, BaseXSource source, BaseXResource resource) {
        String fullResource;
        if (source.equals(BaseXSource.DATABASE)) {
            fullResource = path.getPathComponent(2) + "/" + resource.getName();
        } else {
            fullResource = resource.getName();
        }
        return fullResource;
    }

    private static String getRelativePath(BaseXSource source, String db_path, String fullResource) {
        if (!source.equals(BaseXSource.DATABASE))
            fullResource = CustomProtocolURLHandlerExtension.protocolFromSource(source) + "/" + fullResource;
        int resourceDepth = db_path.split("/").length;
        String[] resourceComponents = fullResource.split("/");
        StringJoiner joiner = new StringJoiner("/");
        for (int i = resourceDepth - 1; i < resourceComponents.length; i++) {
            joiner.add(resourceComponents[i]);
        }
        return joiner.toString();
    }

}
