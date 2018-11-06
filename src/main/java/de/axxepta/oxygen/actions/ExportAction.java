package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.BaseXType;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.FileUtils;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author Markus on 16.09.2016.
 */
public class ExportAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(ExportAction.class);

    private final TreeListener treeListener;
    private final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    public ExportAction(String name, Icon icon, TreeListener treeListener) {
        super(name, icon);
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        BaseXSource source = TreeUtils.sourceFromTreePath(path);
        String db_path = TreeUtils.resourceFromTreePath(path);
        if (source != null) {
            try {
                List<BaseXResource> resourceList = getExportResourceList(source, path, db_path);
                File targetDirectory = workspace.chooseDirectory();
                boolean createdDir = true;
                if (targetDirectory != null) {
                    for (BaseXResource resource : resourceList) {
                        if (resource.getType().equals(BaseXType.RESOURCE)) {
                            String fullResource = getFullResource(path, source, resource);
                            String relativePath = getRelativePath(source, db_path, fullResource);
                            String newFileName = (targetDirectory.getAbsolutePath() + "\\" + relativePath).replace("/", "\\");
                            File newFile = new File(newFileName);
                            if (!newFile.getParentFile().exists())
                                createdDir = newFile.getParentFile().mkdirs();
                            if (!fullResource.endsWith("/" + ArgonConst.EMPTY_FILE)) {
                                FileUtils.copyFromBaseXToFile(source.getProtocol() +
                                        ":" + fullResource, newFileName);
                            }
                        }
                    }
                    if (!createdDir)
                        logger.debug("One of the parent directories couldn't be created while exporting files from BaseX database.");
                }
            } catch (IOException ioe) {
                logger.error("Failed to export resources from database", ioe.getMessage());
                workspace.showErrorMessage(Lang.get(Lang.Keys.warn_failedexport) + " " + ioe.getMessage());
            }
        }
    }

    public static List<BaseXResource> getExportResourceList(BaseXSource source, TreePath path, String db_path) throws IOException {
        List<BaseXResource> resourceList;
        if (!TreeUtils.isFile(path)) {
            resourceList = ConnectionWrapper.listAll(source, db_path);
        } else {
            resourceList = new ArrayList<>();
            resourceList.add(new BaseXResource(getStrippedResourceFromPath(source, path),
                    BaseXType.RESOURCE, source));
        }
        return resourceList;
    }

    public static String getStrippedResourceFromPath(BaseXSource source, TreePath path) {
        StringJoiner joiner = new StringJoiner("/");
        int startIndex;
        if (source.equals(BaseXSource.DATABASE))
            startIndex = 3;
        else
            startIndex = 2;
        for (int i = startIndex; i < path.getPathCount(); i++) {
            joiner.add(path.getPathComponent(i).toString());
        }
        return joiner.toString();
    }

    /**
     * Builds resource name for BaseXResources obtained with ConnectionWrapper.listAll
     *
     * @param path     path in source
     * @param source   BaseX source of resource
     * @param resource resource of which the full name shall be obtained
     * @return full resource name
     */
    public static String getFullResource(TreePath path, BaseXSource source, BaseXResource resource) {
        String fullResource;
        if (source.equals(BaseXSource.DATABASE)) {
            fullResource = path.getPathComponent(2) + "/" + resource.getName();
        } else {
            fullResource = resource.getName().replace("\\", "/");
        }
        return fullResource;
    }

    public static String getRelativePath(BaseXSource source, String db_path, String fullResource) {
        int resourceDepth = db_path.split("/").length;
        String[] resourceComponents = fullResource.split("/");
        StringJoiner joiner = new StringJoiner("/");
        if (!source.equals(BaseXSource.DATABASE) && db_path.equals(""))
            joiner.add(source.getProtocol());
        for (int i = resourceDepth - 1; i < resourceComponents.length; i++) {
            joiner.add(resourceComponents[i]);
        }
        return joiner.toString();
    }

}
