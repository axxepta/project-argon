package de.axxepta.oxygen.versioncontrol;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Markus on 02.02.2016.
 */
public class VersionControlListSelectionListener implements ListSelectionListener {

    private final JTable table;
    private final JButton compareButton;
    private final JButton replaceButton;

    public VersionControlListSelectionListener(JTable table, JButton compareButton, JButton replaceButton) {
        this.table = table;
        this.compareButton = compareButton;
        this.replaceButton = replaceButton;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int nSelected = table.getSelectedRows().length;
        if ((nSelected == 1) || (nSelected == 2))
            compareButton.setEnabled(true);
        else
            compareButton.setEnabled(false);
        if (nSelected == 1)
            replaceButton.setEnabled(true);
        else
            replaceButton.setEnabled(false);
    }
}
