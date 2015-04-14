package de.axxepta.oxygen.tree;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.logging.Logger;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class TreeListener extends MouseAdapter {
    private JTree _Tree;
    private boolean singleClick  = true;
    private int doubleClickDelay = 300;
    private Timer timer;

    public TreeListener(JTree tree)
    {
        this._Tree = tree;
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                timer.stop();
                if (singleClick) {
                    singleClickHandler(e);
                } else {
                    try {
                        doubleClickHandler(e);
                    } catch (ParseException ex) {
                        Logger.getLogger(ex.getMessage());
                    }
                }
            }
        };
        timer = new javax.swing.Timer(doubleClickDelay, actionListener);
        timer.setRepeats(false);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            singleClick = true;
            timer.start();
        } else {
            singleClick = false;
        }
    }

    private void singleClickHandler(ActionEvent e) {
        System.out.println("-- single click --");
    }

    private void doubleClickHandler(ActionEvent e) throws ParseException {
        System.out.println("-- double click -- id=");

    }
}
