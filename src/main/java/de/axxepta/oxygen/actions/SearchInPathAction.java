package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ImageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Markus on 17.10.2015.
 */
public class SearchInPathAction extends AbstractAction {

    JTree tree;
    Icon icon;
    StandalonePluginWorkspace wsa;
    private static final Logger logger = LogManager.getLogger(SearchInPathAction.class);
    JDialog resultsDialog;

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
        // ToDo: own class...
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
                resultsDialog = new JDialog(parentFrame, "Open/Find Resources");
                resultsDialog.setIconImage(ImageUtils.createImage("/images/Oxygen16.png"));
                resultsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                JPanel content = new JPanel(new BorderLayout());
                JLabel foundLabel = new JLabel("Search for '" + filter + "' in '" + pathStr + "' found "
                        + allResources.size() + " resource(s).");
                content.add(foundLabel, BorderLayout.NORTH);

                JList<String> resultList = new JList<>(allResources.toArray(new String[allResources.size()]));

                JPanel buttonsPanel = new JPanel();
                JButton openButton = new JButton(new OpenSelectedAction("Open Resource(s)", icon, this.wsa, resultList));
                buttonsPanel.add(openButton);

                JButton cancelButton = new JButton(new CloseDialogAction("Cancel", resultsDialog));
                buttonsPanel.add(cancelButton);

                content.add(buttonsPanel, BorderLayout.SOUTH);

                resultList.setLayoutOrientation(JList.VERTICAL);
                resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                JScrollPane resultPane = new JScrollPane(resultList);
                resultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                resultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                resultsDialog.getContentPane().add(resultPane, BorderLayout.CENTER);
                content.add(resultPane);

                content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                resultsDialog.setContentPane(content);
                resultsDialog.setSize(500,300);
                resultsDialog.setLocationRelativeTo(parentFrame);
                resultsDialog.setVisible(true);

            }
        }
    }

    protected static ArrayList<String> searchResourcesInPath(BaseXSource source, TreePath path, String filter) {
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

    protected static ArrayList<String> searchResourcesInPathString(BaseXSource source, String basePathStr, String filter) {
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


    private class OpenSelectedAction extends AbstractAction {

        JList<String> results;
        StandalonePluginWorkspace wsa;

        OpenSelectedAction(String name, Icon icon, StandalonePluginWorkspace wsa, JList<String> results) {
            super(name, icon);
            this.results = results;
            this.wsa = wsa;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> selectedResources = new ArrayList<>();
            selectedResources.addAll(results.getSelectedValuesList());

            for (Object resource : selectedResources) {
                String db_path = TreeUtils.urlStringFromTreeString(resource.toString());
                URL argonURL = null;
                try {
                    argonURL = new URL(db_path);
                } catch (MalformedURLException e1) {
                    logger.error(e1);
                }
                this.wsa.open(argonURL);
            }
            resultsDialog.dispose();
        }
    }

}
