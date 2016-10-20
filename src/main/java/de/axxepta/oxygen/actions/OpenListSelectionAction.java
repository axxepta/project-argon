package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import ro.sync.exml.workspace.api.PluginWorkspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    JList<String> results;
    PluginWorkspace wsa;
    JDialog resultsDialog;

    public OpenListSelectionAction(String name, PluginWorkspace wsa, JList<String> results,
                                   JDialog resultsDialog) {
        super(name);
        this.results = results;
        this.wsa = wsa;
        this.resultsDialog = resultsDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<String> selectedResources = new ArrayList<>();
        selectedResources.addAll(results.getSelectedValuesList());

        for (String resource : selectedResources) {
            String db_path = TreeUtils.urlStringFromTreeString(resource);
            WorkspaceUtils.openURLString(db_path);
        }
        resultsDialog.dispose();
    }
}