package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.WorkspaceUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 20.07.2016.
 */
public class OpenFileAction extends AbstractAction {

    private TreeListener treeListener;

    public OpenFileAction(String name, Icon icon, TreeListener treeListener) {
        super(name, icon);
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!treeListener.getNode().getAllowsChildren()) {
            String db_path = TreeUtils.urlStringFromTreePath(treeListener.getPath());
            WorkspaceUtils.openURLString(db_path);
        }
    }

}
