package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.*;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.versioncontrol.DateTableCellRenderer;
import de.axxepta.oxygen.versioncontrol.VersionHistoryEntry;
import de.axxepta.oxygen.versioncontrol.VersionHistoryTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblems;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblemsFilter;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.*;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.ui.Icons;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Main plugin class, defining tree, context menu, and toolbar
 */
public class ArgonWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

    /**
     * The CMS messages area.
     */
    private JTextArea cmsMessagesArea;
    private JTable versionHistoryTable;
    private ToolbarButton runQueryButton;   // declare here for access in inner functions (toggling)
    private ToolbarButton newVersionButton;
    private ToolbarButton replyCommentButton;

    private static final Logger logger = LogManager.getLogger(ArgonWorkspaceAccessPluginExtension.class);
    private StandalonePluginWorkspace pluginWorkspaceAccess;

    @java.lang.Override
    public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
        pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);

        // init language pack
        if (pluginWorkspaceAccess.getUserInterfaceLanguage().equals("de_DE"))
            Lang.init(Locale.GERMAN);
        else
            Lang.init(Locale.UK);

        // init icon map
        ImageUtils.init();

        // init connection
        BaseXConnectionWrapper.refreshFromOptions(false);
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_HOST));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_HTTP_PORT));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_TCP_PORT));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_USERNAME));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_PASSWORD));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_CONNECTION));
        pluginWorkspaceAccess.getOptionsStorage().addOptionListener(new BaseXOptionListener(BaseXOptionPage.KEY_BASEX_LOGFILE));

        ArgonEditorsWatchMap.init();

        pluginWorkspaceAccess.addViewComponentCustomizer(new BaseXViewComponentCustomizer());

        pluginWorkspaceAccess.addEditorChangeListener(new WSEditorChangeListener() {

            @Override
            public void editorPageChanged(URL editorLocation) {
                checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
            }

            @Override
            public void editorSelected(URL editorLocation) {
                checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
            }

            @Override
            public void editorActivated(URL editorLocation) {
                checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
            }

            @Override
            public void editorClosed(URL editorLocation) {
                if (editorLocation.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON))
                    ArgonEditorsWatchMap.removeURL(editorLocation);
                checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);
            }

            @Override
            public void editorOpened(URL editorLocation) {
                if (editorLocation.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON))
                    ArgonEditorsWatchMap.addURL(editorLocation);
                checkEditorDependentMenuButtonStatus(pluginWorkspaceAccess);

                final WSEditor editorAccess = pluginWorkspaceAccess.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);
                boolean isArgon = (editorLocation.toString().startsWith(CustomProtocolURLHandlerExtension.ARGON));

                if (isArgon && URLUtils.isQuery(editorLocation))
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
                ImageUtils.createImageIcon("/images/RunQuery.png"), pluginWorkspaceAccess);
        // ToDo: ICON
        final Action newVersionAction = new NewVersionButtonAction("Increase File Version",
                ImageUtils.createImageIcon("/images/RunQuery.png"), pluginWorkspaceAccess);
        final Action replyToAuthorComment = new ReplyAuthorCommentAction("Reply Author Comment",
                ImageUtils.createImageIcon("/images/ReplyComment.png"), pluginWorkspaceAccess);

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
                        comps.addAll(Arrays.asList(initialComponents));
                    }

                    // Add toolbar buttons
                    // run query in current editor window
                    runQueryButton = new ToolbarButton(runBaseXQueryAction, true);
                    runQueryButton.setText("");
                    // increase revision of document in current editor window
                    newVersionButton = new ToolbarButton(newVersionAction, true);
                    newVersionButton.setText("");

                    // Add in toolbar
                    comps.add(runQueryButton);
                    comps.add(newVersionButton);
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

                if ("toolbar.review".equals(toolbarInfo.getToolbarID())) {
                    List<JComponent> comps = new ArrayList<>();
                    JComponent[] initialComponents = toolbarInfo.getComponents();
                    boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0;
                    if (hasInitialComponents) {
                        // Add initial toolbar components
                        comps.addAll(Arrays.asList(initialComponents));
                    }
                    // reply to author comment
                    replyCommentButton = new ToolbarButton(replyToAuthorComment, true);
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

    public void checkEditorDependentMenuButtonStatus(PluginWorkspace pluginWorkspaceAccess){
        WSEditor currentEditor = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if(currentEditor == null) {
            runQueryButton.setEnabled(false);
            newVersionButton.setEnabled(false);
        } else {
            if (URLUtils.isQuery(currentEditor.getEditorLocation())) {
                runQueryButton.setEnabled(true);
                newVersionButton.setEnabled(true);
            } else {
                runQueryButton.setEnabled(false);
                if (URLUtils.isXML(currentEditor.getEditorLocation())) {
                    newVersionButton.setEnabled(true);
                } else {
                    newVersionButton.setEnabled(false);
                }
            }
        }
    }

    public void updateVersionHistory(List<VersionHistoryEntry> historyList) {
        ((VersionHistoryTableModel) versionHistoryTable.getModel()).setNewContent(historyList);
        versionHistoryTable.setFillsViewportHeight(true);
        versionHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new DateTableCellRenderer());
    }

    @java.lang.Override
    public boolean applicationClosing() {
        return true;
    }


    private class BaseXViewComponentCustomizer implements ViewComponentCustomizer {
        /**
         * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
         */
        @Override
        public void customizeView(ViewInfo viewInfo) {

            if ("ArgonWorkspaceAccessID".equals(viewInfo.getViewID())) {
                //The view ID defined in the "plugin.xml"

                // Create some data to populate our tree.
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(Lang.get(Lang.Keys.tree_root));
                root.setAllowsChildren(true);
                DefaultMutableTreeNode databases = new DefaultMutableTreeNode(Lang.get(Lang.Keys.tree_DB));
                databases.setAllowsChildren(true);
                root.add(databases);
                DefaultMutableTreeNode queryFolder = new DefaultMutableTreeNode(Lang.get(Lang.Keys.tree_restxq));
                queryFolder.setAllowsChildren(true);
                root.add(queryFolder);
                DefaultMutableTreeNode repoFolder = new DefaultMutableTreeNode(Lang.get(Lang.Keys.tree_repo));
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
                for (int i = 0; i < (databaseList.size() / 2); i++) {
                    DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(databaseList.get(i));
                    dbNode.setAllowsChildren(true);
                    databases.add(dbNode);
                }

                // Create a new tree control
                // explicit tree model necessary to use allowsChildren for definition of leafs
                final DefaultTreeModel treeModel = new DefaultTreeModel(root);
                treeModel.setAsksAllowsChildren(true);
                final BasexTree tree = new BasexTree(treeModel);
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                setTreeState(tree, new TreePath(root), false);

                // Add context menu
                BaseXPopupMenu contextMenu = new BaseXPopupMenu();

                // Add Tree Listener
                final TreeListener tListener = new TreeListener(tree, treeModel, contextMenu, pluginWorkspaceAccess);
                tree.addTreeWillExpandListener(tListener);
                tree.addMouseListener(tListener);
                tree.addTreeSelectionListener(tListener);
                TopicHolder.saveFile.register(tListener);
                TopicHolder.deleteFile.register(tListener);

                // Add transfer handler for DnD
                tree.setTransferHandler(new BaseXTreeTransferHandler());
                tree.setDropMode(DropMode.ON);

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
                contextMenu.add(checkOut, Lang.get(Lang.Keys.cm_checkout));

                Action checkIn = new AbstractAction(Lang.get(Lang.Keys.cm_checkin), ImageUtils.getIcon(ImageUtils.FILE_ADD)) {
                    public void actionPerformed(ActionEvent e) {
                    }
                };
                contextMenu.add(checkIn, Lang.get(Lang.Keys.cm_checkin));

                contextMenu.addSeparator();

                Action newDatabase = new AddDatabaseAction(Lang.get(Lang.Keys.cm_adddb), ImageUtils.getIcon(ImageUtils.DB_ADD),
                        treeModel, tListener);
                contextMenu.add(newDatabase, Lang.get(Lang.Keys.cm_adddb));

                Action delete = new DeleteAction(Lang.get(Lang.Keys.cm_delete), ImageUtils.getIcon(ImageUtils.REMOVE),
                        tree, tListener);
                contextMenu.add(delete, Lang.get(Lang.Keys.cm_delete));

                Action rename = new RenameAction(Lang.get(Lang.Keys.cm_rename), ImageUtils.getIcon(ImageUtils.RENAME),
                        tree, tListener);
                contextMenu.add(rename, Lang.get(Lang.Keys.cm_rename));

                // ToDo: ICON
                Action newVersion = new NewVersionContextAction(Lang.get(Lang.Keys.cm_newversion), ImageUtils.getIcon(ImageUtils.RENAME),
                        tListener, pluginWorkspaceAccess);
                contextMenu.add(newVersion, Lang.get(Lang.Keys.cm_newversion));

                // ToDo: ICON
                //Action showVersionHistory = new ShowVersionHistoryContextAction(Lang.get(Lang.Keys.cm_showversion),
                //        ImageUtils.getIcon(ImageUtils.RENAME), tListener);
                Action showVersionHistory = new ShowVersionHistoryContextAction(Lang.get(Lang.Keys.cm_showversion),
                        ImageUtils.getIcon(ImageUtils.RENAME), tListener, ArgonWorkspaceAccessPluginExtension.this);
                contextMenu.add(showVersionHistory, Lang.get(Lang.Keys.cm_showversion));

                Action add = new AddNewFileAction(Lang.get(Lang.Keys.cm_add), ImageUtils.getIcon(ImageUtils.FILE_ADD),
                        pluginWorkspaceAccess, tree);
                contextMenu.add(add, Lang.get(Lang.Keys.cm_add));

                final Action refresh = new RefreshTreeAction(Lang.get(Lang.Keys.cm_refresh), ImageUtils.getIcon(ImageUtils.REFRESH), tree);
                contextMenu.add(refresh, Lang.get(Lang.Keys.cm_refresh));

                contextMenu.addSeparator();

                final Action searchInPath = new SearchInPathAction(Lang.get(Lang.Keys.cm_search), ImageUtils.getIcon(ImageUtils.SEARCH),
                        pluginWorkspaceAccess, tree);
                contextMenu.add(searchInPath, Lang.get(Lang.Keys.cm_search));

                Action searchInFiles = new AbstractAction("Search In Files", ImageUtils.getIcon(ImageUtils.SEARCH)) {
                    public void actionPerformed(ActionEvent e) {
                    }
                };
                contextMenu.add(searchInFiles, "Search In Files");

                tree.add(contextMenu);

                //
                cmsMessagesArea = new JTextArea("CMS Session History:");
                JScrollPane scrollPane = new JScrollPane(cmsMessagesArea);
                scrollPane.getViewport().add(tree);
                viewInfo.setComponent(scrollPane);

                viewInfo.setTitle("BaseX DB Connection");
                viewInfo.setIcon(Icons.getIcon(Icons.CMS_MESSAGES_CUSTOM_VIEW_STRING));
            } else if ("ArgonWorkspaceAccessOutputID".equals(viewInfo.getViewID())) {
                JButton compareRevisionsButton = new JButton("Compare");
                JButton replaceRevisionButton = new JButton("Replace");
                JPanel versionHistoryButtonPanel = new JPanel();
                versionHistoryButtonPanel.setLayout(new BoxLayout(versionHistoryButtonPanel, BoxLayout.X_AXIS));
                compareRevisionsButton.setAlignmentY(Component.CENTER_ALIGNMENT);
                versionHistoryButtonPanel.add(compareRevisionsButton);
                versionHistoryButtonPanel.add(new Box.Filler(
                        new Dimension(10,10), new Dimension(20,10), new Dimension(50,10)));
                replaceRevisionButton.setAlignmentY(Component.CENTER_ALIGNMENT);
                versionHistoryButtonPanel.add(replaceRevisionButton);
                versionHistoryTable = new JTable(new VersionHistoryTableModel(null));
                versionHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
                versionHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(20);
                versionHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new DateTableCellRenderer());
                versionHistoryTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
                JScrollPane scrollPane = new JScrollPane(versionHistoryTable);
                JPanel versionHistoryPanel = new JPanel();
                versionHistoryPanel.setLayout(new BoxLayout(versionHistoryPanel, BoxLayout.Y_AXIS));
                versionHistoryButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                versionHistoryPanel.add(versionHistoryButtonPanel);
                scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
                versionHistoryPanel.add(scrollPane);
                viewInfo.setComponent(versionHistoryPanel);
                viewInfo.setTitle("BaseX Version History");
            } else if ("Project".equals(viewInfo.getViewID())) {
                // Change the 'Project' view title.
                viewInfo.setTitle("CMS Project");
            }
        }
    }

}
