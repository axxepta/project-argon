package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.tree.ArgonTree;
import de.axxepta.oxygen.tree.ArgonTreeNode;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basex.util.TokenBuilder;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus on 20.10.2015.
 */
public class AddNewFileAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(AddNewFileAction.class);

    private ArgonTree tree;
    private JDialog newFileDialog;

    private JTextField newFileNameTextField;
    private JComboBox newFileTypeComboBox;

    public AddNewFileAction(String name, Icon icon, ArgonTree tree){
        super(name, icon);
        this.tree = tree;
    }

    public AddNewFileAction(ArgonTree tree){
        super();
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreeListener listener = ((TreeListener) tree.getTreeSelectionListeners()[0]);
        TreePath path = listener.getPath();
        String db_path = TreeUtils.resourceFromTreePath(path);
        String urlString = ((ArgonTreeNode) path.getLastPathComponent()).getTag().toString();

        if (listener.getNode().getAllowsChildren()) {

            JFrame parentFrame = (JFrame) (new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame();
            newFileDialog = DialogTools.getOxygenDialog(parentFrame, "Add new File to " + urlString);

            AddNewSpecFileAction addFile = new AddNewSpecFileAction("Add File", path, db_path);

            JPanel content = new JPanel(new BorderLayout(10,10));
            JPanel namePanel = new JPanel(new GridLayout());
            JLabel nameLabel = new JLabel("File Name", JLabel.LEFT);
            namePanel.add(nameLabel);
            newFileNameTextField = new JTextField();
            newFileNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newFileNameTextField, false));
            namePanel.add(newFileNameTextField);
            content.add(namePanel, BorderLayout.NORTH);

            newFileNameTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
            newFileNameTextField.getActionMap().put("confirm", addFile);

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
            JButton addBtn = new JButton(addFile);
            btnPanel.add(addBtn, BorderLayout.WEST);
            JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), newFileDialog));
            btnPanel.add(cancelBtn, BorderLayout.EAST);
            content.add(btnPanel, BorderLayout.SOUTH);

            DialogTools.wrapAndShow(newFileDialog, content, parentFrame);
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
                        template.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<a/>");
                        break;
                    case ".xquery":
                        template.add("xquery version \"3.0\";");
                        break;
                    case ".xqm":
                        template.add("xquery version \"3.0\";\n module namespace " + name + " = \"" + name + "\";");
                        break;
                    default:
                        template.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<a/>");
                }
                // add file
                BaseXSource source = TreeUtils.sourceFromTreePath(path);
                String resource = TreeUtils.resourceFromTreePath(path) + "/" + name + ext;
                String urlString = TreeUtils.urlStringFromTreePath(path) + "/" + name + ext;
                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e1) {
                    logger.error(e1);
                    return;
                }
                PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
                boolean isLocked = ConnectionWrapper.isLocked(source, resource);
                if (isLocked) {
                    pluginWorkspace.showInformationMessage("Couldn't create new file. Resource already exists\n" +
                                "and is locked by another user.");
                } else {
                    // ToDo: proper locking while store process
                    if (WorkspaceUtils.newResourceOrOverwrite(source, resource)) {
                        try {
                            WorkspaceUtils.setCursor(WorkspaceUtils.WAIT_CURSOR);
                            ConnectionWrapper.save(url, template.finish(), "UTF-8");
                            WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                        } catch (IOException ex) {
                            WorkspaceUtils.setCursor(WorkspaceUtils.DEFAULT_CURSOR);
                            pluginWorkspace.showInformationMessage("Couldn't create new file.");
                        }
                    }
                }
            }
            newFileDialog.dispose();
        }
    }

}
