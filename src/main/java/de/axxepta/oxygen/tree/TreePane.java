package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.actions.SearchInPathAction;
import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TreePane extends JPanel {

    private DefaultMutableTreeNode root;
    private ArgonTree tree;
    private TreeListener tListener;
    private DefaultTreeModel treeModel;

    public TreePane() {
        initView();
        WorkspaceUtils.setTreePanel(this);
    }

    private void initView() {
        this.setLayout(new BorderLayout());
        JScrollPane treePane = new JScrollPane(initTree());
        JPanel filterPanel = createFilterPanel();
        this.add(filterPanel, BorderLayout.PAGE_START);
        this.add(treePane, BorderLayout.CENTER);
    }

    private ArgonTree initTree() {
        // Create some data to populate our tree.
        root = getArgonBaseNodes();

        // Create a new tree control
        // explicit tree model necessary to use allowsChildren for definition of leafs
        treeModel = new DefaultTreeModel(root);
        treeModel.setAsksAllowsChildren(true);
        TreeUtils.init(treeModel);
        tree = new ArgonTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setTreeState(tree, new TreePath(root), false);

        final ArgonPopupMenu contextMenu = ClassFactory.getInstance().getTreePopupMenu(tree, treeModel);

        tListener = new TreeListener(tree, treeModel, contextMenu);
        tree.addTreeWillExpandListener(tListener);
        tree.addMouseListener(tListener);
        tree.addTreeSelectionListener(tListener);
        tree.addKeyListener(tListener);
        TopicHolder.saveFile.register(tListener);
        TopicHolder.newDir.register(tListener);
        TopicHolder.deleteFile.register(tListener);

        contextMenu.init(tListener);

        tree.setTransferHandler(ClassFactory.getInstance().getTransferHandler(tree));
        tree.setDropMode(DropMode.ON);
        tree.setDragEnabled(true);

        tree.add(contextMenu);

        return tree;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JTextField filterTextField = new JTextField();
        filterTextField.setColumns(15);
        AbstractAction searchAction = new SearchAction("", ImageUtils.getIcon(ImageUtils.SEARCH), treeModel, tree,
                tListener, filterTextField);
        JButton searchButton = new JButton(searchAction);
        AbstractAction resetAction = new ResetAction("", ImageUtils.getIcon(ImageUtils.REMOVE), treeModel, tree,
                tListener, filterTextField);
        JButton clearButton = new JButton(resetAction);

        Action fieldConfirmed = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filterTextField.getText().equals("")) {
                    resetAction.actionPerformed(null);
                } else {
                    searchAction.actionPerformed(null);
                }
            }
        };

        filterTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        filterTextField.getActionMap().put("confirm", fieldConfirmed);

        panel.add(filterTextField);
        panel.add(searchButton);
        panel.add(clearButton);
        return panel;
    }

    private static DefaultMutableTreeNode getArgonBaseNodes() {
        DefaultMutableTreeNode root = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_root));
        root.setAllowsChildren(true);
        DefaultMutableTreeNode databases = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_DB),
                ArgonConst.ARGON + ":");
        databases.setAllowsChildren(true);
        root.add(databases);
//        DefaultMutableTreeNode queryFolder = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_restxq),
//                ArgonConst.ARGON_XQ + ":");
//        queryFolder.setAllowsChildren(true);
//        queryFolder.setAllowsChildren(true);
//        root.add(queryFolder);
//        DefaultMutableTreeNode repoFolder = ClassFactory.getInstance().getTreeNode(Lang.get(Lang.Keys.tree_repo),
//                ArgonConst.ARGON_REPO + ":");
//        root.add(repoFolder);
        return root;
    }

    private static void setTreeState(JTree tree, TreePath path, boolean expanded) {
        Object lastNode = path.getLastPathComponent();
        for (int i = 0; i < tree.getModel().getChildCount(lastNode); i++) {
            Object child = tree.getModel().getChild(lastNode, i);
            TreePath pathToChild = path.pathByAddingChild(child);
            setTreeState(tree, pathToChild, expanded);
        }
        if (expanded) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }

    private class SearchAction extends AbstractAction {

        private final JTextField filterField;
        private final TreePath rootPath;
        private final DefaultTreeModel model;
        private final ArgonTree tree;
        private final TreeListener treeListener;
        private DefaultMutableTreeNode newRoot;

        SearchAction(String name, Icon icon, DefaultTreeModel model, ArgonTree tree, TreeListener treeListener,
                     JTextField filterField) {
            super(name, icon);
            this.filterField = filterField;
            this.model = model;
            this.tree = tree;
            this.treeListener = treeListener;
            rootPath = new TreePath(root.getPath());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> resourceList = SearchInPathAction.search(rootPath, SearchInPathAction.SEARCH_ALL, true,
                    BaseXSource.DATABASE, null, filterField.getText(), false);
            buildFilteredTree(resourceList);
            model.setRoot(newRoot);
            tree.removeTreeWillExpandListener(treeListener);
            treeListener.setDoubleClickExpand(false);
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        }

        private void buildFilteredTree(ArrayList<String> resources) {
            newRoot = TreePane.getArgonBaseNodes();
            addNodes(resources);
        }

        private void addNodes(ArrayList<String> resources) {
            for (String resource : resources) {
                String[] levels = resource.split("/+");
                DefaultMutableTreeNode branch = newRoot;
                int depth = levels.length;
                for (int i = 1; i < depth; i++) {
                    int childIndex = TreeUtils.isNodeAsStrChild(branch, levels[i]);
                    if (childIndex == -1) {
                        branch = TreeUtils.insertStrAsNodeLexi(levels[i], branch, i == (depth - 1));
                    } else {
                        branch = (DefaultMutableTreeNode) branch.getChildAt(childIndex);
                    }
                }
            }
        }
    }

    private class ResetAction extends AbstractAction {

        private final JTextField filterField;
        private final DefaultTreeModel model;
        private final ArgonTree tree;
        private final TreeListener treeListener;

        ResetAction(String name, Icon icon, DefaultTreeModel model, ArgonTree tree, TreeListener treeListener,
                    JTextField filterField) {
            super(name, icon);
            this.model = model;
            this.tree = tree;
            this.treeListener = treeListener;
            this.filterField = filterField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            filterField.setText("");
            if (model.getRoot() != root) {
                model.setRoot(root);
                tree.addTreeWillExpandListener(treeListener);
                treeListener.setDoubleClickExpand(true);
            }
        }
    }

}
