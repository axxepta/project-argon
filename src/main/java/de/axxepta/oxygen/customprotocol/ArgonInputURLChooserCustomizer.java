package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.utils.ImageUtils;
import de.axxepta.oxygen.utils.Lang;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.InputURLChooser;
import ro.sync.exml.workspace.api.standalone.InputURLChooserCustomizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.List;

/**
 * @author Markus on 29.10.2016.
 */
public class ArgonInputURLChooserCustomizer implements InputURLChooserCustomizer {
    @Override
    public void customizeBrowseActions(List<Action> list, InputURLChooser inputURLChooser) {
        Action browseCMS = new AbstractAction("Argon BaseX", ImageUtils.getIcon(ImageUtils.BASEX)) {
            public void actionPerformed(ActionEvent e) {
                ArgonChooserDialog urlChooser = new ArgonChooserDialog(
                        (Frame) PluginWorkspaceProvider.getPluginWorkspace().getParentFrame(),
                        Lang.get(Lang.Keys.dlg_open), ArgonChooserDialog.Type.OPEN);
                URL chosenResource = urlChooser.selectURLs()[0];
                if (chosenResource != null) {
                    inputURLChooser.urlChosen(chosenResource);
                }
            }
        };
        list.add(browseCMS);
    }
}
