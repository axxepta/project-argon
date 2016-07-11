package de.axxepta.oxygen.core;

import de.axxepta.oxygen.actions.SearchInPathAction;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.RestConnection;
import de.axxepta.oxygen.tree.ArgonPopupMenu;
import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.ArgonTreeNode;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.net.MalformedURLException;

/**
 * @author Markus on 02.06.2016.
 * The object instances returned by this factory might be exchanged by aspects from extending plugins to exchange/expand
 *  functionality. All instances of the respected classes that are used in Argon are (supposed to be) created here.
 */
public class ClassFactory {
    private static ClassFactory ourInstance = new ClassFactory();

    public static ClassFactory getInstance() {
        return ourInstance;
    }

    private ClassFactory() {
    }

    public DefaultMutableTreeNode getTreeNode(String obj) {
        return new ArgonTreeNode(obj);
    }

    public DefaultMutableTreeNode getTreeNode(String obj, String url) {
        return new ArgonTreeNode(obj, url);
    }

    public ArgonPopupMenu getTreePopupMenu(StandalonePluginWorkspace pluginWorkspace, ArgonTree tree, TreeModel treeModel) {
        return new ArgonPopupMenu(pluginWorkspace, tree, treeModel);
    }

    public Connection getRestConnection(String host, int port, String user, String password) throws MalformedURLException {
        return new RestConnection(host, port, user, password);
    }

    public Action getSearchInPathAction(String name, Icon icon, StandalonePluginWorkspace wsa, JTree tree) {
        return new SearchInPathAction(name, icon, wsa, tree);
    }
}
