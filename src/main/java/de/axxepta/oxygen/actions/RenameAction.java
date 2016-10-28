package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Markus on 02.11.2015.
 */
public class RenameAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(RenameAction.class);
    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();
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

    public static void rename(TreeModel treeModel, TreePath path, BaseXSource source, String db_path,
                               String newPathString, String newName, PluginWorkspace workspace) throws Exception {
        boolean isFile = TreeUtils.isFile(path);
        if (!isFile && ConnectionWrapper.directoryExists(source, newPathString)) {
            workspace.showInformationMessage("Target directory " + newPathString + " already exists. Cannot rename resource.");
            // bulk handling not possible, would have to check every file for overwrite
            return;
        }
        boolean locked;
        locked = ConnectionWrapper.pathContainsLockedResource(source, db_path) ||
                ConnectionWrapper.pathContainsLockedResource(source, newPathString);
        if (!locked) {
            boolean writable = true;
            if (isFile)
                writable = WorkspaceUtils.newResourceOrOverwrite(source, newPathString);
            if (writable) {
                try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                    connection.rename(source, db_path, newPathString);
                    String newURLString = CustomProtocolURLHandlerExtension.protocolFromSource(source) + ":" + newPathString;
                    int endPosNewBase = Math.max( newURLString.lastIndexOf("/"), newURLString.indexOf(":"));
                    TreePath newBasePath = TreeUtils.pathFromURLString(newURLString.substring(0, endPosNewBase));
                    TreeUtils.insertStrAsNodeLexi(treeModel, newName,
                            (DefaultMutableTreeNode) newBasePath.getLastPathComponent(), isFile);
                    ((DefaultTreeModel) treeModel).removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
                     TopicHolder.saveFile.postMessage(newURLString);
                }
            }
        } else {
            workspace.showInformationMessage("Source or target is locked or contains locked file. Check in before renaming.");
        }
    }


    private class RenameThisAction extends AbstractAction {

        RenameThisAction(String name){
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String newName = newFileNameTextField.getText();
            if (!newName.equals("")) {
                String newPathString;
                if (path.getPathCount() == 2)
                    newPathString = TreeUtils.resourceFromTreePath(path.getParentPath()) + newName;
                else
                    newPathString = TreeUtils.resourceFromTreePath(path.getParentPath()) + "/" + newName;
                try {
                    rename(treeModel, path, source, db_path, newPathString, newName, workspace);
                } catch (Exception ex) {
                    workspace.showInformationMessage("Failed to rename resource");
                    logger.debug(ex.toString());
                }
            }
            renameDialog.dispose();
        }

    }

}
