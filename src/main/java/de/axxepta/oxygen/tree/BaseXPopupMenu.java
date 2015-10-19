package de.axxepta.oxygen.tree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;

/**
 * PopupMenu class which holds extra ArrayLists for MenuItems and their names, providing access methods
 *  for enabling the items via name keys.
 */
public class BaseXPopupMenu extends ro.sync.exml.workspace.api.standalone.ui.PopupMenu {

    private ArrayList<String> itemNames;
    private ArrayList<JMenuItem> items;

    public BaseXPopupMenu() {
        super();
        this.itemNames = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public JMenuItem add(Action a, String name) {
        JMenuItem item = super.add(a);
        this.items.add(item);
        this.itemNames.add(name);
        return item;
    }

    public JMenuItem add(JMenuItem item, String name) {
        JMenuItem jItem = super.add(item);
        this.items.add(jItem);
        this.itemNames.add(name);
        return jItem;
    }

    @Override
    public JMenuItem add(String name) {
        JMenuItem item = super.add(name);
        this.itemNames.add(name);
        return item;
    }

    @Override
    public JMenuItem add(Action a) {
        JMenuItem item = super.add(a);
        this.itemNames.add("");
        return item;
    }

    @Override
    public JMenuItem add(JMenuItem item) {
        JMenuItem jItem = super.add(item);
        this.itemNames.add("");
        return jItem;
    }

    public void show(Component invoker, int x, int y, TreePath path){
        // set entries in menu to (un)visible outside of the class because used constants are not inherent
        TreeListener.prepareContextMenu(this, path);
        super.show(invoker, x, y);
    }

    public int getItemCount() {
        return this.items.size();
    }

    public String getItemName(int i) {
        if (this.itemNames.size() > i)
            return this.itemNames.get(i);
        else
            throw new NullPointerException("Tried to access BaseXPopupMenu item number not in list");
    }

    public void setItemEnabled(int i, boolean b) {
        if (this.itemNames.size() > i)
            items.get(i).setEnabled(b);
        else
            throw new NullPointerException("Tried to access BaseXPopupMenu item number not in list");
    }

    public void setItemEnabled(String name, boolean b) {
        if (this.itemNames.contains(name)) {
            items.get(this.itemNames.indexOf(name)).setEnabled(b);
        }
    }

}
