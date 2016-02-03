package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Markus on 27.10.2015.
 */
public class NewFileListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String fileType = value.toString();
        int ind1 = fileType.indexOf("(");
        int ind2 = fileType.indexOf(")");
        String ext = fileType.substring(ind1+3, ind2);
        Icon icon = ImageUtils.getIcon(ext);
        label.setIcon(icon);
        return label;
    }

}
