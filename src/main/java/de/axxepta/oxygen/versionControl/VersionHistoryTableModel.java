package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.utils.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Markus on 01.02.2016.
 */

public class VersionHistoryTableModel extends AbstractTableModel {

    private static final Logger logger = LogManager.getLogger(VersionHistoryTableModel.class);

    private static final String[] COLUMN_NAMES =
            {Lang.get(Lang.Keys.lbl_version), Lang.get(Lang.Keys.lbl_revision), Lang.get(Lang.Keys.lbl_date)};

    private List<VersionHistoryEntry> data;

    public VersionHistoryTableModel(List<VersionHistoryEntry> data) {
        this.data = data != null ? new ArrayList<>(data) : Collections.emptyList();
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
        if (rowIndex >= getRowCount()) {
            throw new IllegalArgumentException("Row argument out of bounds.");
        }
        if (columnIndex >= getColumnCount()) {
            throw new IllegalArgumentException("Column argument out of bounds.");
        }
        return data.get(rowIndex).getDisplayVector()[columnIndex];
    }

    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex >= getColumnCount())
            throw new IllegalArgumentException("Column argument out of bounds.");
        return COLUMN_NAMES[columnIndex];
    }

    public void setNewContent(List<VersionHistoryEntry> newData) {
        logger.info("setNewContent " + newData);
        data = new ArrayList<>(newData);
        fireTableDataChanged();
    }

    public URL getURL(int rowIndex) {
        if (rowIndex >= getRowCount()) {
            throw new IllegalArgumentException("Row argument out of bounds.");
        }
        return data.get(rowIndex).getURL();
    }
}
