package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.tree.ArgonTreeCellRenderer;
import de.axxepta.oxygen.utils.ImageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * @author Markus on 27.07.2016.
 */
public class ArgonChooserListCellRenderer implements ListCellRenderer {

    private static final Logger logger = LogManager.getLogger(ArgonChooserListCellRenderer.class);
    private static final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        ArgonChooserListModel.Element element = (ArgonChooserListModel.Element) value;

        switch (element.getType()) {
            case DB_BASE:
                renderer.setIcon(ImageUtils.getIcon(ImageUtils.DB_HTTP));
                break;
            case DB:
                renderer.setIcon(ImageUtils.getIcon(ImageUtils.DB_CATALOG));
                break;
            case REPO:
//            case XQ:
                renderer.setIcon(ImageUtils.getIcon(ImageUtils.DB_FOLDER));
                break;
            case DIR:
            case ROOT:
                renderer.setIcon(ImageUtils.getIcon(ImageUtils.FOLDER));
                break;
            default:
                final String thisItemType = ArgonTreeCellRenderer.fileType(element.getName());
                renderer.setIcon(ImageUtils.getIcon(thisItemType));
        }
        renderer.setText(element.getName());

        return renderer;
    }
}
