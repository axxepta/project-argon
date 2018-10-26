package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.TreeUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author Markus on 11.07.2016.
 */

// made public for access via AspectJ
//@SuppressWarnings("all")
public class CheckOutListSelectionAction extends AbstractAction {

    private final JList<String> results;
    private final JDialog resultsDialog;

    public CheckOutListSelectionAction(String name, JList<String> results, JDialog resultsDialog) {
        super(name);
        this.results = results;
        this.resultsDialog = resultsDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final ArrayList<String> selectedResources = new ArrayList<>(results.getSelectedValuesList());

        for (Object resource : selectedResources) {
            final String db_path = TreeUtils.urlStringFromTreeString(resource.toString());
            CheckOutAction.checkOut(db_path);
        }
        resultsDialog.dispose();
    }
}
