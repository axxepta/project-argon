package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ImageUtils;
import org.basex.util.TokenBuilder;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Markus on 20.10.2015.
 */
public class AddNewFileAction extends AbstractAction {

    StandalonePluginWorkspace wsa;
    BasexTree tree;
    JDialog newFileDialog;

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
        String db_path = TreeUtils.resourceFromTreePath(path);
        String pathString = TreeUtils.protocolFromTreePath(path) + ":/" + db_path;

        if (((TreeListener) tree.getTreeSelectionListeners()[0]).getNode().getAllowsChildren()) {

            // show dialog
            JFrame parentFrame = (JFrame) (new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame();
            newFileDialog = new JDialog(parentFrame, "Add new File to " + pathString);
            newFileDialog.setIconImage(ImageUtils.createImage("/images/Oxygen16.png"));
            newFileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


            JPanel content = new JPanel(new BorderLayout(10,10));

            JPanel namePanel = new JPanel(new GridLayout());
            JLabel nameLabel = new JLabel("File Name", JLabel.LEFT);
            namePanel.add(nameLabel);
            newFileNameTextField = new JTextField();
            newFileNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newFileNameTextField, false));
            namePanel.add(newFileNameTextField);
            content.add(namePanel, BorderLayout.NORTH);

            JPanel extPanel = new JPanel(new GridLayout());
            JLabel extLabel = new JLabel("File Type", JLabel.LEFT);
            extPanel.add(extLabel);
            String[] fileTypes = {"XML Document (*.xml)", "XQuery (*.xquery)",
                    "XQuery Module (*.xqm)"};
            newFileTypeComboBox = new JComboBox<>(fileTypes);
            newFileTypeComboBox.setRenderer(new NewFileListCellRenderer());
            extPanel.add(newFileTypeComboBox);
            content.add(extPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton(new AddNewSpecFileAction("Add File", path, db_path));
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction("Cancel", newFileDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            newFileDialog.setContentPane(content);
            newFileDialog.pack();
            newFileDialog.setLocationRelativeTo(parentFrame);
            //DialogTools.CenterDialogRelativeToParent(newFileDialog);
            newFileDialog.setVisible(true);
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
            if (!name.equals("")) {
                String ext = newFileTypeComboBox.getSelectedItem().toString();
                int ind1 = ext.indexOf('(');
                int ind2 = ext.indexOf(')');
                ext = ext.substring(ind1 + 2, ind2);
                // get template
                final TokenBuilder template = new TokenBuilder();
                switch (ext) {
                    case ".xml":
                        template.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        break;
                    case ".xquery":
                        template.add("xquery version \"3.0\";");
                        break;
                    case "xqm":
                        template.add("xquery version \"3.0\";\n module namespace " + name + " = \"" + name + "\";");
                        break;
                    default:
                        template.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                }
                // add file
                String resource = db_path + "/" + name + ext;
                try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                    connection.put(TreeUtils.sourceFromTreePath(path), resource, template.finish());
                    TopicHolder.saveFile.postMessage(TreeUtils.protocolFromTreePath(path) + ":" + resource);
                } catch (IOException er) {
                    er.printStackTrace();
                    JOptionPane.showMessageDialog(null, er.getMessage(), "BaseX Connection Error",
                            JOptionPane.PLAIN_MESSAGE);
                }
            }
            newFileDialog.dispose();
        }
    }

}
