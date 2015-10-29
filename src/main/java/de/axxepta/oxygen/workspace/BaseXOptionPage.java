package de.axxepta.oxygen.workspace;

/**
 * Class defines option page for argon plugin connection details
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * Plugin option page extension Custom Workspace Access Plugin Extension.
 */
public class BaseXOptionPage extends OptionPagePluginExtension {


    /**
     * BaseX Keys
     */

    public static final String KEY_BASEX_HOST = "KEY_BASEX_HOST";
    public static final String KEY_BASEX_HTTP_PORT = "KEY_BASEX_HTTP_PORT";
    public static final String KEY_BASEX_TCP_PORT = "KEY_BASEX_TCP_PORT";
    public static final String KEY_BASEX_USERNAME = "KEY_BASEX_USERNAME";
    public static final String KEY_BASEX_PASSWORD = "KEY_BASEX_PASSWORD";
    public static final String KEY_BASEX_CONNECTION = "KEY_BASEX_CONNECTION";
    public static final String KEY_BASEX_LOGFILE = "KEY_BASEX_LOGFILE";

    private static final String DEF_BASEX_HOST = "localhost";
    private static final String DEF_BASEX_HTTP_PORT = "8984";
    private static final String DEF_BASEX_TCP_PORT = "1984";
    private static final String DEF_BASEX_USERNAME = "admin";
    private static final String DEF_BASEX_PASSWORD = "admin";
    private static final String DEF_BASEX_CONNECTION = "HTTP";
    private static final String DEF_BASEX_LOGFILE = "/tmp/argon.log";

    /**
     * BaseX JTextFields
     */
    private JTextField baseXHostTextField;
    private JTextField baseXHttpPortTextField;
    private JTextField baseXTcpPortTextField;
    private JTextField baseXUsernameTextField;
    private JTextField baseXPasswordTextField;
    private JComboBox baseXConnectionTypeComboBox;
    private JTextField baseXLogfileTextField;

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#apply(ro.sync.exml.workspace.api.PluginWorkspace)
     */
    @Override
    public void apply(PluginWorkspace pluginWorkspace) {

        // save BaseX configs in the option storage


        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_HOST,
                !"".equals(baseXHostTextField.getText()) ? baseXHostTextField.getText() : null);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_HTTP_PORT,
                !"".equals(baseXHttpPortTextField.getText()) ? baseXHttpPortTextField.getText() : null);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_TCP_PORT,
                !"".equals(baseXTcpPortTextField.getText()) ? baseXTcpPortTextField.getText() : null);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_USERNAME,
                !"".equals(baseXUsernameTextField.getText()) ? baseXUsernameTextField.getText() : null);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_PASSWORD,
                !"".equals(baseXPasswordTextField.getText()) ? baseXPasswordTextField.getText() : null);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_CONNECTION,
                baseXConnectionTypeComboBox.getSelectedItem().toString());

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_LOGFILE,
                !"".equals(baseXLogfileTextField.getText()) ? baseXLogfileTextField.getText() : null);
    }

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#restoreDefaults()
     */
    @Override
    public void restoreDefaults() {
        // Reset the text fields values. Empty string is used to map the <null> default values of the options.
        baseXHostTextField.setText(DEF_BASEX_HOST);
        baseXHttpPortTextField.setText(DEF_BASEX_HTTP_PORT);
        baseXTcpPortTextField.setText(DEF_BASEX_TCP_PORT);
        baseXUsernameTextField.setText(DEF_BASEX_USERNAME);
        baseXPasswordTextField.setText(DEF_BASEX_PASSWORD);
        baseXConnectionTypeComboBox.setSelectedItem(DEF_BASEX_CONNECTION);
        baseXLogfileTextField.setText(DEF_BASEX_LOGFILE);
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


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(saveTmpLocationLbl, c);


        /**
         * BaseX Hostname
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXHostTextFieldLbl = new JLabel("BaseX Hostname:");
        panel.add(baseXHostTextFieldLbl, c);

        baseXHostTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXHostTextField, c);


        /**
         * BaseX Http Port
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXHttpPortTextFieldLbl = new JLabel("BaseX HTTP Port:");
        panel.add(baseXHttpPortTextFieldLbl, c);

        baseXHttpPortTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXHttpPortTextField, c);


        /**
         * BaseX TCP Port
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXTcpPortTextFieldLbl = new JLabel("BaseX TCP Port:");
        panel.add(baseXTcpPortTextFieldLbl, c);

        baseXTcpPortTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXTcpPortTextField, c);


        /**
         * BaseX Username
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXUsernameTextFieldLbl = new JLabel("BaseX Username:");
        panel.add(baseXUsernameTextFieldLbl, c);

        baseXUsernameTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXUsernameTextField, c);


        /**
         * BaseX Password
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXPasswordTextFieldLbl = new JLabel("BaseX Password:");
        panel.add(baseXPasswordTextFieldLbl, c);

        baseXPasswordTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXPasswordTextField, c);


        /**
         * BaseX Connection type
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXConnectionTypeRadioFrameLbl = new JLabel("Connection type:");
        panel.add(baseXConnectionTypeRadioFrameLbl, c);

        String[] connectionTypes = { "HTTP" , "TCP" };
        baseXConnectionTypeComboBox = new JComboBox<>(connectionTypes);
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXConnectionTypeComboBox, c);

        /**
         * BaseX Logfile
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXLogfileTextFieldLbl = new JLabel("Argon Logfile:");
        panel.add(baseXLogfileTextFieldLbl, c);

        baseXLogfileTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXLogfileTextField, c);


        JButton chooseBaseXLogfileBtn = new JButton("Choose");
        c.gridx ++;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        panel.add(chooseBaseXLogfileBtn, c);

        chooseBaseXLogfileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = pluginWorkspace.chooseDirectory();
                if (dir != null) {
                    baseXLogfileTextField.setText(dir.getAbsolutePath() + "/argon.log");
                }
            }
        });


        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), c);

        /**
         * BaseX Strings
         */
        String baseXHost = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_HOST,
                null);
        String baseXHttpPort = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_HTTP_PORT,
                null);
        String baseXTcpPort = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_TCP_PORT,
                null);
        String baseXUsername = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_USERNAME,
                null);
        String baseXPassword = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_PASSWORD,
                null);
        String baseXConnection = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_CONNECTION,
                null);
        String baseXLogfile = pluginWorkspace.getOptionsStorage().getOption(
                KEY_BASEX_LOGFILE,
                null);

        // Initialize the text fields with the stored options.

        baseXHostTextField.setText(baseXHost != null ? baseXHost : "");
        baseXHttpPortTextField.setText(baseXHttpPort != null ? baseXHttpPort : "");
        baseXTcpPortTextField.setText(baseXTcpPort != null ? baseXTcpPort : "");
        baseXUsernameTextField.setText(baseXUsername != null ? baseXUsername : "");
        baseXPasswordTextField.setText(baseXPassword != null ? baseXPassword : "");
        if (baseXConnection.equals(connectionTypes[1])) {
            baseXConnectionTypeComboBox.setSelectedItem(connectionTypes[1]);
        } else {
            baseXConnectionTypeComboBox.setSelectedItem(connectionTypes[0]);
        }
        baseXLogfileTextField.setText(baseXLogfile != null ? baseXLogfile : "");

        return panel;
    }

    public static String getOption(String key, boolean defaults) {
        if (defaults) {
            switch (key) {
                case KEY_BASEX_HOST: return DEF_BASEX_HOST;
                case KEY_BASEX_HTTP_PORT: return DEF_BASEX_HTTP_PORT;
                case KEY_BASEX_TCP_PORT: return DEF_BASEX_TCP_PORT;
                case KEY_BASEX_USERNAME: return DEF_BASEX_USERNAME;
                case KEY_BASEX_PASSWORD: return DEF_BASEX_PASSWORD;
                case KEY_BASEX_CONNECTION: return DEF_BASEX_CONNECTION;
                case KEY_BASEX_LOGFILE: return DEF_BASEX_LOGFILE;
                default: return "";
            }
        } else {
            PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
            if (pluginWorkspace != null) {
                WSOptionsStorage store = pluginWorkspace.getOptionsStorage();
                if (store != null) return store.getOption(key, "empty option");
                else return "no store";
            } else return "no plugin workspace";
        }
    }

}

