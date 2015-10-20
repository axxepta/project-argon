package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.BasexTree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.event.ActionEvent;

/**
 * Created by Markus on 20.10.2015.
 */
public class RefreshTreeAction extends AbstractAction {

    BasexTree tree;

    public RefreshTreeAction(String name, Icon icon, BasexTree tree) {
        super(name, icon);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((DefaultTreeModel) this.tree.getModel()).reload();
    }

}
