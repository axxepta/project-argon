package de.axxepta.oxygen.utils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Markus on 01.11.2015.
 */
// keep public for access via AspectJ
public class DialogTools {

    public static void CenterDialogRelativeToParent(Dialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Container parent = dialog.getParent();
        Dimension parentSize = parent.getSize();

        int dx = (parentSize.width - dialogSize.width) / 2;
        int dy = (parentSize.height - dialogSize.height) / 2;

        dialog.setLocation(parent.getX()+dx, parent.getY()+dy);
    }

    public static void wrapAndShow(JDialog dialog, JPanel content, JFrame parentFrame) {
        wrap(dialog, content, parentFrame);
        dialog.setVisible(true);
    }

    public static void wrapAndShow(JDialog dialog, JPanel content, JFrame parentFrame, int width, int height) {
        wrap(dialog, content, parentFrame);
        dialog.setSize(width, height);
        dialog.setVisible(true);
    }

    private static void wrap(JDialog dialog, JPanel content, JFrame parentFrame) {
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    public static JDialog getOxygenDialog(JFrame parentFrame, String title) {
        JDialog dialog = new JDialog(parentFrame, title);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        // ToDo: image from map
        dialog.setIconImage(ImageUtils.createImage("/images/Oxygen16.png"));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return dialog;
    }

}
