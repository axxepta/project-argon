package de.axxepta.oxygen.workspace;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jidesoft.swing.JideScrollPane;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.InputURLChooser;
import ro.sync.exml.workspace.api.standalone.InputURLChooserCustomizer;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefTargetInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefTargetInfoProvider;
import ro.sync.exml.workspace.api.standalone.ui.Menu;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.exml.workspace.api.util.RelativeReferenceResolver;
import ro.sync.ui.Icons;

/**
 * Plugin extension - workspace access extension.
 */
public class CustomWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {
    /**
     * Map between the URL of the temporary local file that contains the content of the
     * checked out file and the URL of the checked out file.
     */
    private Map<URL, URL> openedCheckedOutUrls = new HashMap<URL, URL>();

    /**
     * If <code>true</code> the editor will be verified for Check In on close.
     */
    private boolean verifyCheckInOnClose = true;

    /**
     * If <code>true</code> the document is Checked Out, it will be Checked In on
     * close.
     */
    private boolean forceCheckIn;

    /**
     * The CMS messages area.
     */
    private JTextArea cmsMessagesArea;

    /**
     * Plugin workspace access.
     */
    private StandalonePluginWorkspace pluginWorkspaceAccess;


//  /**
//   * The Oxygen menu bar. You can set this field on startup
//   * and then provide custom actions or filters when a certain file is opened.
//   */
//  private JMenuBar oxygenMenuBar;

    /**
     * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
     */
    @Override
    public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

        pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;

        // Check In action
        @SuppressWarnings("serial")
        final Action checkInAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);

                if (editorAccess != null) {
                    URL tempFileUrl = editorAccess.getEditorLocation();
                    // Get the corresponding URL
                    URL checkedOutUrl = openedCheckedOutUrls.get(tempFileUrl);
                    if (checkedOutUrl == null) {
                        pluginWorkspaceAccess.showInformationMessage("The file is not Checked Out.");
                    } else {
                        checkInFile(pluginWorkspaceAccess, editorAccess, tempFileUrl, checkedOutUrl, true);
                    }
                }
                // Show 'CMS Messages' view
                pluginWorkspaceAccess.showView("ArgonWorkspaceAccessID", true);
            }
        };

        // Check Out action
        @SuppressWarnings("serial")
        final Action checkOutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                File currentFileContext = null;
                // Create temporary file
                String defaultCheckOutDirString = pluginWorkspaceAccess.getOptionsStorage().getOption(
                        CustomWorkspaceAccessOptionPagePluginExtension.KEY_DEFAULT_CHECKOUT_LOCATION,
                        null);
                if (defaultCheckOutDirString != null) {
                    currentFileContext = new File(defaultCheckOutDirString);
                }
                File checkedOutFile = pluginWorkspaceAccess.chooseFile(currentFileContext, "Choose file", new String[] {"xml"}, "XML Files", false);
                if (checkedOutFile != null) {
                    try {
                        // Create temporary file from the checked out file.
                        File tempFile = createTempFromCheckedOutFile(checkedOutFile);
                        URL tempUrl = new URL(pluginWorkspaceAccess.getUtilAccess().correctURL(tempFile.toURI().toString()));
                        // Add the URls pair in the internal map
                        URL checkedoutUrl = new URL(pluginWorkspaceAccess.getUtilAccess().correctURL(checkedOutFile.toURI().toString()));
                        openedCheckedOutUrls.put(tempUrl, checkedoutUrl);

                        // Open the temporary file
                        pluginWorkspaceAccess.open(tempUrl);

                        if (cmsMessagesArea != null) {
                            String messages = cmsMessagesArea.getText() + "\n" +
                                    "Check Out " + checkedoutUrl.toString();
                            cmsMessagesArea.setText(messages);
                        }
                    } catch (Exception e) {
                        pluginWorkspaceAccess.showErrorMessage("Check Out operation failed: " + e.getMessage());
                    }
                }
                // Show 'CMS Messages' view
                pluginWorkspaceAccess.showView("SampleWorkspaceAccessID", true);
            }
        };

        // Show Selection Source action
        @SuppressWarnings("serial")
        final Action selectionSourceAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionevent) {
                WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
                // The action is available only in Author mode.
                if (editorAccess != null && EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())) {
                    WSAuthorEditorPage authorPageAccess = (WSAuthorEditorPage) editorAccess.getCurrentPage();
                    AuthorDocumentController controller = authorPageAccess.getDocumentController();
                    if (authorPageAccess.hasSelection()) {
                        AuthorDocumentFragment selectionFragment;
                        try {
                            // Create fragment from selection
                            selectionFragment = controller.createDocumentFragment(
                                    authorPageAccess.getSelectionStart(),
                                    authorPageAccess.getSelectionEnd() - 1
                            );
                            // Serialize
                            String serializeFragmentToXML = controller.serializeFragmentToXML(selectionFragment);
                            // Show fragment
                            pluginWorkspaceAccess.showInformationMessage(serializeFragmentToXML);
                        } catch (BadLocationException e) {
                            pluginWorkspaceAccess.showErrorMessage("Show Selection Source operation failed: " + e.getMessage());
                        }
                    } else {
                        // No selection
                        pluginWorkspaceAccess.showInformationMessage("No selection available.");
                    }
                }
            }
        };

        // Surround with <important> action
        @SuppressWarnings("serial")
        final Action surroundWith = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess.getCurrentPage() instanceof WSTextEditorPage) {
                    WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
                    int selectionStart = textPage.getSelectionStart();
                    int selectionEnd = textPage.getSelectionEnd();
                    try {
                        textPage.beginCompoundUndoableEdit();
                        // Insert the start tag
                        textPage.getDocument().insertString(selectionStart, "<important>", null);
                        // Insert the end tag
                        textPage.getDocument().insertString(selectionEnd + "<important>".length(), "</important>", null);
                        textPage.endCompoundUndoableEdit();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {
            // Sample of using method getOxygenActionID() from StandalonePluginWorkspace
            // Instead of reverting the file , save it under other name
//      private Action revertAction;
//      private Action saveAsAction = null;

            /**
             * @see ro.sync.exml.workspace.api.standalone.MenuBarCustomizer#customizeMainMenu(javax.swing.JMenuBar)
             */
            @Override
            public void customizeMainMenu(JMenuBar mainMenuBar) {
//        oxygenMenuBar = mainMenuBar;
                // CMS menu
                JMenu menuCMS = createCMSMenu(checkInAction, checkOutAction, selectionSourceAction, surroundWith);
                // Add the CMS menu before the Help menu
                mainMenuBar.add(menuCMS, mainMenuBar.getMenuCount() - 1);

                // Sample of using method getOxygenActionID() from StandalonePluginWorkspace
                // Instead of reverting the file , save it under other name

//        revertAction = null;
//        String fileRevertID = "File/Revert";
//        String saveAsID = "File/File_Save_As";
//        String actionName = null;
//
//        int menuCount = mainMenuBar.getMenuCount();
//        // Iterate over menus to find the revert and Save As actions
//        menuLabel :for (int i = 0; i < menuCount; i++) {
//          // Revert action index in menu
//          int revertActionIndex = 0;
//          JMenu menu = mainMenuBar.getMenu(i);
//          int itemCount = menu.getItemCount();
//          for (int j = 0; j < itemCount; j++) {
//            JMenuItem item = menu.getItem(j);
//            if (item != null) {
//              Action action = item.getAction();
//              String oxygenActionID = pluginWorkspaceAccess.getOxygenActionID(action);
//              if (fileRevertID.equals(oxygenActionID)) {
//                revertAction = action;
//                revertActionIndex = j;
//                actionName = (String) revertAction.getValue(Action.NAME);
//              }
//              if (saveAsID.equals(oxygenActionID)) {
//                saveAsAction = action;
//              }
//
//              if (revertAction != null  && saveAsAction != null) {
//                JMenuItem revertMenuItem = menu.getItem(revertActionIndex);
//                // Replace Revert action with Save As action
//                revertMenuItem.setAction(new AbstractAction(actionName) {
//
//                  public void actionPerformed(ActionEvent e) {
//                    saveAsAction.actionPerformed(e);
//                  }
//                });
//                break menuLabel;
//              }
//            }
//          }
//        }
            }
        });


        //Add an additional browse action to all dialogs/places where Oxygen allows selecting an URL.
        pluginWorkspaceAccess.addInputURLChooserCustomizer(new InputURLChooserCustomizer() {
            @Override
            public void customizeBrowseActions(List<Action> existingBrowseActions, final InputURLChooser chooser) {
                //IMPORTANT, you also need to set a custom icon on the action for situations when its text is not used for display.
                ImageIcon imageIcon = null;
                java.io.InputStream inputStream = null;
                try {

                    inputStream = getClass().getResourceAsStream("/browseCMS.png");
                    imageIcon = new ImageIcon(ImageIO.read(inputStream));

                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                @SuppressWarnings("serial")
                Action browseCMS = new AbstractAction("CMS", imageIcon) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File checkedOutFile = pluginWorkspaceAccess.chooseFile("Choose file", new String[] {"xml"}, "XML Files");
                        if (checkedOutFile != null) {
                            try {
                                chooser.urlChosen(new URL(pluginWorkspaceAccess.getUtilAccess().correctURL(checkedOutFile.toURI().toURL().toString())));
                            } catch (MalformedURLException e1) {
                                //
                            }
                        }
                    }
                };
                // Add the CMS action on the first position.
                existingBrowseActions.add(0, browseCMS);
            }
        });

        //Add a custom relative reference resolver for your custom protocol.
        //Usually when inserting references from one URL to another Oxygen makes the inserted path relative.
        //If your custom protocol needs special relativization techniques then it should set up a custom relative
        //references resolver to be notified when resolving needs to be done.
        pluginWorkspaceAccess.addRelativeReferencesResolver(
                //Your custom URL protocol for which you already have a custom URLStreamHandlerPluginExtension set up.
                "cms",
                //The relative references resolver
                new RelativeReferenceResolver() {
                    @Override
                    public String makeRelative(URL baseURL, URL childURL) {
                        //Return the referenced path as absolute for example.
                        //return childURL.toString();
                        //Or return null for the default behavior.
                        return null;
                    }
                });

        pluginWorkspaceAccess.addEditorChangeListener(
                new WSEditorChangeListener() {
                    /**
                     * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorAboutToBeOpenedVeto(java.net.URL)
                     */
                    @Override
                    public boolean editorAboutToBeOpenedVeto(URL editorLocation) {
                        //Sample of rejecting opening one URL and opening instead another one.
//            String urlString = editorLocation.toString();
//            if (urlString.contains("personal.xml")) {
//                final String newUrlString = urlString.replace("personal.xml", "personal.xsd");
//                Thread openThread = new Thread(new Runnable() {
//                  @Override
//                  public void run() {
//                    try {
//                      logger.info("Opening on thread " + newUrlString);
//                      pluginWorkspaceAccess.open(new URL(newUrlString));
//                      logger.info("Opened success on thread " + newUrlString);
//                    } catch (MalformedURLException e) {
//                      logger.error("Could not open " + newUrlString, e);
//                    }
//                  }
//                }, "open-second-file");
//                openThread.start();
//            } else {
//              logger.info("Just open directly " + urlString);
//            }

                        //You can reject here the opening of an URL if you want
                        //              if(editorLocation != null && editorLocation.getProtocol().equals("cms")) {
                        //                return false;
                        //              }
                        return true;
                    }
                    @Override
                    public void editorOpened(URL editorLocation) {
                        checkActionsStatus(editorLocation);
                        customizePopupMenu();
                        // Show 'Edit' toolbar
                        pluginWorkspaceAccess.showToolbar("Edit");
                    }

                    /**
                     * The current author page which has been customizer
                     */
                    private WSAuthorEditorPage currentCustomizedAuthorPageAccess;
                    /**
                     * The pop-up menu customizer which has been set on it
                     */
                    private AuthorPopupMenuCustomizer authorPopupMenuCustomizer;

                    // Customize popup menu
                    private void customizePopupMenu() {
                        if(currentCustomizedAuthorPageAccess != null && authorPopupMenuCustomizer != null) {
                            //Remove the old popup menu customizer in order to avoid adding two customizers on the same page from the same plugin.
                            currentCustomizedAuthorPageAccess.removePopUpMenuCustomizer(authorPopupMenuCustomizer);
                        }
                        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
                        // Customize menu for Author page
                        if (editorAccess != null && EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())) {
                            currentCustomizedAuthorPageAccess = (WSAuthorEditorPage) editorAccess.getCurrentPage();
                            authorPopupMenuCustomizer = new AuthorPopupMenuCustomizer() {
                                // Customize popup menu
                                @SuppressWarnings("serial")
                                @Override
                                public void customizePopUpMenu(Object popUp, AuthorAccess authorAccess) {
                                    // CMS menu
                                    JMenu menuCMS = createCMSMenu(checkInAction, checkOutAction, selectionSourceAction, surroundWith);
                                    // Add the CMS menu
                                    ((JPopupMenu)popUp).add(menuCMS, 0);
                                    // Add 'Open in external application' action

                                    final URL selectedUrl;
                                    try {
                                        final String selectedText = authorAccess.getEditorAccess().getSelectedText();
                                        if (selectedText != null) {
                                            selectedUrl = new URL(selectedText);
                                            // Open selected url in system application
                                            ((JPopupMenu)popUp).add(new JMenuItem(new AbstractAction("Open in system application") {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    pluginWorkspaceAccess.openInExternalApplication(selectedUrl, true);
                                                }
                                            }), 0);
                                        }
                                    } catch (MalformedURLException e) {}
                                }
                            };
                            currentCustomizedAuthorPageAccess.addPopUpMenuCustomizer(authorPopupMenuCustomizer);
                        }
                    }

                    // Check actions status
                    private void checkActionsStatus(URL editorLocation) {
                        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
                        if (editorAccess != null) {
                            selectionSourceAction.setEnabled(EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID()));
                        }
                        URL checkedOutUrl = openedCheckedOutUrls.get(editorLocation);
                        checkInAction.setEnabled(checkedOutUrl != null);
                    }

                    @Override
                    public void editorClosed(URL editorLocation) {
                        URL checkedOutUrl = openedCheckedOutUrls.get(editorLocation);
                        if (checkedOutUrl != null) {
                            openedCheckedOutUrls.remove(editorLocation);
                        }
                    }

                    /**
                     * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorAboutToBeClosed(java.net.URL)
                     */
                    @Override
                    public boolean editorAboutToBeClosed(URL editorLocation) {
                        URL checkedOutUrl = openedCheckedOutUrls.get(editorLocation);
                        if(checkedOutUrl != null) {
                            if (verifyCheckInOnClose) {
                                if (forceCheckIn) {
                                    // Save the current file.
                                    checkInFile(pluginWorkspaceAccess, null, editorLocation, checkedOutUrl, false);
                                } else if(pluginWorkspaceAccess.showConfirmDialog(
                                        "Close",
                                        "The closed file " + editorLocation + " is Checked Out.\n Do you want to Check In?",
                                        new String[] {"Ok", "Cancel"},
                                        new int[] {0, 1}) == 0) {
                                    // Save the current file.
                                    checkInFile(pluginWorkspaceAccess, null, editorLocation, checkedOutUrl, false);
                                } else {
                                    //Reject the close, user did not want to check in.
                                    return false;
                                }
                            }
                        }
                        return true;
                    }

                    /**
                     * The editor was relocated (Save as was called).
                     *
                     * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorRelocated(java.net.URL, java.net.URL)
                     */
                    @Override
                    public void editorRelocated(URL previousEditorLocation, URL newEditorLocation) {
                        //Refresh the mappings.
                        URL previousCheckedOutUrl = openedCheckedOutUrls.get(previousEditorLocation);
                        if(previousCheckedOutUrl != null) {
                            openedCheckedOutUrls.remove(previousEditorLocation);
                            openedCheckedOutUrls.put(newEditorLocation, previousCheckedOutUrl);
                        }
                    }

                    @Override
                    public void editorPageChanged(URL editorLocation) {
                        checkActionsStatus(editorLocation);
                        customizePopupMenu();
                    }

                    @Override
                    public void editorSelected(URL editorLocation) {
                        checkActionsStatus(editorLocation);
                        customizePopupMenu();

                        //Sample of removing entries from a menu which is only shown for the selected editor.
//          // Remove Document menu.
//          for (int i = 0; i < oxygenMenuBar.getMenuCount(); i++) {
//            JMenu menu = oxygenMenuBar.getMenu(i);
//            String menuName = menu.getActionCommand();
//            logger.info("===== menuName " + menuName + "   " + i);
//            if ("Document".equals(menuName)) {
//              oxygenMenuBar.remove(menu);
//              logger.info("===== removed menu " + menuName);
//              break;
//            }
//          }
                    }

                    @Override
                    public void editorActivated(URL editorLocation) {
                        checkActionsStatus(editorLocation);
                        customizePopupMenu();
                    }
                },
                StandalonePluginWorkspace.MAIN_EDITING_AREA);


        pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
             */
            @SuppressWarnings("serial")
            @Override
            public void customizeToolbar(ToolbarInfo toolbarInfo) {
                //The toolbar ID is defined in the "plugin.xml"
                if("SampleWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
                    List<JComponent> comps = new ArrayList<JComponent>();
                    JComponent[] initialComponents = toolbarInfo.getComponents();
                    boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0;
                    if (hasInitialComponents) {
                        // Add initial toolbar components
                        for (JComponent toolbarItem : initialComponents) {
                            comps.add(toolbarItem);
                        }
                    }

                    // Check In
                    ToolbarButton checkInButton = new ToolbarButton(checkInAction, true);
                    checkInButton.setText("Check In");

                    // Check Out
                    ToolbarButton checkOutButton = new ToolbarButton(checkOutAction, true);
                    checkOutButton.setText("Check Out");

                    // Show Selection Source
                    ToolbarButton selectionSourceButton = new ToolbarButton(selectionSourceAction, true);
                    selectionSourceButton.setText("Show Selection Source");

                    // Add in toolbar
                    comps.add(checkInButton);
                    comps.add(checkOutButton);
                    comps.add(selectionSourceButton);
                    toolbarInfo.setComponents(comps.toArray(new JComponent[0]));

                    // Set title
                    String initialTitle = toolbarInfo.getTitle();
                    String title  = "";
                    if (hasInitialComponents && initialTitle != null && initialTitle.trim().length() > 0) {
                        // Include initial tile
                        title += initialTitle + " | " ;
                    }
                    title  += "CMS";
                    toolbarInfo.setTitle(title);
                } else if("Author_custom_actions1".equals(toolbarInfo.getToolbarID())) {
                    //Contribute a new action directly in the Author toolbar which was dynamically created from the document type
                    //associated to the XML file. You can add a new action or remove an existing one.
                    //See the Javadoc for:
                    //ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace.addToolbarComponentsCustomizer(ToolbarComponentsCustomizer)
                    List<JComponent> comps = new ArrayList<JComponent>(Arrays.asList(toolbarInfo.getComponents()));
                    comps.add(new ToolbarButton(new AbstractAction("MY ACTION") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //You can obtain the current editor, get access to its WSAuthorPage and modify it using the API.
                            System.err.println("Perform action on " + pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA).getEditorLocation());
                        }
                    }, true));
                    toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
                }
            }
        });

        pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
            /**
             * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
             */
            @Override
            public void customizeView(ViewInfo viewInfo) {
                if(
                    //The view ID defined in the "plugin.xml"
                    "SampleWorkspaceAccessID".equals(viewInfo.getViewID())) {


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


        //Add a DITA Map Topic Ref resolver.
        pluginWorkspaceAccess.addTopicRefTargetInfoProvider("cms", new TopicRefTargetInfoProvider() {
            @Override
            public void computeTopicRefTargetInfo(
                    Map<TopicRefInfo, TopicRefTargetInfo> ditaMapTargetReferences) {
                Iterator<TopicRefInfo> keys = ditaMapTargetReferences.keySet().iterator();
                while(keys.hasNext()) {
                    //Here you can see for what "href" the target information should be resolved
                    TopicRefInfo ti = keys.next();
                    TopicRefTargetInfo target = ditaMapTargetReferences.get(ti);
                    String href = (String) ti.getProperty(TopicRefInfo.HREF_VALUE);
                    //The plugin will handle this reference
                    target.setProperty(TopicRefTargetInfo.RESOLVED, "true");
                    //And then resolve the different properties from the CMS Metadata
                    //The title from the target topic
                    target.setProperty(TopicRefTargetInfo.TITLE, href + " TEST");
                    //The class attribute of the target element name
                    target.setProperty(TopicRefTargetInfo.CLASS_VALUE, " topic/topic ");
                    //The target element name
                    target.setProperty(TopicRefTargetInfo.ELEMENT_NAME, "topic");
                    //A parse error if something happened retrieving the data (maybe the target does not exist).
                    target.setProperty(TopicRefTargetInfo.PARSE_ERROR, null);
                }
            }
        });


        //Uncomment to set a custom JPop-up menu to each DITA Map opened in the DITA Maps Manager.
        //      pluginWorkspaceAccess.addEditorChangeListener(new WSEditorChangeListener() {
        //        /**
        //         * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorOpened(java.net.URL)
        //         */
        //        @Override
        //        public void editorOpened(URL editorLocation) {
        //          WSEditor ditaMap = pluginWorkspaceAccess.getEditorAccess(editorLocation, StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        //          WSDITAMapEditorPage edPage = (WSDITAMapEditorPage) ditaMap.getCurrentPage();
        //          final JTree ditaMapTree = (JTree) edPage.getDITAMapTreeComponent();
        //          edPage.setPopUpMenuCustomizer(new DITAMapPopupMenuCustomizer() {
        //            public void customizePopUpMenu(Object popUp, final AuthorDocumentController ditaMapDocumentController) {
        //              JPopupMenu popUpMenu = (JPopupMenu) popUp;
        //              popUpMenu.add(new AbstractAction("Set Custom Href") {
        //                public void actionPerformed(ActionEvent e) {
        //                  TreePath selPath = ditaMapTree.getSelectionPath();
        //                  if(selPath != null) {
        //                    AuthorElement selElement = (AuthorElement) selPath.getLastPathComponent();
        //                    ditaMapDocumentController.setAttribute("href", new AttrValue("testValue"), selElement);
        //                  }
        //                }
        //              });
        //            }
        //          });
        //        }
        //      }, StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
    }

    /**
     * Create CMS menu that contains the following actions:
     * <code>Check In</code>, <code>Check Out</code> and <code>Show Selection Source<code/>, <code>Surround with</code>
     *
     * @param checkInAction The check in action.
     * @param checkOutAction The check out action.
     * @param selectionSourceAction The selection source action.
     * @param surroundWithAction The surround action.
     *
     * @return The CMS menu.
     */
    private JMenu createCMSMenu(
            final Action checkInAction,
            final Action checkOutAction,
            final Action selectionSourceAction,
            final Action surroundWithAction) {
        // CMS menu
        Menu menuCMS = new Menu("CMS", true);

        // Add Check In action on the menu
        final JMenuItem checkInItem = new JMenuItem(checkInAction);
        checkInItem.setText("Check In");
        menuCMS.add(checkInItem);

        // Add Check Out action on the menu
        JMenuItem checkOutItem = new JMenuItem(checkOutAction);
        checkOutItem.setText("Check Out");
        menuCMS.add(checkOutItem);

        // Add Show Section Source action on the menu
        JMenuItem selectionSourceItem = new JMenuItem(selectionSourceAction);
        selectionSourceItem.setText("Show Selection Source");
        menuCMS.add(selectionSourceItem);

        // Surround with "<important>" action
        JMenuItem surroundWithItem = new JMenuItem(surroundWithAction);
        surroundWithItem.setText("Surround with <important>");
        menuCMS.add(surroundWithItem);

        return menuCMS;
    }

    /**
     * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
     */
    @Override
    public boolean applicationClosing() {
        if (!openedCheckedOutUrls.isEmpty()) {
            int result = pluginWorkspaceAccess.showConfirmDialog(
                    "Close",
                    "There are some opened Checked Out files.\n Do you want to Check In?",
                    new String[] {"Check In All", "Don't Check In", "Cancel"},
                    new int[] {0, 1, 2});
            // Check In
            if (result == 0) {
                verifyCheckInOnClose = true;
                forceCheckIn = true;
                // Don't Check In
            } else if (result == 1) {
                verifyCheckInOnClose = false;
                // Cancel
            } else if (result == 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create temporary file from checked out file.
     *
     * @param checkedOutFile The checked out file.
     * @return Temporary file.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private File createTempFromCheckedOutFile(File checkedOutFile) throws IOException, FileNotFoundException {
        int indexOfPoint = checkedOutFile.getName().lastIndexOf('.');
        String fileName =  checkedOutFile.getName();
        // Temporary file name
        String tempFileName = indexOfPoint > -1 ? fileName.substring(0, indexOfPoint) : fileName;
        // Temporary file extension
        String fileExtension = indexOfPoint > -1 ? fileName.substring(indexOfPoint) : null;

        // Create temporary file
        String tmpDirString = pluginWorkspaceAccess.getOptionsStorage().getOption(
                CustomWorkspaceAccessOptionPagePluginExtension.KEY_SAVE_TEMPORARY_FILES_LOCATION,
                null);
        File tmpDir = checkedOutFile.getParentFile();
        if (tmpDirString != null) {
            tmpDir = new File(tmpDirString);
        }
        File tempFile = File.createTempFile("cms_oxy" + tempFileName, fileExtension, tmpDir);

        // Write the content
        copyFileContent(checkedOutFile, tempFile);

        return tempFile;
    }

    /**
     * Copy content from one file to another.
     *
     * @param initialFile The initial file to copy the content from.
     * @param destinationFile The destination.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void copyFileContent(File initialFile, File destinationFile) throws FileNotFoundException,
            IOException {
        FileInputStream fis = new FileInputStream(initialFile);
        FileOutputStream fos = new FileOutputStream(destinationFile);
        // Write the content
        int b;
        while( (b = fis.read()) != -1){
            fos.write(b);
        }
        fos.close();
        fis.close();
    }

    /**
     * Check In file.
     *
     * @param pluginWorkspaceAccess The plugin workspace access
     * @param editorAccess The editor access.
     * @param tempFileUrl The temporary local file URL.
     * @param checkedOutUrl The URL of the corresponding checked out file
     */
    private void checkInFile(final StandalonePluginWorkspace pluginWorkspaceAccess,
                             WSEditor editorAccess, URL tempFileUrl, URL checkedOutUrl, boolean openFile) {
        boolean checkIn = true;
        // Verify if the editor is modified
        if (editorAccess != null && editorAccess.isModified()) {
            // Ask to save
            if (pluginWorkspaceAccess.showConfirmDialog(
                    "Save",
                    "You must save the file in order to Check In.",
                    new String[] {"Ok", "Cancel"},
                    new int[] {0, 1}) == 0) {
                // Save the current file.
                editorAccess.save();
            } else {
                // Cancel.
                checkIn = false;
                pluginWorkspaceAccess.showInformationMessage("Check In operation was canceled.");
            }
        }

        // Perform Check In ...
        if (checkIn) {
            try {
                File tempFile = new File(tempFileUrl.getFile());

                // Copy the content of the temporary file over the original file
                copyFileContent(tempFile, new File(checkedOutUrl.getFile()));

                if (editorAccess != null) {
                    // Close temporary file editor
                    verifyCheckInOnClose = false;
                    editorAccess.close(false);
                    verifyCheckInOnClose = true;
                }

                // Delete temporary file
                tempFile.delete();

                if (openFile) {
                    // Open the checked out file
                    pluginWorkspaceAccess.open(checkedOutUrl);
                    pluginWorkspaceAccess.showInformationMessage("Check In was performed.");
                    if (cmsMessagesArea != null) {
                        String messages = cmsMessagesArea.getText() + "\n" +
                                "Check In " + checkedOutUrl.toString();
                        cmsMessagesArea.setText(messages);
                    }
                }
            } catch (Exception e) {
                // Failed
                pluginWorkspaceAccess.showErrorMessage("Check In operation failed: " + e.getMessage());
            }
        }
    }
}

