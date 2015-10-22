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
 * @ author Markus on 20.10.2015.
 */
public class RefreshTreeAction extends AbstractAction {

    BasexTree tree;

    public RefreshTreeAction(String name, Icon icon, BasexTree tree) {
        super(name, icon);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DefaultTreeModel treeModel = (DefaultTreeModel) this.tree.getModel();
        TreePath rootPath = new TreePath(treeModel.getRoot());
        DummyNode dummyTree = new DummyNode(rootPath.getPathComponent(0).toString(),
                this.tree.isExpanded(rootPath));
        buildDummyTree(dummyTree, this.tree, treeModel, rootPath);
        //((DefaultTreeModel) this.tree.getModel()).reload();
        expandTree(this.tree, dummyTree, rootPath);
    }

    private void buildDummyTree(DummyNode node, JTree tree, DefaultTreeModel model,
                                TreePath path) {
        int childrenCount = model.getChildCount(path.getLastPathComponent());
        for (int i=0; i<childrenCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(path.getLastPathComponent(), i);
            TreePath newPath = path.pathByAddingChild(model.getChild(child, i));
            DummyNode newChild = new DummyNode(child.getUserObject().toString(),
                    tree.isExpanded(newPath));
            node.add(newChild);
            if (child.getAllowsChildren())
                buildDummyTree(node, tree, model, newPath);
        }
    }

    private void expandTree(JTree tree, DummyNode node, TreePath path) {
        if (node.isExpanded()) {
            tree.expandPath(path);
        }
        for (int i=0; i<node.getChildCount(); i++) {
            TreePath childPath = TreeUtils.pathByAddingChildAsStr(path, node.getName());
            if (childPath !=null)
                expandTree(tree, node, childPath);
        }

    }

    private class DummyNode {
        String name;
        ArrayList<DummyNode> children;
        boolean expanded;

        private DummyNode(String name, boolean expanded) {
            this.name = name;
            this.expanded = expanded;
            children = new ArrayList<>();
        }

        private boolean isExpanded(){
            return this.expanded;
        }

        private String getName() {
            return this.name;
        }

        private void add(DummyNode child) {
            children.add(child);
        }

        private int getChildCount(){
            return children.size();
        }

    }

}
