package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ImageUtils;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author Markus on 17.10.2015.
 */
public class SearchInPathAction extends AbstractAction {

    private JTree tree;
    Icon icon;
    private StandalonePluginWorkspace wsa;

    public SearchInPathAction (String name, Icon icon, StandalonePluginWorkspace wsa, JTree tree){
        super(name, icon);
        this.icon = icon;
        this.tree = tree;
        this.wsa = wsa;
    }

    public void actionPerformed(ActionEvent e) {
        TreePath path = ((TreeListener) tree.getTreeSelectionListeners()[0]).getPath();
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());
        if (path.getPathCount() == 1) {
            JOptionPane.showMessageDialog(parentFrame, "Please select source to search in (Databases/RestXQ/Repo).",
                    "Search in Path", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        if ((path.getPathCount() == 2) && (path.getPathComponent(1).toString().equals("Databases"))) {
            JOptionPane.showMessageDialog(parentFrame, "Please select specific database to search in.",
                    "Search in Path", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
            String pathStr;
            BaseXSource source;
            switch (path.getPathComponent(1).toString()) {
                case "Databases":
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.DATABASE;
                    break;
                case "Query Folder":
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON_XQ + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.RESTXQ;
                    break;
                default:
                    if (path.getPathCount() == 2) {
                        pathStr = path.getPathComponent(1).toString();
                    } else {
                        pathStr = CustomProtocolURLHandlerExtension.ARGON_REPO + ":" + TreeUtils.resourceFromTreePath(path);
                    }
                    source = BaseXSource.REPO;

            }
            // get filter string
            String filter = JOptionPane.showInputDialog(parentFrame, "Find resource in path\n" +
                    pathStr, "Search in Path", JOptionPane.PLAIN_MESSAGE);
            if ((filter != null) && (!filter.equals(""))) {
                ArrayList<String> allResources = searchResourcesInPath(source, path, filter);

                // show found resources
                JList<String> resultList = new JList<>(allResources.toArray(new String[allResources.size()]));

                JDialog resultsDialog = createSelectionListDialog(parentFrame,
                        "Open/Find Resources",
                        "Search for '" + filter + "' in '" + pathStr + "' found " + allResources.size() + " resource(s).",
                        resultList,
                        700, 300);

                JPanel buttonsPanel = new JPanel();
                JButton openButton = new JButton(
                        new OpenListSelectionAction("Open Resource(s)", this.wsa, resultList, resultsDialog));
                buttonsPanel.add(openButton);

                JButton checkOutButton = new JButton(
                        new CheckOutListSelectionAction("Check out Resource(s)", resultList, resultsDialog));
                buttonsPanel.add(checkOutButton);

                JButton cancelButton = new JButton(new CloseDialogAction("Cancel", resultsDialog));
                buttonsPanel.add(cancelButton);

                resultsDialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
                resultsDialog.setVisible(true);
            }
        }
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static JDialog createSelectionListDialog(JFrame parentFrame, String title, String label, JList resultList,
                                             int width, int height) {
        JDialog resultsDialog = new JDialog(parentFrame, title);
        resultsDialog.setIconImage(ImageUtils.createImage("/images/Oxygen16.png"));
        resultsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        JLabel foundLabel = new JLabel(label);
        content.add(foundLabel, BorderLayout.NORTH);

        resultList.setLayoutOrientation(JList.VERTICAL);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane resultPane = new JScrollPane(resultList);
        resultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultsDialog.getContentPane().add(resultPane, BorderLayout.CENTER);
        content.add(resultPane);

        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        resultsDialog.setContentPane(content);
        resultsDialog.setSize(width, height);
        resultsDialog.setLocationRelativeTo(parentFrame);
        resultsDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        return resultsDialog;
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static ArrayList<String> searchResourcesInPath(BaseXSource source, TreePath path, String filter) {
        String basePathStr = TreeUtils.resourceFromTreePath(path);
        ArrayList<String> allResources = searchResourcesInPathString(source, basePathStr, filter);
        String searchRoot;
        if (source.equals(BaseXSource.DATABASE))
            searchRoot = TreeUtils.treeStringFromTreePath(TreeUtils.pathToDepth(path,2))+"/";
        else
            searchRoot = TreeUtils.treeStringFromTreePath(path)+"/";
        for (int i=0; i<allResources.size(); i++) {
            allResources.set(i, searchRoot+allResources.get(i));
        }
        return allResources;
    }

    // made public for access via AspectJ
    @SuppressWarnings("all")
    public static ArrayList<String> searchResourcesInPathString(BaseXSource source, String basePathStr, String filter) {
        ArrayList<String> allResources = new ArrayList<>();
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());
        try {
            BaseXRequest search = new BaseXRequest("look", source, basePathStr, filter);
            allResources.addAll(search.getResult());

        } catch (Exception er) {
            JOptionPane.showMessageDialog(parentFrame, "Failed to search for BaseX resources.\n" +
                            " Check if server ist still running.",
                    "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
        }
        for (int i=0; i<allResources.size(); i++) {
            allResources.set(i, allResources.get(i).replaceAll("\\\\","/"));
        }
        return allResources;
    }

}
