package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * @author Markus on 04.11.2015.
 */
public class AddDatabaseAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(AddDatabaseAction.class);

    private JDialog addDbDialog;
    private JTextField newDbNameTextField;

    public AddDatabaseAction(String name, Icon icon){
        super(name, icon);
    }

    public AddDatabaseAction(){
        super();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

        addDbDialog = DialogTools.getOxygenDialog(parentFrame, Lang.get(Lang.Keys.cm_adddb));

        JPanel content = new JPanel(new BorderLayout(10,10));

        AddDbAction addDB = new AddDbAction(Lang.get(Lang.Keys.cm_addsimple));

        newDbNameTextField = new JTextField();
        newDbNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newDbNameTextField, true));
        content.add(newDbNameTextField, BorderLayout.NORTH);

        newDbNameTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        newDbNameTextField.getActionMap().put("confirm", addDB);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton(addDB);
        btnPanel.add(addBtn, BorderLayout.WEST);
        JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), addDbDialog));
        btnPanel.add(cancelBtn, BorderLayout.EAST);
        content.add(btnPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(addDbDialog, content, parentFrame);
    }

    private class AddDbAction extends AbstractAction {

        AddDbAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String db = newDbNameTextField.getText();
            try {   // not the nice way, but catch exception with no database error message
                ConnectionWrapper.list(BaseXSource.DATABASE, db);
                PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(Lang.get(Lang.Keys.msg_dbexists1 ) + "\n " +
                        Lang.get(Lang.Keys.msg_dbexists2));
            } catch (IOException ie) {
                try {
                    ConnectionWrapper.create(db);
                } catch (IOException ex) {
                    PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(Lang.get(Lang.Keys.warn_failednewdb) +
                            " " + ex.getMessage());
                    logger.debug(ex.getMessage());
                }
            }
            addDbDialog.dispose();
        }
    }

}
