package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.BaseXRunQueryAction;
import de.axxepta.oxygen.actions.NewVersionButtonAction;
import de.axxepta.oxygen.actions.ReplyAuthorCommentAction;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.URLUtils;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ArgonToolbarComponentCustomizer implements ToolbarComponentsCustomizer {

    private ToolbarButton runQueryButton;
    private ToolbarButton newVersionButton;

    private final Action runBaseXQueryAction;
    private final Action newVersionAction;
    private final Action replyToAuthorComment;

    ArgonToolbarComponentCustomizer(StandalonePluginWorkspace pluginWorkspaceAccess) {
        super();
        runBaseXQueryAction = new BaseXRunQueryAction("Run BaseX Query",
                ImageUtils.createImageIcon("/images/RunQuery.png"), pluginWorkspaceAccess);
        newVersionAction = new NewVersionButtonAction("Increase File Version",
                ImageUtils.createImageIcon("/images/IncVersion.png"), pluginWorkspaceAccess);
        replyToAuthorComment = new ReplyAuthorCommentAction("Reply Author Comment",
                ImageUtils.createImageIcon("/images/ReplyComment.png"), pluginWorkspaceAccess);
    }

    @Override
    public void customizeToolbar(ToolbarInfo toolbarInfo) {

        ToolbarButton replyCommentButton;

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
            comps.add(new JSeparator(SwingConstants.VERTICAL));
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

    void checkEditorDependentMenuButtonStatus(PluginWorkspace pluginWorkspaceAccess){
        WSEditor currentEditor = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if(currentEditor == null) {
            runQueryButton.setEnabled(false);
            newVersionButton.setEnabled(false);
        } else {
            URL url = currentEditor.getEditorLocation();
            if (URLUtils.isArgon(url)) {
                newVersionButton.setEnabled(true);
            } else {
                newVersionButton.setEnabled(false);
            }
            if (URLUtils.isQuery(currentEditor.getEditorLocation())) {
                runQueryButton.setEnabled(true);
            } else {
                runQueryButton.setEnabled(false);
            }
        }
    }

}
