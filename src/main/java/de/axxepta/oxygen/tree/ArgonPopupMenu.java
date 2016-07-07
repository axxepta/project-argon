package de.axxepta.oxygen.tree;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.axxepta.oxygen.actions.*;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.PopupMenu;

/**
 * PopupMenu class which holds extra ArrayLists for MenuItems and their names, providing access methods
 *  for enabling the items via name keys.
 */
@SuppressWarnings("all")  // CAVE: keep access modifiers public because class is subject to modification by AspectJ
public class ArgonPopupMenu extends PopupMenu {

    private static final Logger logger = LogManager.getLogger(ArgonPopupMenu.class);

    private StandalonePluginWorkspace pluginWorkspaceAccess;
    private ArgonTree tree;
    private final TreeModel treeModel;

    private ArrayList<String> itemNames;
    private ArrayList<JMenuItem> items;

    public ArgonPopupMenu(final StandalonePluginWorkspace pluginWorkspaceAccess, final ArgonTree tree, final TreeModel treeModel) {
        super();
        this.itemNames = new ArrayList<>();
        this.items = new ArrayList<>();
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
        this.tree = tree;
        this.treeModel = treeModel;
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
        prepareContextMenu(path);
        super.show(invoker, x, y);
    }

    public int getItemCount() {
        return this.items.size();
    }

    public String getItemName(int i) {
        if (this.itemNames.size() > i)
            return this.itemNames.get(i);
        else
            throw new NullPointerException("Tried to access ArgonPopupMenu item number not in list");
    }

    public void setItemEnabled(int i, boolean b) {
        if (this.itemNames.size() > i)
            items.get(i).setEnabled(b);
        else
            throw new NullPointerException("Tried to access ArgonPopupMenu item number not in list");
    }

/*    public void setItemEnabled(String name, boolean b) {
        if (this.itemNames.contains(name)) {
            items.get(this.itemNames.indexOf(name)).setEnabled(b);
        }
    }*/

    public void init(final TreeListener tListener) {
        // Populate context menu
        Action checkOut = new AbstractAction(Lang.get(Lang.Keys.cm_checkout), ImageUtils.getIcon(ImageUtils.URL_OPEN)) {
            public void actionPerformed(ActionEvent e) {
                String db_path = TreeUtils.urlStringFromTreePath(tListener.getPath());
                if (!tListener.getNode().getAllowsChildren()) {
                    URL argonURL = null;
                    try {
                        argonURL = new URL(db_path);
                    } catch (MalformedURLException e1) {
                        logger.error(e1);
                    }
                    pluginWorkspaceAccess.open(argonURL);
                }
            }
        };
        this.add(checkOut, Lang.get(Lang.Keys.cm_checkout));

        Action checkIn = new AbstractAction(Lang.get(Lang.Keys.cm_checkin), ImageUtils.getIcon(ImageUtils.FILE_ADD)) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        add(checkIn, Lang.get(Lang.Keys.cm_checkin));

        this.addSeparator();

        Action newDatabase = new AddDatabaseAction(Lang.get(Lang.Keys.cm_adddb), ImageUtils.getIcon(ImageUtils.DB_ADD),
                treeModel, tListener);
        this.add(newDatabase, Lang.get(Lang.Keys.cm_adddb));

        Action delete = new DeleteAction(Lang.get(Lang.Keys.cm_delete), ImageUtils.getIcon(ImageUtils.REMOVE), tree);
        this.add(delete, Lang.get(Lang.Keys.cm_delete));

        Action rename = new RenameAction(Lang.get(Lang.Keys.cm_rename), ImageUtils.getIcon(ImageUtils.RENAME),
                tree, tListener);
        this.add(rename, Lang.get(Lang.Keys.cm_rename));

        Action newVersion = new NewVersionContextAction(Lang.get(Lang.Keys.cm_newversion), ImageUtils.getIcon(ImageUtils.INC_VER),
                tListener, pluginWorkspaceAccess);
        this.add(newVersion, Lang.get(Lang.Keys.cm_newversion));

        Action add = new AddNewFileAction(Lang.get(Lang.Keys.cm_add), ImageUtils.getIcon(ImageUtils.FILE_ADD),
                tree);
        this.add(add, Lang.get(Lang.Keys.cm_add));

        final Action refresh = new RefreshTreeAction(Lang.get(Lang.Keys.cm_refresh), ImageUtils.getIcon(ImageUtils.REFRESH), tree);
        this.add(refresh, Lang.get(Lang.Keys.cm_refresh));

        this.addSeparator();

        final Action searchInPath = new SearchInPathAction(Lang.get(Lang.Keys.cm_search), ImageUtils.getIcon(ImageUtils.SEARCH),
                pluginWorkspaceAccess, tree);
        this.add(searchInPath, Lang.get(Lang.Keys.cm_search));

        Action searchInFiles = new AbstractAction("Search In Files", ImageUtils.getIcon(ImageUtils.SEARCH)) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        this.add(searchInFiles, "Search In Files");
    }

    public void prepareContextMenu(TreePath path){
        // at what kind of node was the context menu invoked?
        boolean isFile = TreeUtils.isFile(path);
        boolean isDir = TreeUtils.isDir(path);
        boolean isDB = TreeUtils.isDB(path);
        boolean isFileSource = TreeUtils.isFileSource(path);

        // check whether items apply to node
        int itemCount = this.getItemCount();
        for (int i=0; i<itemCount; i++){

            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_checkout))) {
                if (isFile)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_checkin))) {
                if (isDir || isDB || isFileSource)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_adddb))) {
                if (TreeUtils.isDbSource(path))
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_delete))) {
                if (isFile || isDir)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_rename))) {
                if (isFile || (isDir && !TreeUtils.isWEBINF(path)))  // never! try to change the name of a WEB-INF folder
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_add))) {
                if (isDir || isDB || isFileSource)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_newversion))) {
                if (isFile)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_showversion))) {
                if (isFile)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
            if ( this.getItemName(i).equals(Lang.get(Lang.Keys.cm_search))) {
                if (isDir || isDB || isFileSource)
                    this.setItemEnabled(i, true);
                else
                    this.setItemEnabled(i, false);
            }
        }
    }

}
