package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 24.10.2015.
 */
public class DeleteAction extends AbstractAction {

    private ArgonTree tree;
    private TreeModel treeModel;

    public DeleteAction(String name, Icon icon, ArgonTree tree){
        super(name, icon);
        this.tree = tree;
        this.treeModel = tree.getModel();
    }

    public DeleteAction(ArgonTree tree){
        super();
        this.tree = tree;
        this.treeModel = tree.getModel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath[] paths = tree.getSelectionPaths();
        PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
        if (paths != null) {
            for (TreePath path : paths) {
                BaseXSource source = TreeUtils.sourceFromTreePath(path);
                String db_path = TreeUtils.resourceFromTreePath(path);
                if ((source != null) && (!db_path.equals(""))) {

                    if (!ConnectionWrapper.pathContainsLockedResource(source, db_path)) {

                        int dialogResult = pluginWorkspace.showConfirmDialog(
                                Lang.get(Lang.Keys.dlg_delete),
                                Lang.get(Lang.Keys.lbl_delete) + "\n" + TreeUtils.urlStringFromTreePath(path),
                                new String[]{Lang.get(Lang.Keys.cm_yes), Lang.get(Lang.Keys.cm_all)},
                                new int[]{0, 1}, 0);
                        if (dialogResult == 0) {
                            if (TreeUtils.isDB(path)) {
                                try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                                        connection.drop(db_path);
                                        ((DefaultTreeModel) treeModel).removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
                                } catch (Exception er) {
                                    pluginWorkspace.showErrorMessage(Lang.get(Lang.Keys.warn_faileddeletedb) + " " + er.getMessage());
                                }
                            } else if (TreeUtils.isDir(path) || TreeUtils.isFile(path)) {
                                deleteFile(source, db_path, path);
                            }
                        }

                    } else {
                        pluginWorkspace.showInformationMessage(Lang.get(Lang.Keys.msg_checkpriordelete));
                    }
                }
            }
        }
    }

    private void deleteFile(BaseXSource source, String db_path, TreePath path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.delete(source, db_path);
            ((DefaultTreeModel) treeModel).removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
        } catch (Exception er) {
            PluginWorkspaceProvider.getPluginWorkspace().showInformationMessage(Lang.get(Lang.Keys.warn_faileddelete) +
                    " " + er.getMessage());
        }
    }

}
