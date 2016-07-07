package de.axxepta.oxygen.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Markus on 02.07.2016.
 */
public class ArgonTreeNode extends DefaultMutableTreeNode {

    private Object tag;

    public ArgonTreeNode() {
        super();
    }

    public ArgonTreeNode(Object name) {
        super(name);
    }

    public ArgonTreeNode(Object name, Object url) {
        super(name);
        this.tag = url;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

}
