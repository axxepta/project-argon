package de.axxepta.oxygen.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author by Markus on 21.10.2015.
 */
 public class CloseDialogAction extends AbstractAction {

    JDialog dialog;

    protected CloseDialogAction(String name, JDialog dialog){
        super(name);
        this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dialog.dispose();
    }
}
