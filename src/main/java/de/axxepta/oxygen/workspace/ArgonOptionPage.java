package de.axxepta.oxygen.workspace;

/**
 * Class defines option page for argon plugin connection details
 */

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;

import de.axxepta.oxygen.actions.FileNameFieldListener;
import de.axxepta.oxygen.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * Plugin option page extension Custom Workspace Access Plugin Extension.
 */
public class ArgonOptionPage extends OptionPagePluginExtension {

    private static final Logger logger = LogManager.getLogger(ArgonOptionPage.class);

    /**
     * BaseX Keys
     */
    public static final String KEY_BASEX_CONNECTION_SETTING = "KEY_BASEX_CONNECTION_SETTING";
    public static final String KEY_BASEX_HOST = "KEY_BASEX_HOST";
    public static final String KEY_BASEX_HTTP_PORT = "KEY_BASEX_HTTP_PORT";
    public static final String KEY_BASEX_TCP_PORT = "KEY_BASEX_TCP_PORT";
    public static final String KEY_BASEX_USERNAME = "KEY_BASEX_USERNAME";
    public static final String KEY_BASEX_PASSWORD = "KEY_BASEX_PASSWORD";
    public static final String KEY_BASEX_CONNECTION = "KEY_BASEX_CONNECTION";
    public static final String KEY_BASEX_VERSIONING = "KEY_BASEX_VERSIONING";
    public static final String KEY_BASEX_LOGFILE = "KEY_BASEX_LOGFILE";
    public static final String KEY_BASEX_DB_CREATE_CHOP = "KEY_BASEX_DB_CREATE_CHOP";
    public static final String KEY_BASEX_DB_CREATE_FTINDEX = "KEY_BASEX_DB_CREATE_FTINDEX";

    private static final String DEF_BASEX_CONNECTION_SETTING = "default";
    private static final String DEF_BASEX_HOST = "localhost:8984/rest";
    private static final String DEF_BASEX_HTTP_PORT = "8984";
    private static final String DEF_BASEX_TCP_PORT = "1984";
    private static final String DEF_BASEX_USERNAME = "admin";
    private static final String DEF_BASEX_PASSWORD = "admin";
    private static final String DEF_BASEX_CONNECTION = "HTTP";
    private static final String DEF_BASEX_VERSIONING = "true";
    private static final String DEF_BASEX_LOGFILE = System.getProperty("user.home") + "/argon.log";
            //"/tmp/argon.log";
    private static final String DEF_BASEX_DB_CREATE_CHOP = "false";
    private static final String DEF_BASEX_DB_CREATE_FTINDEX = "false";

    private static final String CONNECTION_SETTING_PATH = System.getProperty("user.home") + "/argon";
    private static final String CONNECTION_SETTING_FILE_TYPE = ".csini";

    private static final String CS_NAME = "CS_NAME";
    private static final String CS_HOST = "CS_HOST";
    private static final String CS_USER = "CS_USER";
    private static final String CS_PWD = "CS_PWD";

    /**
     * BaseX JTextFields
     */
    private JComboBox<String> baseXConnectionSettingsComboBox;
    private JTextField baseXHostTextField;
    private JTextField baseXHttpPortTextField;
    private JTextField baseXTcpPortTextField;
    private JTextField baseXUsernameTextField;
    private JTextField baseXPasswordTextField;
    private JComboBox baseXConnectionTypeComboBox;
    private JCheckBox baseXVersioningCheckBox;
    private JTextField baseXLogfileTextField;
    private JCheckBox baseXDBCreateChopCheckBox;
    private JCheckBox baseXDBCreateFTIndexCheckBox;

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#apply(ro.sync.exml.workspace.api.PluginWorkspace)
     */
    @Override
    public void apply(PluginWorkspace pluginWorkspace) {
        if (!((String) baseXConnectionSettingsComboBox.getSelectedItem()).
                matches(FileNameFieldListener.FILE_NAME_CHARS_WITHOUT_EXTENSION)) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Choose a name suitable for a file (without ending) as\n" +
                    "'Connection configuration'!", "Store Connection Settings Error", JOptionPane.ERROR_MESSAGE);
        } else {

            // save BaseX configs in the option storage
            //ToDo: clear ComboBox, if entries in the corresponding fields have been changed
            if (baseXConnectionSettingsComboBox.getSelectedIndex() == -1)
                storeConnectionSettings();

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_CONNECTION_SETTING,
                    !"".equals(baseXConnectionSettingsComboBox.getSelectedItem().toString()) ?
                            baseXConnectionSettingsComboBox.getSelectedItem().toString() : DEF_BASEX_CONNECTION_SETTING);

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_HOST,
                    !"".equals(baseXHostTextField.getText()) ? baseXHostTextField.getText() : DEF_BASEX_HOST);

/*        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_HTTP_PORT,
                !"".equals(baseXHttpPortTextField.getText()) ? baseXHttpPortTextField.getText() : DEF_BASEX_HTTP_PORT);

        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_TCP_PORT,
                !"".equals(baseXTcpPortTextField.getText()) ? baseXTcpPortTextField.getText() : DEF_BASEX_TCP_PORT);*/

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_USERNAME,
                    !"".equals(baseXUsernameTextField.getText()) ? baseXUsernameTextField.getText() : DEF_BASEX_USERNAME);

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_PASSWORD,
                    !"".equals(baseXPasswordTextField.getText()) ? baseXPasswordTextField.getText() : DEF_BASEX_PASSWORD);

/*        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_CONNECTION,
                baseXConnectionTypeComboBox.getSelectedItem().toString());*/

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_VERSIONING,
                    baseXVersioningCheckBox.isSelected() ? "true" : "false");

/*        pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_LOGFILE,
                !"".equals(baseXLogfileTextField.getText()) ? baseXLogfileTextField.getText() : DEF_BASEX_LOGFILE);*/

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_DB_CREATE_CHOP,
                    baseXDBCreateChopCheckBox.isSelected() ? "true" : "false");

            pluginWorkspace.getOptionsStorage().setOption(KEY_BASEX_DB_CREATE_FTINDEX,
                    baseXDBCreateFTIndexCheckBox.isSelected() ? "true" : "false");
        }
    }

    /**
     * @see ro.sync.exml.plugin.option.OptionPagePluginExtension#restoreDefaults()
     */
    @Override
    public void restoreDefaults() {
        // Reset the text fields values. Empty string is used to map the <null> default values of the options.
        baseXConnectionSettingsComboBox.setSelectedItem(0);
        baseXHostTextField.setText(DEF_BASEX_HOST);
/*        baseXHttpPortTextField.setText(DEF_BASEX_HTTP_PORT);
        baseXTcpPortTextField.setText(DEF_BASEX_TCP_PORT);*/
        baseXUsernameTextField.setText(DEF_BASEX_USERNAME);
        baseXPasswordTextField.setText(DEF_BASEX_PASSWORD);
/*        baseXConnectionTypeComboBox.setSelectedItem(DEF_BASEX_CONNECTION);*/
        baseXVersioningCheckBox.setSelected(Boolean.parseBoolean(DEF_BASEX_VERSIONING));
/*        baseXLogfileTextField.setText(DEF_BASEX_LOGFILE);*/
        baseXDBCreateChopCheckBox.setSelected(Boolean.parseBoolean(DEF_BASEX_DB_CREATE_CHOP));
        baseXDBCreateFTIndexCheckBox.setSelected(Boolean.parseBoolean(DEF_BASEX_DB_CREATE_FTINDEX));
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

        /**
         * Initial Values
         */
        final String baseXConnectionSetting = getOption(KEY_BASEX_CONNECTION_SETTING, false);
        String baseXHost = getOption(KEY_BASEX_HOST, false);
/*        String baseXHttpPort = getOption(KEY_BASEX_HTTP_PORT, false);*/
/*        String baseXTcpPort = getOption(KEY_BASEX_TCP_PORT, false)*/
        String baseXUsername = getOption(KEY_BASEX_USERNAME, false);
        String baseXPassword = getOption(KEY_BASEX_PASSWORD, false);
/*        String baseXConnection = getOption(KEY_BASEX_CONNECTION, false);*/
        String baseXVersioning = getOption(KEY_BASEX_VERSIONING, false);
/*        String baseXLogfile = getOption(KEY_BASEX_LOGFILE, false);*/
        String baseXDBCreateChop = getOption(KEY_BASEX_DB_CREATE_CHOP, false);
        String baseXDBCreateFTIndex = getOption(KEY_BASEX_DB_CREATE_FTINDEX, false);

        final List<String[]> connectionSettings = loadConnectionSettings();
        List<String> connectionSettingNames = new ArrayList<>();
        for (String[] setting : connectionSettings) {
            connectionSettingNames.add(setting[0]);
        }


        /**
         * Setup of Option Pane
         */
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel saveTmpLocationLbl = new JLabel("BaseX server connection configuration");
        saveTmpLocationLbl.setFont(saveTmpLocationLbl.getFont().deriveFont(Font.BOLD));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(saveTmpLocationLbl, c);


        /**
         * BaseX Connection Settings List
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXConnectionSettingsComboBoxLbl = new JLabel("Connection configuration:");
        panel.add(baseXConnectionSettingsComboBoxLbl, c);

        baseXConnectionSettingsComboBox = new JComboBox(connectionSettingNames.toArray());
        baseXConnectionSettingsComboBox.setEditable(true);
        baseXConnectionSettingsComboBox.addActionListener(e -> {
            int newSelection = ((JComboBox)e.getSource()).getSelectedIndex();
            if (newSelection != -1) {  // one of the old entries was selected
                baseXHostTextField.setText(connectionSettings.get(newSelection)[1]);
                baseXUsernameTextField.setText(connectionSettings.get(newSelection)[2]);
                baseXPasswordTextField.setText(connectionSettings.get(newSelection)[3]);
            }
        });
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXConnectionSettingsComboBox, c);


        /**
         * BaseX Hostname
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXHostTextFieldLbl = new JLabel("BaseX Host:");
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
/*        c.gridx = 0;
        c.gridy++;
        JLabel baseXHttpPortTextFieldLbl = new JLabel("BaseX HTTP Port:");
        panel.add(baseXHttpPortTextFieldLbl, c);

        baseXHttpPortTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXHttpPortTextField, c);*/


        /**
         * BaseX TCP Port
         */
/*        c.gridx = 0;
        c.gridy++;
        JLabel baseXTcpPortTextFieldLbl = new JLabel("BaseX TCP Port:");
        panel.add(baseXTcpPortTextFieldLbl, c);

        baseXTcpPortTextField = new JTextField();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXTcpPortTextField, c);*/


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
/*        c.gridx = 0;
        c.gridy++;
        JLabel baseXConnectionTypeRadioFrameLbl = new JLabel("Connection type:");
        panel.add(baseXConnectionTypeRadioFrameLbl, c);

        String[] connectionTypes = { "HTTP" , "TCP" };
        baseXConnectionTypeComboBox = new JComboBox<>(connectionTypes);
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXConnectionTypeComboBox, c);*/

        c.gridx = 0;
        c.gridy++;
        panel.add(new JSeparator(JSeparator.HORIZONTAL), c);
        /**
         * BaseX Versioning
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXVersioningCheckBoxLbl = new JLabel("Version Control:");
        panel.add(baseXVersioningCheckBoxLbl, c);

        baseXVersioningCheckBox = new JCheckBox();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXVersioningCheckBox, c);

        /**
         * BaseX Logfile
         */
/*
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
*/

        c.gridx = 0;
        c.gridy++;
        panel.add(new JSeparator(JSeparator.HORIZONTAL), c);
        /**
         * BaseX DB Create Options
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXDBCreateOptionsLbl = new JLabel("Options for creating new databases");
        baseXDBCreateOptionsLbl.setFont(baseXDBCreateOptionsLbl.getFont().deriveFont(Font.BOLD));
        panel.add(baseXDBCreateOptionsLbl, c);

        /**
         * BaseX DB Create Chop Option
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXDBCreateChopCheckBoxLbl = new JLabel("Chop whitespaces");
        panel.add(baseXDBCreateChopCheckBoxLbl, c);

        baseXDBCreateChopCheckBox = new JCheckBox();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXDBCreateChopCheckBox, c);

        /**
         * BaseX DB Create FTIndex Option
         */
        c.gridx = 0;
        c.gridy++;
        JLabel baseXDBCreateFTIndexCheckBoxLbl = new JLabel("Full-text index:");
        panel.add(baseXDBCreateFTIndexCheckBoxLbl, c);

        baseXDBCreateFTIndexCheckBox = new JCheckBox();
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(baseXDBCreateFTIndexCheckBox, c);


        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), c);

        /**
         * Initialize the fields with the stored options.
         */
        baseXConnectionSettingsComboBox.setSelectedIndex(0);
        baseXConnectionSettingsComboBox.setSelectedItem(baseXConnectionSetting);
        baseXHostTextField.setText(baseXHost != null ? baseXHost : "");
/*        baseXHttpPortTextField.setText(baseXHttpPort != null ? baseXHttpPort : "");*/
/*        baseXTcpPortTextField.setText(baseXTcpPort != null ? baseXTcpPort : "");*/
        baseXUsernameTextField.setText(baseXUsername != null ? baseXUsername : "");
        baseXPasswordTextField.setText(baseXPassword != null ? baseXPassword : "");
/*        if (baseXConnection.equals(connectionTypes[1])) {
            baseXConnectionTypeComboBox.setSelectedItem(connectionTypes[1]);
        } else {
            baseXConnectionTypeComboBox.setSelectedItem(connectionTypes[0]);
        }*/
        baseXVersioningCheckBox.setSelected(Boolean.parseBoolean(baseXVersioning));
/*        baseXLogfileTextField.setText(baseXLogfile != null ? baseXLogfile : "");*/
        baseXDBCreateChopCheckBox.setSelected(Boolean.parseBoolean(baseXDBCreateChop));
        baseXDBCreateFTIndexCheckBox.setSelected(Boolean.parseBoolean(baseXDBCreateFTIndex));

        return panel;
    }

    public static String getOption(String key, boolean defaults) {
        String defaultValue;
        switch (key) {
            case KEY_BASEX_CONNECTION_SETTING: defaultValue = DEF_BASEX_CONNECTION_SETTING; break;
            case KEY_BASEX_HOST: defaultValue = DEF_BASEX_HOST; break;
            case KEY_BASEX_HTTP_PORT: defaultValue = DEF_BASEX_HTTP_PORT; break;
            case KEY_BASEX_TCP_PORT: defaultValue = DEF_BASEX_TCP_PORT; break;
            case KEY_BASEX_USERNAME: defaultValue = DEF_BASEX_USERNAME; break;
            case KEY_BASEX_PASSWORD: defaultValue = DEF_BASEX_PASSWORD; break;
            case KEY_BASEX_CONNECTION: defaultValue = DEF_BASEX_CONNECTION; break;
            case KEY_BASEX_VERSIONING: defaultValue = DEF_BASEX_VERSIONING; break;
            case KEY_BASEX_LOGFILE: defaultValue = DEF_BASEX_LOGFILE; break;
            case KEY_BASEX_DB_CREATE_CHOP: defaultValue = DEF_BASEX_DB_CREATE_CHOP; break;
            case KEY_BASEX_DB_CREATE_FTINDEX: defaultValue = DEF_BASEX_DB_CREATE_FTINDEX; break;
            default: defaultValue = "empty option";
        }
        if (defaults) {
            return defaultValue;
        } else {
            PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
            if (pluginWorkspace != null) {
                WSOptionsStorage store = pluginWorkspace.getOptionsStorage();
                if (store != null)
                    return store.getOption(key, defaultValue);
                else {
                    logger.error("Plugin error - no option storage accessible");
                    return defaultValue;
                }
            } else {
                logger.error("Plugin error - no plugin workspace accessible");
                return defaultValue;
            }
        }
    }

    public static List loadConnectionSettings() {
        List<String[]> connectionSettings = new ArrayList<>();
        String[] conn = {"default" , DEF_BASEX_HOST, DEF_BASEX_USERNAME, DEF_BASEX_PASSWORD};
        connectionSettings.add(conn);
        File settingsPath = new File(CONNECTION_SETTING_PATH);
        if (FileUtils.directoryExists(settingsPath)) {
            String[] fileList = settingsPath.list(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String fileName) {
                    return fileName.toLowerCase().endsWith(CONNECTION_SETTING_FILE_TYPE);
                }
            });
            if (fileList != null) {
                for (String settingsFile : fileList) {
                    Properties properties = new Properties();
                    try (InputStream in = new BufferedInputStream(
                            new FileInputStream(CONNECTION_SETTING_PATH + "/" + settingsFile))) {
                        properties.load(in);
                        String[] connSetting = {properties.getProperty(CS_NAME), properties.getProperty(CS_HOST),
                                properties.getProperty(CS_USER), properties.getProperty(CS_PWD)};
                        connectionSettings.add(connSetting);
                    } catch (IOException ioe) {
                        logger.debug(ioe.getMessage());
                    }
                }
            }
        }
        return connectionSettings;
    }

    public void storeConnectionSettings() {
        File settingsPath = new File(CONNECTION_SETTING_PATH);
        boolean noDirectory = false;
        if (!FileUtils.directoryExists(settingsPath)) {
            if (!settingsPath.mkdir()) {
                noDirectory = true;
                PluginWorkspaceProvider.getPluginWorkspace().
                        showErrorMessage("Couldn't create config directory 'argon' in user home path.\n" +
                                "Please create it manually to store connection settings permanently.");
            }
        }
        if (!noDirectory) {
            String fileName = CONNECTION_SETTING_PATH + "/" +
                    baseXConnectionSettingsComboBox.getSelectedItem() + CONNECTION_SETTING_FILE_TYPE;
            File settingsFile = new File(fileName);
            Properties properties = new Properties();
            properties.setProperty(CS_NAME, (String) baseXConnectionSettingsComboBox.getSelectedItem());
            properties.setProperty(CS_HOST, baseXHostTextField.getText());
            properties.setProperty(CS_USER, baseXUsernameTextField.getText());
            properties.setProperty(CS_PWD, baseXPasswordTextField.getText());
            try (FileOutputStream fileOut = new FileOutputStream(settingsFile)) {
                properties.store(fileOut, "Favorite Things");
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
                PluginWorkspaceProvider.getPluginWorkspace().
                        showErrorMessage("Couldn't store connection settings to file\n" + fileName);
            }
        }
    }


}

