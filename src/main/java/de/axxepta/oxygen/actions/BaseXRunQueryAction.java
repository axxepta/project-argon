package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.rest.BaseXRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.editor.ContentTypes;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Markus on 08.10.2015.
 */
public class BaseXRunQueryAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;
    private static final Logger logger = LogManager.getLogger(BaseXRunQueryAction.class);

    public BaseXRunQueryAction (StandalonePluginWorkspace pluginWorkspaceAccess) {
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    public BaseXRunQueryAction (String name, Icon icon, final StandalonePluginWorkspace pluginWorkspaceAccess){
        super(name, icon);
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);

        if (editorAccess != null) {
            URL editorUrl = editorAccess.getEditorLocation();
            boolean isXquery = (
                    editorUrl.toString().endsWith("xq") ||
                    editorUrl.toString().endsWith("xqm") ||
                    editorUrl.toString().endsWith("xql") ||
                    editorUrl.toString().endsWith("xqy") ||
                    editorUrl.toString().endsWith("xquery"));
            if (isXquery) {
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
                // get database name of current editor window
                int startInd = editorUrl.toString().indexOf(":");
                int stopInd = editorUrl.toString().indexOf("/", startInd+2);
                //ToDo: catch unexpected error that argon URL is malformed
                String db_name = editorUrl.toString().substring(startInd+1, stopInd);
                // pass content of editor window to ListDBEntries with queryRun
                String queryRes;

                try {
                    queryRes = (new BaseXRequest("query", null, editorContent)).getAnswer();
                } catch (Exception er) {
                    logger.error("query to BaseX failed");
                    queryRes = "";
                }

/*                try {
                    ListDBEntries testQuery = new ListDBEntries("queryRun", db_name, editorContent);
                    queryRes = testQuery.getAnswer();
                } catch (Exception er) {
                    logger.error("query to BaseX failed");
                    queryRes = "";
                }*/

                //+ display result of query in a new info window
                //argonOutputArea.setText(queryRes);
                //pluginWorkspaceAccess.showView("ArgonWorkspaceAccessOutputID", true);

                //+ display result of query in a new editor window
                pluginWorkspaceAccess.createNewEditor("txt", ContentTypes.PLAIN_TEXT_CONTENT_TYPE,queryRes);

            } else {
                pluginWorkspaceAccess.showInformationMessage("No XQuery in editor window!");
            }

        } else {
            pluginWorkspaceAccess.showInformationMessage("No editor window opened!");
        }
    }

}
