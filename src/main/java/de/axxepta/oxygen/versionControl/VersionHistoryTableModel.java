package de.axxepta.oxygen.versioncontrol;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author Markus on 01.02.2016.
 */
public class VersionHistoryTableModel extends AbstractTableModel {

    public static final String[] COLUMN_NAMES = {"Version", "Revision", "Date"};

    private final List<VersionHistoryEntry> data;

    public VersionHistoryTableModel(List<VersionHistoryEntry> data) {
        this.data = data;
    }

    @Override
    public int getRowCount() {
        if (data != null)
            return data.size();
        else
            return 0;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (data != null)
            return data.get(rowIndex).getDisplayVector()[columnIndex];
        else
            return "";
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }
}
