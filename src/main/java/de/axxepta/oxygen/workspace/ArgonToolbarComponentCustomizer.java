package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.actions.BaseXRunQueryAction;
import de.axxepta.oxygen.actions.NewVersionAction;
import de.axxepta.oxygen.actions.ReplyAuthorCommentAction;
import de.axxepta.oxygen.actions.SaveFileToArgonAction;
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
    private ToolbarButton saveToArgonButton;

    private final Action runBaseXQueryAction;
    private final Action newVersionAction;
    private final Action replyToAuthorComment;
    private final Action saveToArgonAction;

    ArgonToolbarComponentCustomizer(StandalonePluginWorkspace pluginWorkspaceAccess) {
        runBaseXQueryAction = new BaseXRunQueryAction("Run BaseX Query",
                ImageUtils.createImageIcon("/images/RunQuery.png"), pluginWorkspaceAccess);
        newVersionAction = new NewVersionAction("Increase File Version",
                ImageUtils.createImageIcon("/images/IncVersion.png"), pluginWorkspaceAccess);
        replyToAuthorComment = new ReplyAuthorCommentAction("Reply Author Comment",
                ImageUtils.createImageIcon("/images/ReplyComment.png"), pluginWorkspaceAccess);
        saveToArgonAction = new SaveFileToArgonAction("Save As with Argon Protocol",
                ImageUtils.createImageIcon("/images/AddFile16.gif"), pluginWorkspaceAccess);
    }

    @Override
    public void customizeToolbar(ToolbarInfo toolbarInfo) {

        ToolbarButton replyCommentButton;

        runQueryButton = new ToolbarButton(runBaseXQueryAction, true);
        runQueryButton.setText("");
        newVersionButton = new ToolbarButton(newVersionAction, true);
        newVersionButton.setText("");
        addButtons("ArgonWorkspaceAccessToolbarID",
                new JComponent[]{runQueryButton, new JSeparator(SwingConstants.VERTICAL), newVersionButton},
                toolbarInfo, -1);

        // Set title
        if ("ArgonWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
            JComponent[] initialComponents = toolbarInfo.getComponents();
            boolean hasInitialComponents = (initialComponents != null) && (initialComponents.length > 0);
            String initialTitle = toolbarInfo.getTitle();
            String title = "";
            if (hasInitialComponents && initialTitle != null && initialTitle.trim().length() > 0) {
                // Include initial tile
                title += initialTitle + " | ";
            }
            title += "BaseX DB";
            toolbarInfo.setTitle(title);
        }

        saveToArgonButton = new ToolbarButton(saveToArgonAction, true);
        saveToArgonButton.setText("");
        addButtons("File", new JComponent[]{saveToArgonButton}, toolbarInfo, 2);

        replyCommentButton = new ToolbarButton(replyToAuthorComment, true);
        replyCommentButton.setText("");
        addButtons("toolbar.review", new JComponent[]{replyCommentButton}, toolbarInfo, -1);
    }

    private void addButtons(String toolbarID, JComponent[] components, final ToolbarInfo toolbarInfo, int pos) {
        if (toolbarID.equals(toolbarInfo.getToolbarID())) {
            List<JComponent> comps = new ArrayList<>();
            JComponent[] initialComponents = toolbarInfo.getComponents();
            boolean hasInitialComponents = (initialComponents != null) && (initialComponents.length > 0);
            if (hasInitialComponents) {
                // Add initial toolbar components
                comps.addAll(Arrays.asList(initialComponents));
            }
            for (JComponent component : components) {
                if (pos == -1)
                    comps.add(component);
                else
                    comps.add(pos, component);
            }
            toolbarInfo.setComponents(comps.toArray(new JComponent[comps.size()]));
        }
    }

    void checkEditorDependentMenuButtonStatus(PluginWorkspace pluginWorkspaceAccess){
        WSEditor currentEditor = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);

        if(currentEditor == null) {
            runQueryButton.setEnabled(false);
            newVersionButton.setEnabled(false);
            saveToArgonButton.setEnabled(false);
        } else {
            saveToArgonButton.setEnabled(true);
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
