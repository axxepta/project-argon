package de.axxepta.oxygen.versioncontrol;

import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Markus on 01.02.2016.
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final Date date = (Date) value;
        String dateString;
        SimpleDateFormat formatter;
        String currentLocaleStr = PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
        if (currentLocaleStr.equals("de_DE")) {
            String pattern = "dd.MM.yyyy, hh:mm";
            formatter = new SimpleDateFormat(pattern, Locale.GERMAN);
        } else {
            String pattern = "MMM d, yyyy, h:mm a";
            formatter = new SimpleDateFormat(pattern, Locale.UK);
        }
        dateString = formatter.format(date);
        setText(dateString);
        return this;
    }
}
