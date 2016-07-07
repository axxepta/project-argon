package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.List;

public class TreePane extends JScrollPane {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public TreePane(StandalonePluginWorkspace pluginWorkspaceAccess) {
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
        initView();
    }

    private void initView() {
        // Create some data to populate our tree.
        DefaultMutableTreeNode root = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_root));
        root.setAllowsChildren(true);
        DefaultMutableTreeNode databases = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_DB),
                CustomProtocolURLHandlerExtension.ARGON + "://");
        databases.setAllowsChildren(true);
        root.add(databases);
        DefaultMutableTreeNode queryFolder = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_restxq),
                CustomProtocolURLHandlerExtension.ARGON_XQ + ":/");
        queryFolder.setAllowsChildren(true);
        root.add(queryFolder);
        DefaultMutableTreeNode repoFolder = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_repo),
                CustomProtocolURLHandlerExtension.ARGON_REPO + ":/");
        queryFolder.setAllowsChildren(true);
        root.add(repoFolder);

        List<BaseXResource> databaseList;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            databaseList = connection.list(BaseXSource.DATABASE, "");
        } catch (Exception er) {
//                    JOptionPane.showMessageDialog(null, "Couldn't read list of databases. Check whether BaseX server is running."
//                            , "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
            databaseList = new ArrayList<>();
        }
        for (BaseXResource database : databaseList) {
            DefaultMutableTreeNode dbNode = ClassFactory.getInstance().getTreeNode(database.getName(),
                    CustomProtocolURLHandlerExtension.ARGON + "://" + database.getName());
            dbNode.setAllowsChildren(true);
            databases.add(dbNode);
        }

        // Create a new tree control
        // explicit tree model necessary to use allowsChildren for definition of leafs
        final TreeModel treeModel = new DefaultTreeModel(root);
        ((DefaultTreeModel) treeModel).setAsksAllowsChildren(true);
        TreeUtils.init(treeModel);
        final ArgonTree tree = new ArgonTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setTreeState(tree, new TreePath(root), false);

        final TreeListener tListener;
        // Add context menu
        final ArgonPopupMenu contextMenu = ClassFactory.getInstance().getTreePopupMenu(pluginWorkspaceAccess, tree, treeModel);

        // Add Tree Listener
        tListener = new TreeListener(tree, treeModel, contextMenu, pluginWorkspaceAccess);
        tree.addTreeWillExpandListener(tListener);
        tree.addMouseListener(tListener);
        tree.addTreeSelectionListener(tListener);
        tree.addKeyListener(tListener);
        TopicHolder.saveFile.register(tListener);
        TopicHolder.deleteFile.register(tListener);

        contextMenu.init(tListener);

        // Add transfer handler for DnD
        tree.setTransferHandler(new ArgonTreeTransferHandler());
        tree.setDropMode(DropMode.ON);

        tree.add(contextMenu);
        this.getViewport().add(tree);
    }

    private static void setTreeState(JTree tree, TreePath path, boolean expanded) {
        Object lastNode = path.getLastPathComponent();
        for (int i = 0; i < tree.getModel().getChildCount(lastNode); i++) {
            Object child = tree.getModel().getChild(lastNode,i);
            TreePath pathToChild = path.pathByAddingChild(child);
            setTreeState(tree,pathToChild,expanded);
        }
        if (expanded)
            tree.expandPath(path);
        else
            tree.collapsePath(path);
    }

}
