package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.BaseXType;
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
                File targetDirectory = workspace.chooseDirectory();
                if (targetDirectory != null) {
                    for (BaseXResource resource : resourceList) {
                        if (resource.getType().equals(BaseXType.DIRECTORY)) {
                            String newDirectory = (targetDirectory.getAbsolutePath() + "/" + resource.getName()).replace("/", "\\");
                            FileUtils.createDirectory(newDirectory);
                        } else {
                            FileUtils.copyFromBaseXToFile(resource.getURLString(),
                                    (targetDirectory.getAbsolutePath() + "/" + resource.getName()).replace("/", "\\"));
                        }
                    }
                }
            } catch (IOException ioe) {
                logger.error("Failed to export resources from database", ioe.getMessage());
                workspace.showErrorMessage("Failed to export resources from database: " + ioe.getMessage());
            }
        }
    }

}
