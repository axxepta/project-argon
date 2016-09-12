package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Markus on 02.11.2015.
 */
public class RenameAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(RenameAction.class);
    private final TreeModel treeModel;
    private final TreeListener treeListener;
    private JDialog renameDialog;
    private JTextField newFileNameTextField;
    private BaseXSource source;
    private TreePath path;
    private String db_path;

    public RenameAction(String name, Icon icon, ArgonTree tree, TreeListener treeListener){
        super(name, icon);
        this.treeModel = tree.getModel();
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        path = treeListener.getPath();
        source = TreeUtils.sourceFromTreePath(path);
        db_path = TreeUtils.resourceFromTreePath(path);
        if ((source != null) && (!db_path.equals(""))) {
            JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

            String urlString = TreeUtils.urlStringFromTreePath(path);
            int filePosition = urlString.lastIndexOf("/");
            if (filePosition == -1)
                filePosition = urlString.indexOf(":");
            String fileName = urlString.substring(filePosition + 1);
            String filePath = urlString.substring(0, filePosition + 1);

            renameDialog = DialogTools.getOxygenDialog(parentFrame, Lang.get(Lang.Keys.cm_rename) + " in " + filePath);

            JPanel content = new JPanel(new BorderLayout(10,10));

            RenameThisAction renameThisAction = new RenameThisAction(Lang.get(Lang.Keys.cm_rename));

            newFileNameTextField = new JTextField();
            newFileNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newFileNameTextField, true));
            content.add(newFileNameTextField, BorderLayout.NORTH);
            newFileNameTextField.setText(fileName);
            newFileNameTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
            newFileNameTextField.getActionMap().put("confirm", renameThisAction);

            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton(renameThisAction);
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), renameDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            DialogTools.wrapAndShow(renameDialog, content, parentFrame);
        }
    }

    private class RenameThisAction extends AbstractAction {

        RenameThisAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String newPath = newFileNameTextField.getText();
            if (!newPath.equals("")) {
                String newPathString;
                if (path.getPathCount() == 2)
                    newPathString = TreeUtils.resourceFromTreePath(path.getParentPath()) + newPath;
                else
                    newPathString = TreeUtils.resourceFromTreePath(path.getParentPath()) + "/" + newPath;
                if (!ConnectionWrapper.isLocked(source, newPathString)) {
                    if (WorkspaceUtils.newResourceOrOverwrite(source, newPathString)) {
                        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                            connection.rename(source, db_path, newPathString);
                            treeModel.valueForPathChanged(path, newPath);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Failed to rename resource",
                                    "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                            logger.debug(ex.toString());
                        }
                    }
                } else {
                    PluginWorkspaceProvider.getPluginWorkspace().showInformationMessage("Resource " + newPathString +
                            " already exists and is locked by another user");
                }
            }
            renameDialog.dispose();
        }
    }

}
