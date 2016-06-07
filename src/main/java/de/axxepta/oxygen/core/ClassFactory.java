package de.axxepta.oxygen.core;

import javax.swing.tree.DefaultMutableTreeNode;

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
}
