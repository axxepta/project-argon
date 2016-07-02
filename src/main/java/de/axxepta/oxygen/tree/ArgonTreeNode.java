package de.axxepta.oxygen.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Markus on 02.07.2016.
 */
public class ArgonTreeNode extends DefaultMutableTreeNode {

    private String url;

    public ArgonTreeNode(Object name) {
        super(name);
    }

    public ArgonTreeNode(Object name, String url) {
        super(name);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
