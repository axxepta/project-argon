package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Markus on 20.10.2015.
 */
public class AddNewFileAction extends AbstractAction {

    StandalonePluginWorkspace wsa;
    BasexTree tree;

    JTextField newFileNameTextField;
    JComboBox newFileTypeComboBox;

    public AddNewFileAction(String name, Icon icon, StandalonePluginWorkspace wsa, BasexTree tree){
        super(name, icon);
        this.wsa = wsa;
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath path = ((TreeListener) tree.getTreeSelectionListeners()[0]).getPath();
        String db_path = TreeUtils.urlStringFromTreePath(path);
        if (((TreeListener) tree.getTreeSelectionListeners()[0]).getNode().getAllowsChildren()) {

            // show dialog
            JDialog newFileDialog = new JDialog(
                    (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame()),
                    "Add new File to BaseX Database path");
            newFileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


            JPanel content = new JPanel(new BorderLayout());

            JPanel namePanel = new JPanel();
            JLabel nameLabel = new JLabel("File Name");
            namePanel.add(nameLabel, BorderLayout.WEST);
            newFileNameTextField = new JTextField();
            namePanel.add(newFileNameTextField, BorderLayout.EAST);
            content.add(namePanel, BorderLayout.NORTH);

            JPanel extPanel = new JPanel();
            JLabel extLabel = new JLabel("File Type");
            extPanel.add(extLabel, BorderLayout.WEST);
            String[] fileTypes = {"XML Document (*.xml)", "XQuery (*.xquery)",
                    "XQuery Module (*.xqm)"};
            newFileTypeComboBox = new JComboBox(fileTypes);
            extPanel.add(extLabel, BorderLayout.EAST);
            content.add(extPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton(new AddNewSpecFileAction("Add File", path, db_path));
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction("Cancel", newFileDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            // get template

            // add file
            try {
                new BaseXRequest("add", TreeUtils.sourceFromTreePath(path), db_path);
            } catch (Exception er) {
                er.printStackTrace();
            }
        }
    }

    private class AddNewSpecFileAction extends AbstractAction {

        TreePath path;
        String db_path;

        AddNewSpecFileAction(String name, TreePath path, String db_path) {
            super(name);
            this.path = path;
            this.db_path = db_path;
        }

        @Override
        public void actionPerformed (ActionEvent e){

            String name = newFileNameTextField.getText();
            String ext = newFileTypeComboBox.getSelectedItem().toString();
            int ind1 = ext.indexOf('(');
            int ind2 = ext.indexOf(')');
            ext = ext.substring(ind1+2, ind2);
            // get template
            byte[] template;
            switch (ext) {
                case ".xml" : template = "<a/>".getBytes();
                    break;
                case ".xquery" : template = "xquery version \"3.0\";".getBytes();
                    break;
                case "xqm" : template = ("xquery version \"3.0\";\n module namespace " + name + " = \"" + name +"\";").getBytes();
                    break;
                default: template = "<a/>".getBytes();
            }
            // add file
            try {
                Connection connection = BaseXConnectionWrapper.getConnection();
                connection.put(TreeUtils.sourceFromTreePath(path), db_path + "/" + name + ext,
                        template);
                connection.close();
            } catch (Exception er) {
                er.printStackTrace();
            }

        }
    }

}
