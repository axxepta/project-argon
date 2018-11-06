package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author Markus on 11.07.2016.
 */
// made public for access via AspectJ
@SuppressWarnings("all")
public class OpenListSelectionAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(OpenListSelectionAction.class);
    private static final PluginWorkspace wsa = PluginWorkspaceProvider.getPluginWorkspace();

    JList<String> results;
    JDialog resultsDialog;

    public OpenListSelectionAction(String name, JList<String> results, JDialog resultsDialog) {
        super(name);
        this.results = results;
        this.resultsDialog = resultsDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<String> selectedResources = new ArrayList<>();
        selectedResources.addAll(results.getSelectedValuesList());

        for (String resource : selectedResources) {
            WorkspaceUtils.openURLString(resource);
        }
        resultsDialog.dispose();
    }
}
