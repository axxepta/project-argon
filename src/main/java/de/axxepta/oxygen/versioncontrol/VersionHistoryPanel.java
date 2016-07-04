package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.actions.CompareVersionsAction;
import de.axxepta.oxygen.actions.RollbackVersionAction;
import de.axxepta.oxygen.api.TopicHolder;

import javax.swing.*;
import java.awt.*;

/**
 * @author Markus on 04.07.2016.
 */
public class VersionHistoryPanel extends JPanel {

    public VersionHistoryPanel() {
        initView();
    }

    private void initView() {
        JTable versionHistoryTable;
        // Table (will be put in bottom Box)
        versionHistoryTable = new JTable(new VersionHistoryTableModel(null));
        versionHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        versionHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new DateTableCellRenderer());
        versionHistoryTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

        VersionHistoryUpdater versionHistoryUpdater = new VersionHistoryUpdater(versionHistoryTable);
        TopicHolder.changedEditorStatus.register(versionHistoryUpdater);

        // Two Buttons (with filler) in Pane in Top Box
        JButton compareRevisionsButton = new JButton(new CompareVersionsAction("Compare", versionHistoryTable));
        compareRevisionsButton.setEnabled(false);
        JButton replaceRevisionButton = new JButton(new RollbackVersionAction("Reset to", versionHistoryTable));
        replaceRevisionButton.setEnabled(false);
        JPanel versionHistoryButtonPanel = new JPanel();
        versionHistoryButtonPanel.setLayout(new BoxLayout(versionHistoryButtonPanel, BoxLayout.X_AXIS));
        compareRevisionsButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        versionHistoryButtonPanel.add(compareRevisionsButton);
        versionHistoryButtonPanel.add(new Box.Filler(
                new Dimension(10,10), new Dimension(20,10), new Dimension(50,10)));
        replaceRevisionButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        versionHistoryButtonPanel.add(replaceRevisionButton);

        // Table in ScrollPane in bottom Box
        versionHistoryTable.getSelectionModel().addListSelectionListener(
                new VersionControlListSelectionListener(versionHistoryTable, compareRevisionsButton, replaceRevisionButton));
        JScrollPane scrollPane = new JScrollPane(versionHistoryTable);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        versionHistoryButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(versionHistoryButtonPanel);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(scrollPane);
    }

}
