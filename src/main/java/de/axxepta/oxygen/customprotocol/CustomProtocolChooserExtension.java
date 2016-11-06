package de.axxepta.oxygen.customprotocol;

import java.awt.*;
import java.net.URL;

import javax.swing.Icon;

import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin extension - custom protocol chooser extension
 */
public class CustomProtocolChooserExtension implements URLChooserPluginExtension2, URLChooserToolbarExtension {

    /**
    * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2#chooseURLs(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
    */
    public URL[] chooseURLs(StandalonePluginWorkspace workspaceAccess) {
        ArgonChooserDialog urlChooser = new ArgonChooserDialog((Frame)workspaceAccess.getParentFrame(),
                Lang.get(Lang.Keys.dlg_open), ArgonChooserDialog.Type.OPEN);
        return urlChooser.selectURLs();
    }

    /**
    * @return A menu name.
    */
    public String getMenuName() {
        return Lang.get(Lang.Keys.dlg_open);
    }

    /**
    * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension#getToolbarIcon()
    */
    public Icon getToolbarIcon() {
        return ImageUtils.getIcon(ImageUtils.BASEX24);
    }

    /**
    * @see ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension#getToolbarTooltip()
    */
    public String getToolbarTooltip() {
        return Lang.get(Lang.Keys.dlg_open);
    }
}