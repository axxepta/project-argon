package de.axxepta.oxygen.workspace;

/**
 * Created by daltiparmak on 02.04.15.
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;

/**
 * Plugin option page extension Custom Workspace Access Plugin Extension.
 */
public class CustomWorkspaceAccessOptionPagePluginExtension extends OptionPagePluginExtension {

    /**
     * The option key describing the temporary location for the saved files.
     */
    public static final String KEY_SAVE_TEMPORARY_FILES_LOCATION = "save.tmp.location";

    /**
     * The option key describing the default checkout location for the directory chooser.
     */
    public static final String KEY_DEFAULT_CHECKOUT_LOCATION = "default.checkout.location";

    /**
     * The text filed for the temporary location of the saved files.
     */
    private JTextField saveTmpLocationTextField;

    /**
     * The text field for the default checkout location of the directory chooser.
     */
    private JTextField defaultCheckoutLocationTextField;

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#apply(ro.sync.exml.workspace.api.PluginWorkspace)
     */
    @Override
    public void apply(PluginWorkspace pluginWorkspace) {
        // Save the new locations in the option storage.
        pluginWorkspace.getOptionsStorage().setOption(KEY_SAVE_TEMPORARY_FILES_LOCATION,
                !"".equals(saveTmpLocationTextField.getText()) ? saveTmpLocationTextField.getText() : null);
        pluginWorkspace.getOptionsStorage().setOption(KEY_DEFAULT_CHECKOUT_LOCATION,
                !"".equals(defaultCheckoutLocationTextField.getText()) ? defaultCheckoutLocationTextField.getText() : null);
    }

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#restoreDefaults()
     */
    @Override
    public void restoreDefaults() {
        // Reset the text fields values. Empty string is used to map the <null> default values of the options.
        saveTmpLocationTextField.setText("");
        defaultCheckoutLocationTextField.setText("");
    }

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#getTitle()
     */
    @Override
    public String getTitle() {
        return "BaseX Configuration";
    }

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#init(ro.sync.exml.workspace.api.PluginWorkspace)
     */
    @Override
    public JComponent init(final PluginWorkspace pluginWorkspace) {
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel saveTmpLocationLbl = new JLabel("This Panel will be used to set BaseX DB configurations");

        // Create a new tree control
        //tree = new JTree();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(saveTmpLocationLbl, c);

        /*

        saveTmpLocationTextField = new JTextField();
        c.gridx ++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(saveTmpLocationTextField, c);

        JButton chooseSaveTmpLocatioBtn = new JButton("Choose");
        c.gridx ++;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        panel.add(chooseSaveTmpLocatioBtn, c);

        chooseSaveTmpLocatioBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = pluginWorkspace.chooseDirectory();
                if (dir != null) {
                    saveTmpLocationTextField.setText(dir.getAbsolutePath());
                }
            }
        });

        c.gridx = 0;
        c.gridy ++;
        JLabel defaultCheckOutLocationLbl = new JLabel("Default checkout location:");
        panel.add(defaultCheckOutLocationLbl, c);

        defaultCheckoutLocationTextField = new JTextField();
        c.gridx ++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(defaultCheckoutLocationTextField, c);

        JButton chooseDefaultCheckOutLocatioBtn = new JButton("Choose");
        c.gridx ++;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        panel.add(chooseDefaultCheckOutLocatioBtn, c);

        chooseDefaultCheckOutLocatioBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = pluginWorkspace.chooseDirectory();
                if (dir != null) {
                    defaultCheckoutLocationTextField.setText(dir.getAbsolutePath());
                }
            }
        });

        c.gridx = 0;
        c.gridy ++;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), c);

        String saveTmpLocation = pluginWorkspace.getOptionsStorage().getOption(
                KEY_SAVE_TEMPORARY_FILES_LOCATION,
                null);

        String defaultCheckOutLocation = pluginWorkspace.getOptionsStorage().getOption(
                KEY_DEFAULT_CHECKOUT_LOCATION,
                null);

        // Initialize the text fields with the stored options.
        saveTmpLocationTextField.setText(saveTmpLocation != null ? saveTmpLocation : "");
        defaultCheckoutLocationTextField.setText(defaultCheckOutLocation != null ? defaultCheckOutLocation : "");
        */

        return panel;
    }
}

