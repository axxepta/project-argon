package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.actions.SearchInPathAction;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class TreePane extends JPanel {

    private StandalonePluginWorkspace pluginWorkspaceAccess;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;

    public TreePane(StandalonePluginWorkspace pluginWorkspaceAccess) {
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
        initView();
    }

    private void initView() {
        this.setLayout(new BorderLayout());

        final ArgonTree tree = initTree();
        JScrollPane treePane = new JScrollPane(tree);

        JPanel filterPanel = createFilterPanel();

        this.add(filterPanel, BorderLayout.PAGE_START);
        this.add(treePane, BorderLayout.CENTER);
    }

    private ArgonTree initTree() {
        // Create some data to populate our tree.
        root = getArgonBaseNodes();

        // Create a new tree control
        // explicit tree model necessary to use allowsChildren for definition of leafs
        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        treeModel.setAsksAllowsChildren(true);
        TreeUtils.init(treeModel);
        ArgonTree tree = new ArgonTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setTreeState(tree, new TreePath(root), false);
        this.model = treeModel;

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

        return tree;
    }

    private static DefaultMutableTreeNode getArgonBaseNodes() {
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
        return root;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JTextField filterTextField = new JTextField();
        filterTextField.setColumns(15);
        AbstractAction searchAction = new SearchAction("", ImageUtils.getIcon(ImageUtils.SEARCH), model, filterTextField);
        JButton searchButton = new JButton(searchAction);
        AbstractAction resetAction = new ResetAction("", ImageUtils.getIcon(ImageUtils.REMOVE), model, filterTextField);
        JButton clearButton = new JButton(resetAction);
        panel.add(filterTextField);
        panel.add(searchButton);
        panel.add(clearButton);
        return panel;
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

    private class SearchAction extends AbstractAction {

        private JTextField filterField;
        private TreePath rootPath;
        private DefaultTreeModel model;

        SearchAction(String name, Icon icon, DefaultTreeModel model, JTextField filterField) {
            super(name, icon);
            this.filterField = filterField;
            this.model = model;
            rootPath = new TreePath(root.getPath());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> resourceList = SearchInPathAction.search(rootPath, SearchInPathAction.SEARCH_ALL,
                    BaseXSource.DATABASE, null, filterField.getText());
            DefaultMutableTreeNode newRoot = getFilteredTree(resourceList);
            model.setRoot(newRoot);
        }

        private DefaultMutableTreeNode getFilteredTree(ArrayList<String> resources) {
            DefaultMutableTreeNode newRoot = TreePane.getArgonBaseNodes();
            return newRoot;
        }
    }

    private class ResetAction extends AbstractAction {

        private JTextField filterField;
        private DefaultTreeModel model;

        ResetAction(String name, Icon icon, DefaultTreeModel model, JTextField filterField) {
            super(name, icon);
            this.model = model;
            this.filterField = filterField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.setRoot(root);
            filterField.setText("");
        }
    }

}
