package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.workspace.ArgonOptionPage;
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

        AddDbAction addDB = new AddDbAction("Add");

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
            // ToDo: check, whether database already exists, otherwise duplicate node is inserted
            String chop = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_CHOP, false).toLowerCase();
            String ftindex = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_FTINDEX, false).toLowerCase();
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                connection.create(db, chop, ftindex);
                TopicHolder.newDir.postMessage(ArgonConst.ARGON + ":" + db);
            } catch (IOException | NullPointerException ex) {
                String error = ex.getMessage();
                if ((error == null) || error.equals("null"))
                    error = "Database connection could not be established.";
                PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage("Failed to add new database: " + error);
                logger.debug(ex.getMessage());
            }
            addDbDialog.dispose();
        }
    }

}
