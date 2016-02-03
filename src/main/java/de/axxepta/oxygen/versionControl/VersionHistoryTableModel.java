package de.axxepta.oxygen.versioncontrol;

import javax.swing.table.AbstractTableModel;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 01.02.2016.
 */

public class VersionHistoryTableModel extends AbstractTableModel {

    public static final String[] COLUMN_NAMES = {"Version", "Revision", "Date"};

    private List<VersionHistoryEntry> data;

    public VersionHistoryTableModel(List<VersionHistoryEntry> data) {
        this.data = new ArrayList<>();
        if (data != null)
            this.data.addAll(data);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= getRowCount())
            throw new IllegalArgumentException("Row argument out of bounds.");
        if (columnIndex >= getColumnCount())
            throw new IllegalArgumentException("Column argument out of bounds.");
        return data.get(rowIndex).getDisplayVector()[columnIndex];
    }

    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex >= getColumnCount())
            throw new IllegalArgumentException("Column argument out of bounds.");
        return COLUMN_NAMES[columnIndex];
    }

    public void setNewContent(List<VersionHistoryEntry> newData) {
        data.clear();
        data.addAll(newData);
        fireTableDataChanged();
    }

    public URL getURL(int rowIndex) {
        if (rowIndex >= getRowCount())
            throw new IllegalArgumentException("Row argument out of bounds.");
        return data.get(rowIndex).getURL();
    }
}
