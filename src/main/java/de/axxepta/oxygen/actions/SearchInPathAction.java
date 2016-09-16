package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author Markus on 17.10.2015.
 */
public class SearchInPathAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(SearchInPathAction.class);

    private JTree tree;
    private TreePath rootPath;
    private StandalonePluginWorkspace wsa;

    private static final int SEARCH_DB = 1;
    private static final int SEARCH_ALL_DBS = 2;
    private static final int SEARCH_REPO = 4;
    private static final int SEARCH_XQ = 8;
    public static final int SEARCH_ALL = 15;

    public SearchInPathAction (String name, Icon icon, StandalonePluginWorkspace wsa, JTree tree){
        super(name, icon);
        this.tree = tree;
        rootPath = new TreePath(((DefaultMutableTreeNode) tree.getModel().getRoot()).getPath());
        this.wsa = wsa;
    }

    public void actionPerformed(ActionEvent e) {
        int search_type;
        String pathStr;
        BaseXSource source = null;
        TreePath path = ((TreeListener) tree.getTreeSelectionListeners()[0]).getPath();
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());
        if (!((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
            return;
        } else if (path.getPathCount() == 1) {
            search_type = SEARCH_ALL;
            pathStr = "all sources";
        } else if ((path.getPathCount() == 2) && (path.getPathComponent(1).toString().equals("Databases"))) {
            search_type = SEARCH_ALL_DBS;
            pathStr = "all databases";
        } else {
            switch (path.getPathComponent(1).toString()) {
                case "Databases":
                    search_type = SEARCH_DB;
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.DATABASE;
                    break;
                case "Query Folder":
                    search_type = SEARCH_XQ;
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON_XQ + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.RESTXQ;
                    break;
                default:
                    search_type = SEARCH_REPO;
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON_REPO + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.REPO;

            }
        }
        String filter = JOptionPane.showInputDialog(parentFrame, "Find resource in \n" +
                pathStr, "Search in Path", JOptionPane.PLAIN_MESSAGE);
        if ((filter != null) && (!filter.equals(""))) {
            ArrayList<String> allResources = search(rootPath, search_type, source, path, filter);
            showSearchResults(allResources, pathStr, parentFrame, filter);
        }
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static JPanel createSelectionListPanel(String label, JList resultList) {
        JPanel content = new JPanel(new BorderLayout());
        JLabel foundLabel = new JLabel(label);
        content.add(foundLabel, BorderLayout.NORTH);

        resultList.setLayoutOrientation(JList.VERTICAL);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane resultPane = new JScrollPane(resultList);
        resultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        content.add(resultPane, BorderLayout.CENTER);

        return content;
    }

    private void showSearchResults(ArrayList<String> allResources, String pathStr, JFrame parentFrame, String filter) {
        JList<String> resultList = new JList<>(allResources.toArray(new String[allResources.size()]));

        JDialog resultsDialog = DialogTools.getOxygenDialog(parentFrame, "Open/Find Resources");

        JPanel content = createSelectionListPanel("Search for '" + filter + "' in '" + pathStr + "' found " +
                allResources.size() + " resource(s).", resultList);

        JPanel buttonsPanel = new JPanel();
        JButton openButton = new JButton(
                new OpenListSelectionAction("Open Resource(s)", this.wsa, resultList, resultsDialog));
        buttonsPanel.add(openButton);
        JButton checkOutButton = new JButton(
                new CheckOutListSelectionAction("Check out Resource(s)", resultList, resultsDialog));
        buttonsPanel.add(checkOutButton);
        JButton cancelButton = new JButton(new CloseDialogAction("Cancel", resultsDialog));
        buttonsPanel.add(cancelButton);

        content.add(buttonsPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(resultsDialog, content, parentFrame, 700, 300);
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static ArrayList<String> search(TreePath rootPath, int type, BaseXSource source, TreePath path, String filter) {
        ArrayList<String> allResources = new ArrayList<>();
        WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
        switch (type) {
            case SEARCH_ALL: {
                TreePath currentPath = TreeUtils.pathByAddingChildAsStr(rootPath, Lang.get(Lang.Keys.tree_repo));
                allResources.addAll(searchResourcesInPath(BaseXSource.REPO, currentPath, filter));
                currentPath = TreeUtils.pathByAddingChildAsStr(rootPath, Lang.get(Lang.Keys.tree_restxq));
                allResources.addAll(searchResourcesInPath(BaseXSource.RESTXQ, currentPath, filter));
            }
            case SEARCH_ALL_DBS: {
                List<String> dbList = getDatabases();
                TreePath dbBasePath = TreeUtils.pathByAddingChildAsStr(rootPath, Lang.get(Lang.Keys.tree_DB));
                for (String db : dbList) {
                    TreePath currentPath = TreeUtils.pathByAddingChildAsStr(dbBasePath, db);
                    if (currentPath == null) {
                        currentPath = new TreePath(
                                TreeUtils.insertStrAsNodeLexi(db, (MutableTreeNode) dbBasePath.getLastPathComponent(), false).
                                        getPath());
                    }
                    allResources.addAll(searchResourcesInPath(BaseXSource.DATABASE, currentPath, filter));
                }
                break;
            }
            default : {
                allResources.addAll(searchResourcesInPath(source, path, filter));
            }
        }
        WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
        return allResources;
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static List<String> getDatabases() {
        List<BaseXResource> databaseList;
        List<String> databases = new ArrayList<>();
        try {
            databaseList = ConnectionWrapper.list(BaseXSource.DATABASE, "");
        } catch (Exception er) {
            logger.debug("Couldn't obtain database list. Error: ", er.getMessage());
            databaseList = new ArrayList<>();
        }
        for (BaseXResource database : databaseList) {
            databases.add(database.getName());
        }
        return databases;
    }

    private static List<String> searchResourcesInPath(BaseXSource source, TreePath path, String filter) {
        String basePathStr = TreeUtils.resourceFromTreePath(path);
        List<String> allResources = searchResourcesInPathString(source, basePathStr, filter);
        String searchRoot;
        if (source.equals(BaseXSource.DATABASE))
            searchRoot = TreeUtils.treeStringFromTreePath(TreeUtils.pathToDepth(path,2))+"/";
        else
            searchRoot = TreeUtils.treeStringFromTreePath(path)+"/";
        for (int i=0; i<allResources.size(); i++) {
            allResources.set(i, searchRoot+allResources.get(i));
        }
        return allResources;
    }

    private static List<String> searchResourcesInPathString(BaseXSource source, String basePathStr, String filter) {
        List<String> allResources;
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());
        try {
        //try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            allResources = new BaseXRequest("look", source, basePathStr, filter).getResult();
            //allResources = connection.search(source, basePathStr, filter);
        } catch (IOException io) {
            allResources = new ArrayList<>();
            JOptionPane.showMessageDialog(parentFrame, "Failed to search for BaseX resources:\n" +
                            io.getMessage(),
                    "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
        }
        for (int i=0; i<allResources.size(); i++) {
            allResources.set(i, allResources.get(i).replaceAll("\\\\","/"));
        }
        return allResources;
    }

}
