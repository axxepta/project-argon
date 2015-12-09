package de.axxepta.oxygen.actions;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Markus on 04.12.2015.
 * This action should be called from a toolbar button only if an argon protocol file is opened in the current editor
 * and initiates an update of the version (and revision) number and storage of the updated file to BaseX.
 */
public class NewVersionButtonAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public NewVersionButtonAction(String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
