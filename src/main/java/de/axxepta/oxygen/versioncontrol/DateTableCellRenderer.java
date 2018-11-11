package de.axxepta.oxygen.versioncontrol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Markus on 01.02.2016.
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer {

    private static final Logger logger = LogManager.getLogger(DateTableCellRenderer.class);

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        //final LocalDateTime date = (LocalDateTime) value;
        final Date date = (Date) value;

        final SimpleDateFormat formatter;
        //final DateTimeFormatter formatter;
        final String currentLocaleStr = PluginWorkspaceProvider.getPluginWorkspace().getUserInterfaceLanguage();
        if (currentLocaleStr.equals("de_DE")) {
           // formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, hh:mm").withLocale(Locale.GERMAN);
            formatter = new SimpleDateFormat("dd.MM.yyyy, hh:mm", Locale.GERMAN);
        } else {
           // formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a").withLocale(Locale.UK);
            formatter = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.UK);
        }
        //final String dateString = date.format(formatter);
        final String dateString = formatter.format(date);
        return super.getTableCellRendererComponent(table, dateString, isSelected, hasFocus, row, column);
    }
}
