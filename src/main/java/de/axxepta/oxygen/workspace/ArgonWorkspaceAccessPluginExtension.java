package de.axxepta.oxygen.workspace;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.ui.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

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
                if(
                    //The view ID defined in the "plugin.xml"
                        "ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {


                    // Create data for the tree
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Database: localhost:1984" );

                    //create the child nodes
                    DefaultMutableTreeNode db1Node = new DefaultMutableTreeNode("dbname_1");
                    DefaultMutableTreeNode db2Node = new DefaultMutableTreeNode("dbname_2");
                    DefaultMutableTreeNode db3Node = new DefaultMutableTreeNode("dbname_3");

                    //add the child nodes to the root node
                    root.add(db1Node);
                    root.add(db2Node);
                    root.add(db3Node);

                    // Create a new tree control
                    JTree tree = new JTree(root);

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

    @java.lang.Override
    public boolean applicationClosing() {
        return false;
    }
}
