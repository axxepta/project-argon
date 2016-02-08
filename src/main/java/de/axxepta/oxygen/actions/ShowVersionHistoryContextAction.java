package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.VersionHistoryUpdater;
import de.axxepta.oxygen.workspace.ArgonWorkspaceAccessPluginExtension;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 28.01.2016.
 */
public class ShowVersionHistoryContextAction extends AbstractAction {

    final TreeListener treeListener;

    public ShowVersionHistoryContextAction(String name, Icon icon, TreeListener treeListener){
        super(name, icon);
        this.treeListener = treeListener;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = treeListener.getPath();
        String urlString = TreeUtils.urlStringFromTreePath(path);
        VersionHistoryUpdater.updateAndShow(urlString, path);
    }

}
