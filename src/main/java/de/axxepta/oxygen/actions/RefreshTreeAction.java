package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author Markus on 20.10.2015.
 */
public class RefreshTreeAction extends AbstractAction {

    BasexTree tree;
    DefaultTreeModel model;

    public RefreshTreeAction(String name, Icon icon, BasexTree tree) {
        super(name, icon);
        this.tree = tree;
        this.model = (DefaultTreeModel) tree.getModel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath rootPath = new TreePath(model.getRoot());
        DummyNode dummyTree = new DummyNode(rootPath.getPathComponent(0).toString(),
                tree.isExpanded(rootPath));
        buildDummyTree(dummyTree, rootPath);
        expandTree(dummyTree, rootPath);
    }

    private void buildDummyTree(DummyNode node, TreePath path) {
        int childCount = model.getChildCount(path.getLastPathComponent());
        for (int i=0; i<childCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(path.getLastPathComponent(), i);
            if (child.getAllowsChildren()) {
                TreePath newPath = path.pathByAddingChild(child);
                DummyNode newChild = new DummyNode(child.getUserObject().toString(),
                        tree.isExpanded(newPath));
                node.add(newChild);
                buildDummyTree(newChild, newPath);
            }
        }
    }

    private void expandTree(DummyNode node, TreePath path) {
        if (node.isExpanded()) {
            tree.collapsePath(path);
            tree.expandPath(path);
        } else {
            tree.expandPath(path);
            tree.collapsePath(path);
        }
        int childCount = model.getChildCount(path.getLastPathComponent());
        for (int i=0; i<childCount; i++) {
            if (TreeUtils.isNodeAsStrChild((DefaultMutableTreeNode) path.getLastPathComponent(),
                                            node.getChild(i).getName())) {
                TreePath childPath = TreeUtils.pathByAddingChildAsStr(path, node.getChild(i).getName());
                expandTree(node.getChild(i), childPath);
            }
        }
    }

    private class DummyNode {
        String name;
        ArrayList<DummyNode> children;
        boolean expanded;

        private DummyNode(String name, boolean expanded) {
            this.name = name;
            this.expanded = expanded;
            this.children = new ArrayList<>();
        }

        private boolean isExpanded(){
            return this.expanded;
        }

        private DummyNode getChild(int i) {
            return this.children.get(i);
        }

        private String getName() {
            return this.name;
        }

        private void add(DummyNode child) {
            this.children.add(child);
        }

        private int getChildCount(){
            return this.children.size();
        }

    }

}
