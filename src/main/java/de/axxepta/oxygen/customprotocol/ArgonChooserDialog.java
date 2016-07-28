package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

/**
 * @author Markus on 27.07.2016.
 */
public class ArgonChooserDialog extends JDialog implements MouseListener {

    private static final Logger logger = LogManager.getLogger(ArgonChooserDialog.class);

    public final static int OPEN = 0;
    public final static int SAVE = 1;

    private boolean singleClick  = true;
    private Timer timer;

    private int type;
    private int depth = 0;
    private List<ArgonChooserListModel.Element> path = new ArrayList<>();
    private String pathString;
    private SelectionAction selectionAction;

    private boolean canceled = true;

    private JLabel pathLabel;
    private JList resourceList;
    private ArgonChooserListModel model;
    private JTextField selectedFileTextField;

    public ArgonChooserDialog(Frame parent, String title, int type) {
        super(parent);
        setModal(true);
        setTitle(title);
        this.type = type;
        setLayout(new BorderLayout());
        selectionAction = new SelectionAction();
        JPanel topPanel = createTopPanel();
        JScrollPane listPane = new JScrollPane(createSelectionTable());
        JPanel bottomPanel = createBottomPanel();
        add(topPanel, BorderLayout.PAGE_START);
        add(listPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
        setLocationRelativeTo(null);
        timer = new javax.swing.Timer(300, e -> {
            timer.stop();
            if (!singleClick)
                selectionAction.actionPerformed(null);
        });
        timer.setRepeats(false);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        pathLabel = new JLabel();
        pathLabel.setText("");

        JScrollPane labelScrollPane = new JScrollPane(pathLabel);
        labelScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        labelScrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        labelScrollPane.setMinimumSize(new Dimension(350,40));
        labelScrollPane.setPreferredSize(new Dimension(350,40));
        labelScrollPane.setMaximumSize(new Dimension(350,40));
        panel.add(labelScrollPane);
        return panel;
    }

    private JScrollPane createSelectionTable() {
        List<ArgonChooserListModel.Element> baseList = getProtocolList();
        model = new ArgonChooserListModel(baseList);
        resourceList = new JList(model);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceList.setCellRenderer(ClassFactory.getInstance().getChooserListCellRenderer());
        resourceList.addListSelectionListener(e -> {
            if (!resourceList.isSelectionEmpty() &&
                    (model.getTypeAt(resourceList.getMinSelectionIndex()).equals(ArgonEntity.FILE))) {
                selectedFileTextField.setText(model.getNameAt(resourceList.getMinSelectionIndex()));
            } else {
                selectedFileTextField.setText("");
            }
        });
        resourceList.addMouseListener(this);
        resourceList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        resourceList.getActionMap().put("confirm", selectionAction);
        return new JScrollPane(resourceList);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JLabel fileNameLabel = new JLabel("File Name:");
        selectedFileTextField = new JTextField();
        selectedFileTextField.setEditable(false);
        selectedFileTextField.setColumns(25);
        selectedFileTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        selectedFileTextField.getActionMap().put("confirm", selectionAction);
        String[] buttonNames;
        JButton[] buttons;
        switch (type) {
            case OPEN: {
                buttonNames = new String[]{"OPEN", "CHECK OUT", "CANCEL"};
                buttons = createButtons(buttonNames);
                buttons[0].addActionListener(selectionAction);
                buttons[1].addActionListener(e -> {
                    if (!StringUtils.isEmpty(selectedFileTextField.getText())) {
                        lock();
                        selectionAction.actionPerformed(null);
                    }
                });
                buttons[2].addActionListener( e ->  this.dispose() );
                break;
            }
            default: {
                buttonNames = new String[]{"SAVE", "CANCEL"};
                buttons = createButtons(buttonNames);
                buttons[0].addActionListener(selectionAction);
                buttons[1].addActionListener( e -> this.dispose() );
            }
        }
        panel.add(fileNameLabel);
        panel.add(selectedFileTextField);
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    private JButton[] createButtons(String[] labels) {
        JButton[] buttons = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            buttons[i] = new JButton(labels[i]);
        }
        return buttons;
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
            if (depth == 0) {
                newList = getProtocolList();
            } else {
                newList = getNewList(element);
            }
        } else {
            depth++;
            path.add(element);
            newList = getNewList(element);
        }
        if ((depth == 0) || ((depth == 1) && (path.get(0).getType().equals(ArgonEntity.DB_BASE)))) {
            selectedFileTextField.setEditable(false);
        } else {
            selectedFileTextField.setEditable(true);
        }
        model.setData(newList);
        selectedFileTextField.setText("");
        buildSelectionString();
        pathLabel.setText(pathString);
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
        StringBuilder resourceString = new StringBuilder("");
        for (int i = 1; i < path.size(); i++) {
            if (i != 1)
                resourceString.append("/");
            resourceString.append(path.get(i).getName());
        }
        return resourceString.toString();
    }

    private void buildSelectionString() {
        if (depth == 0)
            pathString = "";
        else
            pathString = CustomProtocolURLHandlerExtension.protocolFromSource(getSourceFromElement(path.get(0))) +
                    "://" + getResourceString() + "/" + selectedFileTextField.getText();
    }

    private List<ArgonChooserListModel.Element> obtainNewList(BaseXSource source, String path) {
        List<ArgonChooserListModel.Element> newList = new ArrayList<>();
        newList.add(new ArgonChooserListModel.Element(ArgonEntity.ROOT, ".."));
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            List<BaseXResource> resourceList = connection.list(source, path);
            for (BaseXResource resource : resourceList) {
                if ((depth == 1) && (this.path.get(0).getType().equals(ArgonEntity.DB_BASE)))
                    newList.add(new ArgonChooserListModel.Element(ArgonEntity.DB, resource.getName()));
                else if (resource.getType().equals(BaseXType.DIRECTORY))
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

    private void lock() {
        BaseXSource source = getSourceFromElement(path.get(0));
        String path = getResourceString() + "/" + selectedFileTextField.getText();
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(source, path);
        } catch (IOException ioe) {
            logger.error("Failed to lock resource " + path + " in " + source.toString() + ": " + ioe.getMessage());
        }
    }

    URL[] selectURLs() {
        ArrayList<URL> selectedURLs = new ArrayList<>();
        canceled = true;
        setVisible(true);
        if (canceled) {
            return null;
        } else {
            URL url;
            try {
                url = new URL(pathString);
                selectedURLs.add(url);
            } catch (MalformedURLException mue) {
                logger.error("Selected path " + pathString + " cannot be converted to URL.");
            }
            return selectedURLs.toArray(new URL[selectedURLs.size()]);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            singleClick = true;
            timer.start();
        } else {
            singleClick = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}


    private class SelectionAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!(StringUtils.isEmpty(selectedFileTextField.getText()) && resourceList.isSelectionEmpty())) {
                if ((resourceList.isSelectionEmpty()) ||
                        (model.getTypeAt(resourceList.getSelectedIndices()[0]).equals(ArgonEntity.FILE))) {
                    canceled = false;
                    buildSelectionString();
                    setVisible(false);
                    dispose();
                } else {
                    updateList((ArgonChooserListModel.Element) model.getElementAt(resourceList.getSelectedIndices()[0]));
                }
            }
        }
    }

}
