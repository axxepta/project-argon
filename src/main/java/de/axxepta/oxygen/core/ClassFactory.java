package de.axxepta.oxygen.core;

import de.axxepta.oxygen.actions.SearchInPathAction;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.RestConnection;
import de.axxepta.oxygen.customprotocol.ArgonChooserListCellRenderer;
import de.axxepta.oxygen.tree.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.standalone.ui.TreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author Markus on 02.06.2016.
 * The object instances returned by this factory might be exchanged by aspects from extending plugins to exchange/expand
 * functionality. All instances of the respected classes that are used in Argon are (supposed to be) created here.
 */
public class ClassFactory {

    private static final Logger logger = LogManager.getLogger(ClassFactory.class);
    private static final ClassFactory ourInstance = new ClassFactory();

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

    public ArgonPopupMenu getTreePopupMenu(ArgonTree tree, TreeModel treeModel) {
        return new ArgonPopupMenu(tree, treeModel);
    }

    public Connection getRestConnection(String host, int port, String user, String password) throws MalformedURLException {
        return new RestConnection(host, port, user, password);
    }

    public Action getSearchInPathAction(String name, Icon icon, JTree tree) {
        return new SearchInPathAction(name, icon, tree);
    }

    public TreeCellRenderer getTreeCellRenderer() {
        return new ArgonTreeCellRenderer();
    }

    public ListCellRenderer getChooserListCellRenderer() {
        return new ArgonChooserListCellRenderer();
    }

    public ArgonTreeTransferHandler getTransferHandler(ArgonTree tree) {
        return new ArgonTreeTransferHandler(tree);
    }

}
