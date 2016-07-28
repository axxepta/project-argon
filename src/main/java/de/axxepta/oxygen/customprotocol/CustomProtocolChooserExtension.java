package de.axxepta.oxygen.customprotocol;

import java.awt.*;
import java.net.URL;

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
        ArgonChooserDialog urlChooser = new ArgonChooserDialog((Frame)workspaceAccess.getParentFrame(),
                "Open File via BaseX Database Connection", ArgonChooserDialog.OPEN);
        return urlChooser.selectURLs();
    }

    /**
    * @return A menu name.
    */
    public String getMenuName() {
        return "Open using Argon Protocol";
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
        return "Open with Argon Protocol";
    }
}