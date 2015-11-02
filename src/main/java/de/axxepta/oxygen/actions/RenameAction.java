package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.BasexTreeCellRenderer;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 02.11.2015.
 */
public class RenameAction extends AbstractAction {

    final BasexTree tree;
    final DefaultTreeModel treeModel;
    final TreeListener treeListener;
    JDialog renameDialog;
    JTextField newFileNameTextField;
    BaseXSource source;
    TreePath path;
    String db_path;

    public RenameAction(String name, Icon icon, BasexTree tree, TreeListener treeListener){
        super(name, icon);
        this.tree = tree;
        this.treeModel = (DefaultTreeModel) tree.getModel();
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        path = treeListener.getPath();
        source = TreeUtils.sourceFromTreePath(path);
        db_path = TreeUtils.resourceFromTreePath(path);
        if ((source != null) && (!db_path.equals(""))) {
            JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

            renameDialog = new JDialog(parentFrame, "Rename Resource");
            renameDialog.setIconImage(BasexTreeCellRenderer.createImage("/images/Oxygen16.png"));
            renameDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel content = new JPanel(new BorderLayout(10,10));

            newFileNameTextField = new JTextField();
            newFileNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newFileNameTextField, true));
            content.add(newFileNameTextField, BorderLayout.NORTH);

            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton(new renameThisAction("Rename"));
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction("Cancel", renameDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            renameDialog.setContentPane(content);
            renameDialog.pack();
            renameDialog.setLocationRelativeTo(parentFrame);
            renameDialog.setVisible(true);
        }
    }

    private class renameThisAction extends AbstractAction {

        public renameThisAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String newPath = newFileNameTextField.getText();
            if (!newPath.equals("")) {
                try {
                    new BaseXRequest("rename", source, db_path,
                            TreeUtils.resourceFromTreePath(path.getParentPath()) + "/" + newPath);
                    treeModel.valueForPathChanged(path, newPath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Failed to rename resource",
                            "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                }
            }
            renameDialog.dispose();
        }
    }

}
