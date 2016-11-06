package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.tree.TreeListener;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 03.11.2016.
 */
public class SearchInFilesAction extends AbstractAction {
    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    private JTree tree;
    private String path = "";

    private JDialog searchDialog;
    private JTextField searchTermTextField;
    private JCheckBox elementCheckBox;
    private JCheckBox textCheckBox;
    private JCheckBox attributeCheckBox;
    private JCheckBox attrValueCheckBox;
    private JCheckBox wholeCheckBox;
    private JCheckBox caseCheckBox;


    public SearchInFilesAction (String name, Icon icon, JTree tree){
        super(name, icon);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath treePath = ((TreeListener) tree.getTreeSelectionListeners()[0]).getPath();
        path = TreeUtils.resourceFromTreePath(treePath);
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

        searchDialog = DialogTools.getOxygenDialog(parentFrame, Lang.get(Lang.Keys.cm_search));
        JPanel content = new JPanel(new BorderLayout(5,5));

        JPanel termPanel = new JPanel();
        termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
        termPanel.add(new Label(Lang.get(Lang.Keys.lbl_searchpath) + " " + path));
        searchTermTextField = new JTextField();
        termPanel.add(searchTermTextField);
        content.add(termPanel, BorderLayout.NORTH);

        JPanel allSettingsPanel = new JPanel(new GridLayout(2,1));

        JPanel settingsPanel = new JPanel(new GridLayout(2,2));
        elementCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_elements));
        settingsPanel.add(elementCheckBox);
        textCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_text));
        settingsPanel.add(textCheckBox);
        attributeCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_attributes));
        settingsPanel.add(attributeCheckBox);
        attrValueCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_attrbvalues));
        settingsPanel.add(attrValueCheckBox);
        TitledBorder titleScope = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.darkGray),
                Lang.get(Lang.Keys.lbl_scope));
        titleScope.setTitleJustification(TitledBorder.LEADING);
        settingsPanel.setBorder(titleScope);
        allSettingsPanel.add(settingsPanel);

        JPanel modSettingsPanel = new JPanel(new GridLayout(1,2));
        wholeCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_whole));
        modSettingsPanel.add(wholeCheckBox);
        caseCheckBox = new JCheckBox(Lang.get(Lang.Keys.lbl_casesens));
        modSettingsPanel.add(caseCheckBox);
        TitledBorder titleOptions = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.darkGray),
                Lang.get(Lang.Keys.lbl_options));
        titleOptions.setTitleJustification(TitledBorder.LEADING);
        modSettingsPanel.setBorder(titleOptions);
        allSettingsPanel.add(modSettingsPanel);

        content.add(allSettingsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton applyBtn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search(path, searchTermTextField.getText(), elementCheckBox.isSelected(), textCheckBox.isSelected(),
                        attributeCheckBox.isSelected(), attrValueCheckBox.isSelected(),
                        wholeCheckBox.isSelected(), caseCheckBox.isSelected());
                searchDialog.dispose();
            }
        });
        applyBtn.setText(Lang.get(Lang.Keys.cm_searchsimple));
        btnPanel.add(applyBtn, BorderLayout.EAST);
        JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), searchDialog));
        btnPanel.add(cancelBtn, BorderLayout.WEST);

        content.add(btnPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(searchDialog, content, parentFrame);
    }

    private void search(String path, String filter, boolean element, boolean text, boolean attribute, boolean attrValue,
                        boolean wholeMatch, boolean caseSensitive) {
        List<String> fileMatches = new ArrayList<>();
        try {
            if (element)
                fileMatches.addAll(ConnectionWrapper.searchElements(path, filter, wholeMatch, caseSensitive));
            if (text)
                fileMatches.addAll(ConnectionWrapper.searchText(path, filter, wholeMatch, caseSensitive));
            if (attribute)
                fileMatches.addAll(ConnectionWrapper.searchAttributes(path, filter, wholeMatch, caseSensitive));
            if (attrValue)
                fileMatches.addAll(ConnectionWrapper.searchAttributeValues(path, filter, wholeMatch, caseSensitive));

        } catch (IOException ie) {
            //
        }
    }
}
