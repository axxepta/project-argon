package de.axxepta.oxygen.actions;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Markus on 02.11.2015.
 */
public class FileNameFieldListener implements DocumentListener {

    boolean textFieldResetInProgress;
    JTextField textField;
    final String fileNameChar;
    final String fileNameChars;

    public FileNameFieldListener(JTextField textField, boolean withExtension) {
        this.textField = textField;
        if (withExtension) {
            fileNameChar = "\\w|_|-|\\.";
            fileNameChars = "(\\w|_|-|\\.)*";
        } else {
            fileNameChar = "\\w|_|-";
            fileNameChars = "(\\w|_|-)*";
        }
    }

    // check input to filename field, only characters allowed in file names will be adopted
    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!textFieldResetInProgress) {
            String name = textField.getText();
            int pos = e.getOffset();
            if (!name.substring(pos, pos + 1).matches(fileNameChar)) {
                textFieldResetInProgress = true;
                resetTextField(new StringBuilder(name).deleteCharAt(pos).toString());
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    // check paste to filename field, only inserts witch all characters allowed in file names will be adopted
    @Override
    public void changedUpdate(DocumentEvent e) {
        if (!textFieldResetInProgress) {
            String name = textField.getText();
            int pos = e.getOffset();
            int length = e.getLength();
            if (!name.substring(pos, pos + length).matches(fileNameChars)) {
                textFieldResetInProgress = true;
                resetTextField(new StringBuilder(name).delete(pos, pos + length).toString());
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public void resetTextField(final String name) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textField.setText(name);
                textFieldResetInProgress = false;
            }
        });
    }

}
