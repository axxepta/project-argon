package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.BaseXRunQueryAction;
import de.axxepta.oxygen.actions.ReplyAuthorCommentAction;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.BasexTreeCellRenderer;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblems;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblemsFilter;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.*;
import ro.sync.exml.workspace.api.standalone.ui.PopupMenu;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.ui.Icons;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Main plugin class, defining tree, context menu, and toolbar
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

        pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
             */
            @Override
            public void customizeView(ViewInfo viewInfo) {

                Iterator<String> iterator;

                if("ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {
                    //The view ID defined in the "plugin.xml"

                    // Create some data to populate our tree.
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("BaseX Server");
                    root.setAllowsChildren(true);
                    DefaultMutableTreeNode databases = new DefaultMutableTreeNode("Databases");
                    databases.setAllowsChildren(true);
                    root.add(databases);
                    DefaultMutableTreeNode queryFolder = new DefaultMutableTreeNode("Query Folder");
                    queryFolder.setAllowsChildren(true);
                    root.add(queryFolder);
                    DefaultMutableTreeNode repoFolder = new DefaultMutableTreeNode("Repo Folder");
                    queryFolder.setAllowsChildren(true);
                    root.add(repoFolder);

                    ArrayList<String> databaseList;
                    try {
                        databaseList = (new BaseXRequest("list", BaseXSource.DATABASE, "")).getResult();
                    } catch (Exception er) {
                        JOptionPane.showMessageDialog(null, "Couldn't read list of databases. Check whether BaseX server is running."
                                , "BaseX Communication Error", JOptionPane.PLAIN_MESSAGE);
                        databaseList = new ArrayList<>();
                    }
                    for (int i=0; i<(databaseList.size()/2); i++) {
                        DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(databaseList.get(i));
                        dbNode.setAllowsChildren(true);
                        databases.add(dbNode);
                    }

                    // Create a new tree control
                    // explicit tree model necessary to use allowsChildren for definition of leafs
                    final DefaultTreeModel treeModel = new DefaultTreeModel(root);
                    treeModel.setAsksAllowsChildren(true);
                    final BasexTree tree = new BasexTree(treeModel);
                    tree.setEditable(true);
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    setTreeState(tree, new TreePath(root), false);

                    // Add context menu
                    PopupMenu contextMenu = new PopupMenu();
                    //JPopupMenu contextMenu = new JPopupMenu();

                    // Add Tree Listener
                    final TreeListener tListener = new TreeListener(tree, treeModel, contextMenu, pluginWorkspaceAccess);
                    tree.addTreeWillExpandListener(tListener);
                    tree.addMouseListener(tListener);
                    tree.addTreeSelectionListener(tListener);
                    TopicHolder.saveFile.register(tListener);
                    TopicHolder.deleteFile.register(tListener);

                    // Populate context menu
                    Action checkOut = new AbstractAction("Check Out", BasexTreeCellRenderer.createImageIcon("/OpenURL16.gif")) {
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
                    contextMenu.add(checkOut);

                    Action checkIn = new AbstractAction("Check In", BasexTreeCellRenderer.createImageIcon("/AddFile16.gif")) {
                        public void actionPerformed(ActionEvent e) {
                        }
                    };
                    contextMenu.add(checkIn);

                    contextMenu.addSeparator();

                    Action delete = new AbstractAction("Delete", BasexTreeCellRenderer.createImageIcon("/Remove16.png")) {
                        public void actionPerformed(ActionEvent e) {
                            TreePath path = tListener.getPath();
                            BaseXSource source = TreeUtils.sourceFromTreePath(path);
                            String db_path = TreeUtils.resourceFromTreePath(path);
                            if ((source != null) && (!db_path.equals(""))) {
                                // don't try to delete databases!
                                if ((!(source == BaseXSource.DATABASE)) || (db_path.contains("/"))) {
                                    try {
                                        new BaseXRequest("delete", source, db_path);
                                        treeModel.removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
                                        //TopicHolder.deleteFile.postMessage(db_path);
                                    } catch (Exception er) {
                                        JOptionPane.showMessageDialog(null, "Failed to delete resource", "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "You cannot delete databases!", "BaseX Error", JOptionPane.PLAIN_MESSAGE);
                                }
                            }
                        }
                    };
                    contextMenu.add(delete);

                    Action add = new AbstractAction("Add", BasexTreeCellRenderer.createImageIcon("/AddFile16.gif")) {
                        public void actionPerformed(ActionEvent e) {
/*                            TreePath path = tListener.getPath();
                            String db_path = BasexTree.urlStringFromTreePath(path);
                            if (tListener.getNode().getAllowsChildren()) {
                                try {
                                    ListDBEntries fileDummy = new ListDBEntries("add", db_path, db_path);
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }*/
                        }
                    };
                    contextMenu.add(add);

                    final Action refresh = new AbstractAction("Refresh", BasexTreeCellRenderer.createImageIcon("/Refresh16.png")) {
                        public void actionPerformed(ActionEvent e) {
                            //treeModel.reload();
                            // removes parent node from tree???
                            // build structure copy of expanded tree, reload from root
                        }
                    };
                    contextMenu.add(refresh);

                    contextMenu.addSeparator();

                    final Action searchInPath = new AbstractAction("Search In Path", BasexTreeCellRenderer.createImageIcon("/SearchInPath16.png")) {
                        public void actionPerformed(ActionEvent e) {
                            TreePath path = tListener.getPath();
                            if (path.getPathCount() == 1) {
                                JOptionPane.showMessageDialog(null, "Please select source to search in (Databases/RestXQ/Repo).",
                                        "Search in Path", JOptionPane.PLAIN_MESSAGE);
                                return;
                            }
                            // ToDo: own class...
                            if ((path.getPathCount() == 2) && (path.getPathComponent(1).toString().equals("Databases"))) {
                                JOptionPane.showMessageDialog(null, "Please select specific database to search in.",
                                        "Search in Path", JOptionPane.PLAIN_MESSAGE);
                                return;
                            }
                            if (((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
                                String pathStr;
                                BaseXSource source;
                                switch (path.getPathComponent(1).toString()) {
                                    case "Databases":
                                        if (path.getPathCount() == 2) {
                                            pathStr = path.getPathComponent(1).toString();
                                        } else {
                                            pathStr = "argon:" + TreeUtils.resourceFromTreePath(path);
                                        }
                                        source = BaseXSource.DATABASE;
                                        break;
                                    case "Query Folder":
                                        if (path.getPathCount() == 2) {
                                            pathStr = path.getPathComponent(1).toString();
                                        } else {
                                            pathStr = "argon_restxq:" + TreeUtils.resourceFromTreePath(path);
                                        }
                                        source = BaseXSource.RESTXQ;
                                        pathStr = "argon_restxq:" + TreeUtils.resourceFromTreePath(path);
                                        break;
                                    default:
                                        if (path.getPathCount() == 2) {
                                            pathStr = path.getPathComponent(1).toString();
                                        } else {
                                            pathStr = "argon_repo:" + TreeUtils.resourceFromTreePath(path);
                                        }
                                        source = BaseXSource.REPO;

                                }
                                String resource = JOptionPane.showInputDialog(null, "Find resource in path\n" +
                                        pathStr, "Search in Path", JOptionPane.PLAIN_MESSAGE);
                                if ((resource != null) && (!resource.equals(""))) {
                                    // ToDo: add filter in query or here
                                    String query = TreeUtils.resourceFromTreePath(path);
                                    String allResources;
                                    try {
                                        BaseXRequest search = new BaseXRequest("look", source, query);
                                        allResources = search.getAnswer();

                                    } catch (Exception er) {
                                        JOptionPane.showMessageDialog(null, "Failed to search for BaseX resources.\n Check if server ist still running.",
                                                "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                                        allResources = "";
                                    }
                                    // ToDo: expand branches
                                    JOptionPane.showMessageDialog(null, allResources,
                                            "Search in BaseX", JOptionPane.PLAIN_MESSAGE);
                                }
                            }
                        }
                    };
                    contextMenu.add(searchInPath);

                    Action searchInFiles = new AbstractAction("Search In Files", BasexTreeCellRenderer.createImageIcon("/SearchInPath16.png")) {
                        public void actionPerformed(ActionEvent e) {
                        }
                    };
                    contextMenu.add(searchInFiles);

                    tree.add(contextMenu);

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
                boolean isArgon = (editorLocation.toString().startsWith("argon"));
                boolean isXquery = (editorLocation.toString().toLowerCase().endsWith("xqm") ||
                        editorLocation.toString().toLowerCase().endsWith("xq") ||
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
                                //ListDBEntries testQuery = new ListDBEntries("queryTest", "", editorContent);
                                //valProbStr = testQuery.getResult();
                                BaseXRequest testQuery = new BaseXRequest("parse",
                                        BaseXSource.DATABASE, editorContent);
                                valProbStr = testQuery.getResult();
                            } catch (Exception er) {
                                logger.error("query to BaseX failed");
                                valProbStr = new ArrayList<>();
                                valProbStr.add("1");
                                valProbStr.add("1");
                                valProbStr.add("Fatal BaseX request error: "+er.getMessage());
                                er.printStackTrace();
                            }
                            // build DocumentPositionedInfo list from query return;
                            List<DocumentPositionedInfo> problemList = new ArrayList<>();
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

        // create actions for Toolbars
        final Action runBaseXQueryAction = new BaseXRunQueryAction("Run BaseX Query",
                BasexTreeCellRenderer.createImageIcon("/RunQuery.png"), pluginWorkspaceAccess);
        final Action replyToAuthorComment = new ReplyAuthorCommentAction("Reply Author Comment",
                BasexTreeCellRenderer.createImageIcon("/ReplyComment.png"), pluginWorkspaceAccess);
        //final Action replyToAuthorComment = new ReplyAuthorCommentAction(pluginWorkspaceAccess);

        pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
             */
            @SuppressWarnings("serial")
            @Override
            public void customizeToolbar(ToolbarInfo toolbarInfo) {
                //The toolbar ID is defined in the "plugin.xml"
                if ("ArgonWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
                    List<JComponent> comps = new ArrayList<>();
                    JComponent[] initialComponents = toolbarInfo.getComponents();
                    boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0;
                    if (hasInitialComponents) {
                        // Add initial toolbar components
                        for (JComponent toolbarItem : initialComponents) {
                            comps.add(toolbarItem);
                        }
                    }

                    // Add toolbar buttons
                    // run query in current editor window
                    ToolbarButton runQueryButton = new ToolbarButton(runBaseXQueryAction, true);
                    runQueryButton.setText("");
                    // run query in current editor window
                    ToolbarButton replyCommentButton = new ToolbarButton(replyToAuthorComment, true);
                    replyCommentButton.setText("");

                    // Add in toolbar
                    comps.add(runQueryButton);
                    comps.add(replyCommentButton);
                    toolbarInfo.setComponents(comps.toArray(new JComponent[comps.size()]));

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

                if ("ReviewID".equals(toolbarInfo.getToolbarID())) {
                    List<JComponent> comps = new ArrayList<>();
                    JComponent[] initialComponents = toolbarInfo.getComponents();
                    boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0;
                    if (hasInitialComponents) {
                        // Add initial toolbar components
                        for (JComponent toolbarItem : initialComponents) {
                            comps.add(toolbarItem);
                        }
                    }
                    // run query in current editor window
                    ToolbarButton replyCommentButton = new ToolbarButton(replyToAuthorComment, true);
                    replyCommentButton.setText("");
                    comps.add(replyCommentButton);
                    toolbarInfo.setComponents(comps.toArray(new JComponent[comps.size()]));
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
