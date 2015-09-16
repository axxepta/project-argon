package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.rest.BasexWrapper;
import de.axxepta.oxygen.rest.ListDBEntries;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.editor.ContentTypes;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.Workspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblems;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblemsFilter;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.*;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.ui.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    /**
     * The CMS messages area.
     */
    private JTextArea cmsMessagesArea;
    private JTextArea argonOutputArea;

    private static final Logger logger = LogManager.getLogger(ArgonWorkspaceAccessPluginExtension.class);

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
                    // explicit tree model necessary to use allowsChildren for definition of leafs
                    DefaultTreeModel treeModel = new DefaultTreeModel(root);
                    treeModel.setAsksAllowsChildren(true);
                    final BasexTree tree = new BasexTree(treeModel);
                    tree.setEditable(true);
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    setTreeState(tree, new TreePath(root), false);

                    // Add context menu
                    JPopupMenu contextMenu = new JPopupMenu();
                    Action checkOut = new AbstractAction("Check Out") {
                        public void actionPerformed(ActionEvent e) {
                            TreePath path = tree.getPath();
                            logger.debug("-- double click --");
                            String db_path = BasexTree.urlStringFromTreePath(path);
                            logger.info("DbPath: " + db_path);
                            if (!tree.getNode().getAllowsChildren()) {
                                // open file
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
                    contextMenu.add(checkOut);
                    Action checkIn = new AbstractAction("Check In") {
                        public void actionPerformed(ActionEvent e) {
                        }
                    };
                    contextMenu.add(checkIn);
                    contextMenu.addSeparator();
                    Action delete = new AbstractAction("Delete") {
                        public void actionPerformed(ActionEvent e) {
                            //
                        }
                    };
                    contextMenu.add(delete);
                    Action add = new AbstractAction("Add") {
                        public void actionPerformed(ActionEvent e) {
                            //
                        }
                    };
                    contextMenu.add(add);
                    tree.add(contextMenu);

                    // Add Tree Listener
                    TreeListener tListener = new TreeListener(tree, treeModel, contextMenu, pluginWorkspaceAccess, basexWrapper);
                    tree.addTreeWillExpandListener(tListener);
                    tree.addMouseListener(tListener);
                    tree.addTreeSelectionListener(tListener);
                    TopicHolder.saveFile.register(tListener);

                    //
                    cmsMessagesArea = new JTextArea("CMS Session History:");
                    JScrollPane scrollPane = new JScrollPane(cmsMessagesArea);
                    scrollPane.getViewport().add(tree);
                    viewInfo.setComponent(scrollPane);

                    viewInfo.setTitle("BaseX Db Connection");
                    viewInfo.setIcon(Icons.getIcon(Icons.CMS_MESSAGES_CUSTOM_VIEW_STRING));
                } else if ("ArgonWorkspaceAccessOutputID".equals(viewInfo.getViewID())) {
                    argonOutputArea = new JTextArea();
                    JScrollPane scrollPane = new JScrollPane(argonOutputArea);
                    viewInfo.setComponent(scrollPane);
                    viewInfo.setTitle("Argon BaseX Query Output");
                } else if("Project".equals(viewInfo.getViewID())) {
                    // Change the 'Project' view title.
                    viewInfo.setTitle("CMS Project");
                }
            }
        });

        pluginWorkspaceAccess.addEditorChangeListener(new WSEditorChangeListener() {
            /**
             * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorOpened(java.net.URL)
             */
            @Override
            public void editorOpened(URL editorLocation) {
                final WSEditor editorAccess = pluginWorkspaceAccess.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);
                //TODO: define string static somewhere
                boolean isArgon = editorLocation.toString().startsWith("argon");
                boolean isXquery = (editorLocation.toString().endsWith("xqm") ||
                        editorLocation.toString().endsWith("XQM") ||
                        editorLocation.toString().endsWith("xquery"));
                if (isArgon && isXquery)
                    editorAccess.addValidationProblemsFilter(new ValidationProblemsFilter() {
                        /**
                         * @see ro.sync.exml.workspace.api.editor.validation.ValidationProblemsFilter#filterValidationProblems(ro.sync.exml.workspace.api.editor.validation.ValidationProblems)
                         */
                        @Override
                        public void filterValidationProblems(ValidationProblems validationProblems) {
                            // get content of editor window
                            String editorContent;
                            try {
                                InputStream editorStream = editorAccess.createContentInputStream();
/*                            StringWriter writer = new StringWriter();
                            org.apache.commons.io.IOUtils.copy(editorStream, writer, "UTF-8");
                            editorContent = writer.toString();*/
                                Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
                                editorContent = s.hasNext() ? s.next() : "";
                                editorStream.close();
                            } catch (IOException er) {
                                logger.error(er);
                                editorContent = "";
                            }
                            // pass content of editor window to ListDBEntries with queryTest
                            ArrayList<String> valProbStr;
                            try {
                                ListDBEntries testQuery = new ListDBEntries("queryTest", "", editorContent);
                                valProbStr = testQuery.getResult();
                            } catch (Exception er) {
                                logger.error("query to BaseX failed");
                                valProbStr = new ArrayList<String>();
                                valProbStr.add("1");
                                valProbStr.add("1");
                                valProbStr.add("Fatal BaseX request error: "+er.getMessage());
                            }
                            // build DocumentPositionedInfo list from query return;
                            List<DocumentPositionedInfo> problemList = new ArrayList<DocumentPositionedInfo>();
                            if (valProbStr.size() > 0) {
                                DocumentPositionedInfo dpi =
                                        new DocumentPositionedInfo(DocumentPositionedInfo.SEVERITY_ERROR, valProbStr.get(2), "",
                                                Integer.parseInt(valProbStr.get(0)), Integer.parseInt(valProbStr.get(1)), 0);
                                problemList.add(dpi);
                            }
                            //The DocumentPositionInfo represents an error with location in the document and has a constructor like:
                            //  public DocumentPositionedInfo(int severity, String message, String systemID, int line, int column, int length)
                            validationProblems.setProblemsList(problemList);
                            super.filterValidationProblems(validationProblems);
                        }
                    });
            }
        }, PluginWorkspace.MAIN_EDITING_AREA);

        final Action runBaseXQueryAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);

                if (editorAccess != null) {
                    URL editorUrl = editorAccess.getEditorLocation();
                    boolean isXquery = (editorUrl.toString().endsWith("xqm") ||
                            editorUrl.toString().endsWith("XQM") ||
                            editorUrl.toString().endsWith("xquery"));
                    if (isXquery) {
                        // get content of current editor window
                        String editorContent;
                        try {
                            InputStream editorStream = editorAccess.createContentInputStream();
                            Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
                            editorContent = s.hasNext() ? s.next() : "";
                            editorStream.close();
                        } catch (IOException er) {
                            logger.error(er);
                            editorContent = "";
                        }
                        // get database name of current editor window
                        int startInd = editorUrl.toString().indexOf(":");
                        int stopInd = editorUrl.toString().indexOf("/", startInd+2);
                        //ToDo: catch unexpected error that argon URL is malformed
                        String db_name = editorUrl.toString().substring(startInd+1, stopInd);
                        // pass content of editor window to ListDBEntries with queryRun
                        String queryRes;
                        try {
                            ListDBEntries testQuery = new ListDBEntries("queryRun", db_name, editorContent);
                            queryRes = testQuery.getAnswer();
                        } catch (Exception er) {
                            logger.error("query to BaseX failed");
                            queryRes = "";
                        }
                        //+ display result of query in a new info window
                        //argonOutputArea.setText(queryRes);
                        //pluginWorkspaceAccess.showView("ArgonWorkspaceAccessOutputID", true);

                        //+ display result of query in a new editor window
                        URL newEditor = pluginWorkspaceAccess.createNewEditor("txt",ContentTypes.PLAIN_TEXT_CONTENT_TYPE,queryRes);

                    } else {
                        pluginWorkspaceAccess.showInformationMessage("No XQuery in editor window!");
                    }

                } else {
                    pluginWorkspaceAccess.showInformationMessage("No editor window opened!");
                }
            }
        };

        pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
             */
            @SuppressWarnings("serial")
            @Override
            public void customizeToolbar(ToolbarInfo toolbarInfo) {
                //The toolbar ID is defined in the "plugin.xml"
                if ("ArgonWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
                    List<JComponent> comps = new ArrayList<JComponent>();
                    JComponent[] initialComponents = toolbarInfo.getComponents();
                    boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0;
                    if (hasInitialComponents) {
                        // Add initial toolbar components
                        for (JComponent toolbarItem : initialComponents) {
                            comps.add(toolbarItem);
                        }
                    }

                    // run query in current editor window
                    ToolbarButton runQueryButton = new ToolbarButton(runBaseXQueryAction, true);
                    runQueryButton.setText("Run BaseX Query");

                    // Add in toolbar
                    comps.add(runQueryButton);
                    toolbarInfo.setComponents(comps.toArray(new JComponent[0]));

                    // Set title
                    String initialTitle = toolbarInfo.getTitle();
                    String title = "";
                    if (hasInitialComponents && initialTitle != null && initialTitle.trim().length() > 0) {
                        // Include initial tile
                        title += initialTitle + " | ";
                    }
                    title += "BaseX DB";
                    toolbarInfo.setTitle(title);
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
