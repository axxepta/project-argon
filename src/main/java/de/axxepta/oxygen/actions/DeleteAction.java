package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 24.10.2015.
 */
public class DeleteAction extends AbstractAction {

    BasexTree tree;
    DefaultTreeModel treeModel;
    TreeListener treeListener;

    public DeleteAction(String name, Icon icon, BasexTree tree, TreeListener treeListener){
        super(name, icon);
        this.tree = tree;
        this.treeModel = (DefaultTreeModel) tree.getModel();
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        BaseXSource source = TreeUtils.sourceFromTreePath(path);
        String db_path = TreeUtils.resourceFromTreePath(path);
        if ((source != null) && (!db_path.equals(""))) {
            // don't try to delete databases!
            if ((!(source == BaseXSource.DATABASE)) || (db_path.contains("/"))) {
                if (((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
                    if (tree.isCollapsed(path)) {
                        tree.expandPath(path);
                        tree.collapsePath(path);
                    } else {
                        tree.collapsePath(path);
                        tree.expandPath(path);
                    }
                }
                if (treeModel.getChildCount(path.getLastPathComponent()) == 0) {
                    try {
                        new BaseXRequest("delete", source, db_path);
                        treeModel.removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
                    } catch (Exception er) {
                        JOptionPane.showMessageDialog(null, "Failed to delete resource",
                                "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "You cannot delete non-empty directories!",
                            "BaseX Delete Warning", JOptionPane.PLAIN_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "You cannot delete databases!",
                        "BaseX Delete Warning", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }
}
