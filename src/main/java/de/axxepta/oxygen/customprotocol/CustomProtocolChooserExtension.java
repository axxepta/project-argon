package de.axxepta.oxygen.customprotocol;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.ui.Icons;

/**
 * Plugin extension - custom protocol chooser extension
 */
public class CustomProtocolChooserExtension implements URLChooserPluginExtension2, URLChooserToolbarExtension {
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
	
    String argonURL = "argon:/Users/daltiparmak/zilicon/develop/app/ionic/config.xml";
    try {
		selectedURLs.add(new URL(argonURL));
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