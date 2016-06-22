package de.axxepta.oxygen.core;

import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.RestConnection;

import javax.swing.tree.DefaultMutableTreeNode;
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
        return new DefaultMutableTreeNode(obj);
    }

    public Connection getRestConnection(String host, int port, String user, String password) throws MalformedURLException {
        return new RestConnection(host, port, user, password);
    }
}
