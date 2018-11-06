package de.axxepta.oxygen.workspace;

import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ArgonToolbarComponentCustomizer implements ToolbarComponentsCustomizer {

    private static final String ARGON_WORKSPACE_ACCESS_TOOLBAR_ID = "ArgonWorkspaceAccessToolbarID";

    private final ToolbarButton runQueryButton;
    private final ToolbarButton newVersionButton;
    private final ToolbarButton saveToArgonButton;
    private final ToolbarButton replyCommentButton;

    ArgonToolbarComponentCustomizer(ToolbarButton runQueryButton, ToolbarButton newVersionButton,
                                    ToolbarButton saveToArgonButton, ToolbarButton replyCommentButton) {
        this.runQueryButton = runQueryButton;
        this.newVersionButton = newVersionButton;
        this.saveToArgonButton = saveToArgonButton;
        this.replyCommentButton = replyCommentButton;
    }

    @Override
    public void customizeToolbar(ToolbarInfo toolbarInfo) {

        addButtons(ARGON_WORKSPACE_ACCESS_TOOLBAR_ID,
                new JComponent[]{runQueryButton, new JSeparator(SwingConstants.VERTICAL), newVersionButton},
                toolbarInfo, -1);

        // Set title
        if (ARGON_WORKSPACE_ACCESS_TOOLBAR_ID.equals(toolbarInfo.getToolbarID()))
            toolbarInfo.setTitle("BaseX DB Connection");

        addButtons("File", new JComponent[]{saveToArgonButton}, toolbarInfo, 2);
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


}
