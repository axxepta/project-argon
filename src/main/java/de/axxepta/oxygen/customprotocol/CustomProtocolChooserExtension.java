package de.axxepta.oxygen.customprotocol;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.ui.Icons;

/**
 * Plugin extension - custom protocol chooser extension
 */
public class CustomProtocolChooserExtension implements URLChooserPluginExtension2, URLChooserToolbarExtension {
  /**
   * The custom protocol name.
   */
  private static final String CPROTO = "cproto";

  /**
   * The dialog used to select the URL
   */
  @SuppressWarnings("serial")
  private static class URLChooserDialog extends JDialog {

    // The text field containing the URL of the file to be opened
    private final JTextArea selectionArea = new JTextArea();
    
    // The selected files of the dialog.
    List<URL> selectedURLs = new ArrayList<URL>();

    /**
     * Constructor for the dialog
     * @param jFrame The parent frame.
     */
    private URLChooserDialog(Frame parentFrame) {
      super(parentFrame);
      setModal(true);
      setTitle("Open Using Custom Protocol");
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      GridBagLayout layout = new GridBagLayout();
      getContentPane().setLayout(layout);
      JLabel label = new JLabel("URL:");
      label.setPreferredSize(label.getMinimumSize());
      // The text field containing the URL of the file to be opened
      selectionArea.setText(CPROTO + ":/");
      // Set the caret at the end of text
      selectionArea.setCaretPosition("cproto:/".length());
      selectionArea.setPreferredSize(new Dimension(250, 100));
      selectionArea.setBorder(BorderFactory.createEtchedBorder());

      // Opens a file chooser dialog
      JButton open = new JButton("Browse");

      JButton ok = new JButton("aufmachen");
      ok.setPreferredSize(new Dimension(80, 25));
      JButton cancel = new JButton("Cancel");
      cancel.setPreferredSize(new Dimension(80, 25));
      Container contentPane = getContentPane();

      // Add the components to the content pane
      GridBagConstraints constr = new GridBagConstraints();
      constr.gridx = 0;
      constr.gridy = 0;
      constr.weightx = 0;
      constr.gridwidth = 1;
      constr.anchor = GridBagConstraints.EAST;
      constr.insets = new Insets(3, 3, 3, 3);
      contentPane.add(label, constr);
      
      constr.anchor = GridBagConstraints.CENTER;
      constr.gridy = 1;
      constr.fill = GridBagConstraints.VERTICAL;
      contentPane.add(new JPanel(), constr);
      
      // The text field.
      constr.gridx = 1;
      constr.gridy = 0;
      constr.weightx = 1;
      constr.gridheight = 2;
      constr.gridwidth = 4;
      constr.fill = GridBagConstraints.BOTH;
      contentPane.add(selectionArea, constr);
      
      // The browse button.
      constr.gridx = 5;
      constr.gridy = 0;
      constr.weightx = 0;
      constr.gridheight = 1;
      constr.gridwidth = 1;
      constr.fill = GridBagConstraints.NONE;
      contentPane.add(open, constr);
      
      constr.gridy = 1;
      constr.fill = GridBagConstraints.VERTICAL;
      contentPane.add(new JPanel(), constr);
      
      // The OK button.
      constr.gridx = 0;
      constr.gridy = 2;
      constr.weightx = 0;
      constr.gridwidth = 1;      
      constr.fill = GridBagConstraints.NONE;
      contentPane.add(ok, constr);

      // The cancel button.
      constr.gridx = 5;
      constr.gridy = 2;
      constr.weightx = 0;
      constr.gridwidth = 1;      
      contentPane.add(cancel, constr);

      // Sets the automatic size.
      pack();

      // Centers to the screen.
      setLocationRelativeTo(null);
      
      // Action listener for the open button
      // Opens a file chooser dialog and sets the text in the field to the URL of the selected file
      open.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(true);
          int returnVal = chooser.showOpenDialog(URLChooserDialog.this);
          selectedURLs.clear();
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            StringBuffer selection = new StringBuffer();
            try {
              for (int i = 0; i < selectedFiles.length; i++) {
                String cprotoURL = CPROTO + ":" + selectedFiles[i].toURI().toURL().getPath();
                selectedURLs.add(new URL(cprotoURL));
                selection.append(cprotoURL).append("\n");
              }
              selectionArea.setText(selection.toString());
              selectionArea.requestFocus();
              // Set the caret at the end of text
              selectionArea.setCaretPosition(selectionArea.getText().length());
            } catch (MalformedURLException e1) {
              e1.printStackTrace();
            }
          }
        }
      });

      // Add listener for OK button
      // close the dialog
      ok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println(e.toString());
          canceled = false;
          setVisible(false);
        }
      });
      // Add listener for Cancel button
      // Clear the selectionArea and close the dialog
      cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          canceled = true;
          setVisible(false);
        }
      });
    }

    /**
     * Flag telling if the user canceled the operation. This
     * is done either by clicking the CANCEL button or by closing
     * the dialog from the titlebar.
     */
    private boolean canceled = true;
    
    /**
     * Shows the dialog and returns the URL from the text field
     */
    URL[] selectURLs() {
      // Assume the user cancels the operation.
      canceled = true;
      setVisible(true);
      if (canceled) {
        return null;
      } else {
        return selectedURLs.toArray(new URL[0]);
      }
    }
  }
  

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2#chooseURLs(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public URL[] chooseURLs(StandalonePluginWorkspace workspaceAccess) {
    // Create the dialog
    //URLChooserDialog urlChooser = new URLChooserDialog((Frame)workspaceAccess.getParentFrame());

    // Show the dialog and return the string from the text field
    //URL[] urls = urlChooser.selectURLs();
	 
	List<URL> selectedURLs = new ArrayList<URL>();
	selectedURLs.clear();
	
    String cprotoURL = "cproto:/Users/daltiparmak/zilicon/develop/app/ionic/config.xml";
    try {
		selectedURLs.add(new URL(cprotoURL));
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    
    return selectedURLs.toArray(new URL[0]);
  }

  /**
   * @return A menu name.
   */
  public String getMenuName() {
    return "Open using Custom Protocol";
  }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension#getToolbarIcon()
   */
  public Icon getToolbarIcon() {
    return Icons.getIcon(Icons.OPEN_CUSTOM_PROTOCOL_TOOLBAR_STRING);
  }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension#getToolbarTooltip()
   */
  public String getToolbarTooltip() {
    return "Open with Custom Protocol";
  }
}