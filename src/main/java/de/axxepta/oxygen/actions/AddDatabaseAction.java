package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Markus on 04.11.2015.
 */
public class AddDatabaseAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(AddDatabaseAction.class);

    DefaultTreeModel treeModel;
    TreeListener listener;
    JDialog addDbDialog;
    JTextField newDbNameTextField;

    public AddDatabaseAction(String name, Icon icon, DefaultTreeModel treeModel, TreeListener listener){
        super(name, icon);
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
        addDbDialog.setVisible(true);
/*        String db;
        BaseXRequest("create", BaseXSource.DATABASE, db);*/
    }

    private class AddDbAction extends AbstractAction {

        public AddDbAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String db = newDbNameTextField.getText();
            DefaultMutableTreeNode parentNode = listener.getNode();
            try {
                new BaseXRequest("create", BaseXSource.DATABASE, db);
                TreeUtils.insertStrAsNodeLexi(treeModel, db, parentNode, false);
/*                DefaultMutableTreeNode newDb = new DefaultMutableTreeNode("db");
                newDb.setAllowsChildren(true);
                treeModel.insertNodeInto(newDb, parentNode, treeModel.getChildCount(parentNode));
                treeModel.valueForPathChanged(new TreePath(treeModel.getPathToRoot(parentNode)), db);*/
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to add new database",
                        "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                logger.debug(ex.toString());
            }
            addDbDialog.dispose();
        }
    }

}