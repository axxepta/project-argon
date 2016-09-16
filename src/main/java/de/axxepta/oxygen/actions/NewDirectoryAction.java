package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.ArgonTreeNode;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Markus on 12.07.2016.
 */
public class NewDirectoryAction extends AbstractAction {

    private ArgonTree tree;
    private JDialog newDirectoryDialog;
    private JTextField newDirectoryNameTextField;

    public NewDirectoryAction(String name, Icon icon, ArgonTree tree) {
        super(name, icon);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreeListener listener = ((TreeListener) tree.getTreeSelectionListeners()[0]);
        TreePath path = listener.getPath();
        String urlString = ((ArgonTreeNode) path.getLastPathComponent()).getTag().toString();

        if (listener.getNode().getAllowsChildren()) {

            JFrame parentFrame = (JFrame) (new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame();
            newDirectoryDialog = DialogTools.getOxygenDialog(parentFrame, "Add new Directory to " + urlString);

            JPanel content = new JPanel(new BorderLayout(10,10));

            MakeNewDirectoryAction makeDirectory = new MakeNewDirectoryAction(Lang.get(Lang.Keys.cm_newdir),
                    (DefaultMutableTreeNode) path.getLastPathComponent());

            JPanel namePanel = new JPanel(new GridLayout());
            JLabel nameLabel = new JLabel("Directory Name", JLabel.LEFT);
            namePanel.add(nameLabel);
            newDirectoryNameTextField = new JTextField();
            newDirectoryNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newDirectoryNameTextField, false));
            namePanel.add(newDirectoryNameTextField);
            content.add(namePanel, BorderLayout.NORTH);

            newDirectoryNameTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
            newDirectoryNameTextField.getActionMap().put("confirm", makeDirectory);

            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton(makeDirectory);
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), newDirectoryDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            DialogTools.wrapAndShow(newDirectoryDialog, content, parentFrame);
        }
    }

    private class MakeNewDirectoryAction extends AbstractAction {

        private DefaultMutableTreeNode node;

        MakeNewDirectoryAction(String name, DefaultMutableTreeNode node) {
            super(name);
            this.node = node;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = newDirectoryNameTextField.getText();
            TreeUtils.insertStrAsNodeLexi(tree.getModel(), name, node, false);
            newDirectoryDialog.dispose();
        }
    }

}
