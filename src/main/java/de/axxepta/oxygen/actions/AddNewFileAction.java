package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 20.10.2015.
 */
public class AddNewFileAction extends AbstractAction {

    StandalonePluginWorkspace wsa;
    BasexTree tree;

    public AddNewFileAction(String name, Icon icon, StandalonePluginWorkspace wsa, BasexTree tree){
        super(name, icon);
        this.wsa = wsa;
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = ((TreeListener) tree.getTreeSelectionListeners()[0]).getPath();
        String db_path = TreeUtils.urlStringFromTreePath(path);
        if (((TreeListener) tree.getTreeSelectionListeners()[0]).getNode().getAllowsChildren()) {

            try {
                BaseXRequest request = new BaseXRequest("add", TreeUtils.sourceFromTreePath(path), db_path);
            } catch (Exception er) {
                er.printStackTrace();
            }
        }
    }

}
