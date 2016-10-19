package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.utils.URLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.editor.ContentTypes;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Markus on 08.10.2015.
 */
public class BaseXRunQueryAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(BaseXRunQueryAction.class);
    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    public BaseXRunQueryAction (String name, Icon icon){
        super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        WSEditor editorAccess = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);

        if (editorAccess != null) {
            if (URLUtils.isQuery(editorAccess.getEditorLocation())) {
                // get content of current editor window
                String editorContent;
                try {
                    InputStream editorStream = editorAccess.createContentInputStream();
                    Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
                    editorContent = s.hasNext() ? s.next() : "";
                    editorStream.close();
                } catch (IOException er) {
                    logger.error(er);
                    editorContent = "";
                }

                // parse query
                String queryRes;
                try {
                    queryRes = (new BaseXRequest("query", null, editorContent)).getAnswer();
                } catch (Exception er) {
                    logger.error("query to BaseX failed");
                    queryRes = "";
                }

                // display result of query in a new editor window
                workspace.createNewEditor("xml", ContentTypes.XML_CONTENT_TYPE, queryRes);

            } else {
                workspace.showInformationMessage("No XQuery in editor window!");
            }

        } else {
            workspace.showInformationMessage("No editor window opened!");
        }
    }

}
