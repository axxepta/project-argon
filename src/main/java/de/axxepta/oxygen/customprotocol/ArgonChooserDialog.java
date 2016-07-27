package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @author Markus on 27.07.2016.
 */
public class ArgonChooserDialog extends JDialog {

    public final static int OPEN = 0;
    public final static int SAVE = 1;

    private int type;
    private int depth = 0;
    private List<ArgonChooserListModel.Element> path = new ArrayList<>();
    private String pathString;

    private boolean canceled = true;

    private JLabel pathLabel;
    JList resourceList;
    private ArgonChooserListModel model;

    public ArgonChooserDialog(Frame parent, String title, int type) {
        super(parent);
        setTitle(title);
        this.type = type;
        setLayout(new BorderLayout());
        JPanel topPanel = createTopPanel();
        JScrollPane listPane = new JScrollPane(createSelectionTable());
        JPanel bottomPanel = createBottomPanel();
        add(topPanel, BorderLayout.PAGE_START);
        add(listPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        pathLabel = new JLabel();
        panel.add(pathLabel);
        return panel;
    }

    private JScrollPane createSelectionTable() {
        List<ArgonChooserListModel.Element> baseList = getProtocolList();
        model = new ArgonChooserListModel(baseList);
        resourceList = new JList(model);
        resourceList.setCellRenderer(new ArgonChooserListCellRenderer());
        return new JScrollPane(resourceList);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JLabel fileNameLabel = new JLabel("File Name:");
        JTextField selectedFileTextField = new JTextField();
        String[] buttonNames;
        JButton[] buttons;
        switch (type) {
            case OPEN: {
                buttonNames = new String[]{"OPEN", "CHECK OUT", "CANCEL"};
                buttons = new JButton[3];
                break;
            }
            default: {
                buttonNames = new String[]{"SAVE", "CANCEL"};
                buttons = new JButton[2];
            }
        }
        panel.add(fileNameLabel);
        panel.add(selectedFileTextField);
        int i = 0;
        for (JButton button : buttons) {
            button.setText(buttonNames[i++]);
            panel.add(button);
        }
        return panel;
    }

    /*
     * expects "level-up" element in the list to be of type ROOT
     */
    private void updateList(ArgonChooserListModel.Element element) {
        if (element.getType().equals(ArgonEntity.FILE))
            return;
        List<ArgonChooserListModel.Element> newList;
        if (element.getType().equals(ArgonEntity.ROOT)) {
            depth--;
            path.remove(path.size() - 1);
            if (depth == 1) {
                newList = getProtocolList();
            } else {
                newList = getNewList(element);
            }
        } else {
            depth++;
            path.add(element);
            newList = getNewList(element);
        }
        model.setData(newList);
    }

    private List<ArgonChooserListModel.Element> getNewList(ArgonChooserListModel.Element element) {
        String resourcePath = getResourceString();
        BaseXSource source = getSourceFromElement(element);
        return obtainNewList(source, resourcePath);
    }

    private BaseXSource getSourceFromElement(ArgonChooserListModel.Element element) {
        ArgonEntity rootEntity;
        if (element.getType().equals(ArgonEntity.DIR)) {
            rootEntity = path.get(0).getType();
        } else {
            rootEntity = element.getType();
        }
        BaseXSource source;
        switch (rootEntity) {
            case XQ: { source = BaseXSource.RESTXQ; break; }
            case REPO: { source = BaseXSource.REPO; break; }
            default: source = BaseXSource.DATABASE;
        }
        return source;
    }

    private String getResourceString() {
        StringBuilder resourceString = new StringBuilder();
        for (ArgonChooserListModel.Element element : path) {
            resourceString.append("/").append(element.getName());
        }
        return resourceString.toString();
    }

    private List<ArgonChooserListModel.Element> obtainNewList(BaseXSource source, String path) {
        List<ArgonChooserListModel.Element> newList = new ArrayList<>();
        newList.add(new ArgonChooserListModel.Element(ArgonEntity.ROOT, ".."));
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            List<BaseXResource> resourceList = connection.list(source, path);
            for (BaseXResource resource : resourceList) {
                if (resource.getType().equals(BaseXType.DIRECTORY))
                    newList.add(new ArgonChooserListModel.Element(ArgonEntity.DIR, resource.getName()));
                else
                    newList.add(new ArgonChooserListModel.Element(ArgonEntity.FILE, resource.getName()));
            }
        } catch (IOException ioe) {
            PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage("Failed to get resource list from BaseX server: " +
                    ioe.getMessage());
        }
        return newList;
    }

    private List<ArgonChooserListModel.Element> getProtocolList() {
        List<ArgonChooserListModel.Element> list = new ArrayList<>();
        list.add(new ArgonChooserListModel.Element(ArgonEntity.DB_BASE, Lang.get(Lang.Keys.tree_DB)));
        list.add(new ArgonChooserListModel.Element(ArgonEntity.XQ, Lang.get(Lang.Keys.tree_restxq)));
        list.add(new ArgonChooserListModel.Element(ArgonEntity.REPO, Lang.get(Lang.Keys.tree_repo)));
        return list;
    }

    URL[] selectURLs() {
        ArrayList<URL> selectedURLs = new ArrayList<>();
        canceled = true;
        setVisible(true);
        if (canceled) {
            return null;
        } else {
            return selectedURLs.toArray(new URL[0]);
        }
    }

}
