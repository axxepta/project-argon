package de.axxepta.oxygen.actions;

import java.awt.*;

/**
 * @author Markus on 01.11.2015.
 */
public class DialogTools {

    public static void CenterDialogRelativeToParent(Dialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Container parent = dialog.getParent();
        Dimension parentSize = parent.getSize();

        int dx = (parentSize.width - dialogSize.width) / 2;
        int dy = (parentSize.height - dialogSize.height) / 2;

        dialog.setLocation(parent.getX()+dx, parent.getY()+dy);

    }

}
