package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.rest.BasexWrapper;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import org.xml.sax.SAXException;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.ui.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    /**
     * The CMS messages area.
     */
    private JTextArea cmsMessagesArea;


    @java.lang.Override
    public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

        pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
        StandalonePluginWorkspace pluginWorkspaceAccess1 = pluginWorkspaceAccess;

        pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
             */
            @Override
            public void customizeView(ViewInfo viewInfo) {

                Iterator<String> iterator = null;

                if(
                    //The view ID defined in the "plugin.xml"
                    "ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {

                    // Create data for the tree
                    // DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Database: localhost:1984" );

                    // Create some data to populate our tree.
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("BaseX Server");
                    root.setAllowsChildren(true);
                    DefaultMutableTreeNode databases = new DefaultMutableTreeNode("Databases");
                    databases.setAllowsChildren(true);
                    root.add(databases);
                    DefaultMutableTreeNode queryFolder = new DefaultMutableTreeNode("Query Folder");
                    queryFolder.setAllowsChildren(true);
                    root.add(queryFolder);

                    BasexWrapper basexWrapper = new BasexWrapper();
                    basexWrapper.setRestApiClient();

                    List<String> databaseList = null;
                    try {
                        databaseList = basexWrapper.getResources();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }

                    iterator = databaseList.iterator();

                    int i = 0;
                    while (iterator.hasNext()) {
                        DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(databaseList.get(i));
                        dbNode.setAllowsChildren(true);
                        databases.add(dbNode);
                        iterator.next();
                        i++;
                    }

                    // Create a new tree control
                    DefaultTreeModel treeModel = new DefaultTreeModel(root);
                    treeModel.setAsksAllowsChildren(true);
                    BasexTree tree = new BasexTree(treeModel);
                    tree.setEditable(true);
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    setTreeState(tree, new TreePath(root), false);

                    // Add Tree Listener
                    TreeListener tListener = new TreeListener(tree, treeModel, pluginWorkspaceAccess, basexWrapper);
                    tree.addTreeWillExpandListener(tListener);
                    tree.addMouseListener(tListener);
                    tree.addTreeSelectionListener(tListener);

                    //JOptionPane.showMessageDialog(null, "This language just gets better and better!");

                    cmsMessagesArea = new JTextArea("CMS Session History:");
                    JScrollPane scrollPane = new JScrollPane(cmsMessagesArea);
                    scrollPane.getViewport().add(tree);
                    viewInfo.setComponent(scrollPane);

                    viewInfo.setTitle("BaseX Db Connection");
                    viewInfo.setIcon(Icons.getIcon(Icons.CMS_MESSAGES_CUSTOM_VIEW_STRING));
                } else if("Project".equals(viewInfo.getViewID())) {
                    // Change the 'Project' view title.
                    viewInfo.setTitle("CMS Project");
                }
            }
        });
    }

    public static void setTreeState(JTree tree, TreePath path, boolean expanded) {
        Object lastNode = path.getLastPathComponent();
        for (int i = 0; i < tree.getModel().getChildCount(lastNode); i++) {
            Object child = tree.getModel().getChild(lastNode,i);
            TreePath pathToChild = path.pathByAddingChild(child);
            setTreeState(tree,pathToChild,expanded);
        }
        if (expanded)
            tree.expandPath(path);
        else
            tree.collapsePath(path);


    }

    @java.lang.Override
    public boolean applicationClosing() {
        return true;
    }
}
