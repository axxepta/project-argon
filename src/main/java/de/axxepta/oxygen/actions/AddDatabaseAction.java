package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.workspace.BaseXOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Markus on 04.11.2015.
 */
public class AddDatabaseAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(AddDatabaseAction.class);

    private TreeModel treeModel;
    private TreeListener listener;
    private JDialog addDbDialog;
    private JTextField newDbNameTextField;

    public AddDatabaseAction(String name, Icon icon, TreeModel treeModel, TreeListener listener){
        super(name, icon);
        this.treeModel = treeModel;
        this.listener = listener;
    }

    public AddDatabaseAction(TreeModel treeModel, TreeListener listener){
        super();
        this.treeModel = treeModel;
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Frame parentFrame = (Frame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

        addDbDialog = new JDialog(parentFrame, Lang.get(Lang.Keys.cm_adddb));
        addDbDialog.setIconImage(ImageUtils.createImage("/images/Oxygen16.png"));
        addDbDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10,10));

        newDbNameTextField = new JTextField();
        newDbNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newDbNameTextField, true));
        content.add(newDbNameTextField, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton(new AddDbAction("Add"));
        btnPanel.add(addBtn, BorderLayout.WEST);
        JButton cancelBtn = new JButton(new CloseDialogAction("Cancel", addDbDialog));
        btnPanel.add(cancelBtn, BorderLayout.EAST);
        content.add(btnPanel, BorderLayout.SOUTH);

        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addDbDialog.setContentPane(content);
        addDbDialog.pack();
        addDbDialog.setLocationRelativeTo(parentFrame);
        addDbDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        addDbDialog.setVisible(true);
    }

    private class AddDbAction extends AbstractAction {

        AddDbAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String db = newDbNameTextField.getText();
            // ToDo: check, whether database already exists, otherwise duplicate node is inserted
            TreeNode parentNode = listener.getNode();
            String chop = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_DB_CREATE_CHOP, false).toLowerCase();
            String ftindex = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_DB_CREATE_FTINDEX, false).toLowerCase();
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                connection.create(db, chop, ftindex);
                TreeUtils.insertStrAsNodeLexi(treeModel, db, (DefaultMutableTreeNode) parentNode, false);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to add new database",
                        "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                logger.debug(ex.toString());
            }
            addDbDialog.dispose();
        }
    }

}
