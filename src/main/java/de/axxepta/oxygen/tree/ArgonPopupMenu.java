package de.axxepta.oxygen.tree;

import de.axxepta.oxygen.actions.*;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.standalone.ui.PopupMenu;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static de.axxepta.oxygen.utils.ImageUtils.getIcon;
import static de.axxepta.oxygen.utils.Lang.Keys.*;

/**
 * PopupMenu class which holds extra ArrayLists for MenuItems and their names, providing access methods
 * for enabling the items via name keys.
 */
//@SuppressWarnings("all")  // CAVE: keep access modifiers public because class is subject to modification by AspectJ
public class ArgonPopupMenu extends PopupMenu {

    private static final Logger logger = LogManager.getLogger(ArgonPopupMenu.class);

    private final ArgonTree tree;
    private final TreeModel treeModel;

    private final ArrayList<String> itemNames;
    private final ArrayList<JMenuItem> items;

    public ArgonPopupMenu(final ArgonTree tree, final TreeModel treeModel) {
        super();
        this.itemNames = new ArrayList<>();
        this.items = new ArrayList<>();
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

    public void show(Component invoker, int x, int y, TreePath path) {
        // set entries in menu to (un)visible outside of the class because used constants are not inherent
        prepareContextMenu(path);
        tree.setSelectionPath(path);
        super.show(invoker, x, y);
    }

    public int getItemCount() {
        return this.items.size();
    }

    public String getItemName(int i) {
        if (this.itemNames.size() > i) {
            return this.itemNames.get(i);
        } else {
            throw new NullPointerException("Tried to access ArgonPopupMenu item number not in list");
        }
    }

    public void setItemEnabled(int i, boolean b) {
        if (this.itemNames.size() > i) {
            items.get(i).setEnabled(b);
        } else {
            throw new NullPointerException("Tried to access ArgonPopupMenu item number not in list");
        }
    }

    public void init(final TreeListener tListener) {
        // Populate context menu
        final Action open = new OpenFileAction(Lang.get(cm_open), getIcon(ImageUtils.BASEX_LOCKED), tListener);
        this.add(open, Lang.get(cm_open));

        final Action checkOut = new CheckOutAction(Lang.get(cm_checkout), getIcon(ImageUtils.BASEX), tListener);
        this.add(checkOut, Lang.get(cm_checkout));

        final Action checkIn = new CheckInAction(Lang.get(cm_checkin), getIcon(ImageUtils.BASEX), tListener);
        this.add(checkIn, Lang.get(cm_checkin));

        this.addSeparator();

        final Action newDatabase = new AddDatabaseAction(Lang.get(cm_adddb), getIcon(ImageUtils.DB_ADD));
        this.add(newDatabase, Lang.get(cm_adddb));

        final Action delete = new DeleteAction(Lang.get(cm_delete), getIcon(ImageUtils.REMOVE), tree);
        this.add(delete, Lang.get(cm_delete));

        final Action rename = new RenameAction(Lang.get(cm_rename), getIcon(ImageUtils.RENAME),
                tree, tListener);
        this.add(rename, Lang.get(cm_rename));

        final Action export = new ExportAction(Lang.get(cm_export), getIcon(ImageUtils.EXPORT),
                tListener);
        this.add(export, Lang.get(cm_export));

        final Action add = new AddNewFileAction(Lang.get(cm_add), getIcon(ImageUtils.FILE_ADD),
                tree);
        this.add(add, Lang.get(cm_add));

        final Action newDir = new NewDirectoryAction(Lang.get(cm_newdir), getIcon(ImageUtils.ADD_DIR),
                tree);
        this.add(newDir, Lang.get(cm_newdir));

        final Action refresh = new RefreshTreeAction(Lang.get(cm_refresh), getIcon(ImageUtils.REFRESH), tree);
        this.add(refresh, Lang.get(cm_refresh));

        this.addSeparator();

        final Action searchInPath = ClassFactory.getInstance().getSearchInPathAction(Lang.get(cm_find),
                getIcon(ImageUtils.SEARCH_PATH), tree);
        this.add(searchInPath, Lang.get(cm_find));

//        final Action searchInFiles = new SearchInFilesAction(Lang.get(cm_search), ImageUtils.getIcon(ImageUtils.SEARCH), tree);
//        this.add(searchInFiles, Lang.get(cm_search));
    }

    public void prepareContextMenu(TreePath path) {
        // at what kind of node was the context menu invoked?
        boolean isFile = TreeUtils.isFile(path);
        boolean isDir = TreeUtils.isDir(path);
        boolean isDB = TreeUtils.isDB(path);
        boolean isInDB = TreeUtils.isInDB(path);
        boolean isRoot = TreeUtils.isRoot(path);
        boolean isDbSource = TreeUtils.isDbSource(path);
        boolean isFileSource = TreeUtils.isFileSource(path);
        final URL url;
        try {
            url = new URL(TreeUtils.urlStringFromTreePath(path));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        final CustomProtocolURLHandlerExtension handlerExtension = new CustomProtocolURLHandlerExtension();
        boolean isLocked = handlerExtension.canCheckReadOnly(url.getProtocol()) && !handlerExtension.isReadOnly(url);

        // check whether items apply to node
        int itemCount = this.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            final String itemName = this.getItemName(i);
            if (itemName.equals(Lang.get(cm_open))) {
                this.setItemEnabled(i, isFile);
            } else if (itemName.equals(Lang.get(cm_checkout))) {
                this.setItemEnabled(i, isFile && !isLocked);
            } else if (itemName.equals(Lang.get(cm_checkin))) {
                this.setItemEnabled(i, isFile && isLocked);
            } else if (itemName.equals(Lang.get(cm_adddb))) {
                this.setItemEnabled(i, TreeUtils.isDbSource(path));
            } else if (itemName.equals(Lang.get(cm_delete))) {
                this.setItemEnabled(i, isFile || isDir || isDB);
            } else if (itemName.equals(Lang.get(cm_rename))) {
                // never! try to change the name of a WEB-INF folder
                this.setItemEnabled(i, isFile || (isDir && !TreeUtils.isWEBINF(path)));
            } else if (itemName.equals(Lang.get(cm_export))) {
                this.setItemEnabled(i, !isRoot && !isDbSource);
            } else if (itemName.equals(Lang.get(cm_add))) {
                this.setItemEnabled(i, isDir || isDB || isFileSource);
            } else if (itemName.equals(Lang.get(cm_newdir))) {
                this.setItemEnabled(i, isDir || isDB || isFileSource);
            } else if (itemName.equals(Lang.get(cm_showversion))) {
                this.setItemEnabled(i, isFile);
            } else if (itemName.equals(Lang.get(cm_find))) {
                this.setItemEnabled(i, !isFile);
            } else if (itemName.equals(Lang.get(cm_search))) {
                this.setItemEnabled(i, isDB || isInDB);
            }
        }
    }

}
