package de.axxepta.oxygen.actions;
import de.axxepta.oxygen.customprotocol.ArgonEditorsWatchMap;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.versioncontrol.VersionHistoryTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus on 02.02.2016.
 */
public class CompareVersionsAction extends AbstractAction {

    private final JTable table;
    private static final Logger logger = LogManager.getLogger(CompareVersionsAction.class);

    public CompareVersionsAction(String name, JTable table) {
        super(name);
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        URL[] urls = selectURLs();
        openDiffer(urls);
    }

    private URL[] selectURLs() {
        URL[] urls = new URL[2];
        int[] selection = table.getSelectedRows();
        int rows = table.getModel().getRowCount();
        // if only one revision row is selected, it is compared to the last (current) one
        if (selection.length == 1) {
            urls[0] = ((VersionHistoryTableModel) table.getModel()).getURL(rows - 1);
            urls[1] = ((VersionHistoryTableModel) table.getModel()).getURL(selection[0]);
            // if a changed instance of the file is currently opened in an editor, save it and use it's URL
            URL currentURL = obtainCurrentURLFromHistoryURL(urls[0]);
            if (ArgonEditorsWatchMap.isURLInMap(currentURL)) {
                WSEditor editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                        getEditorAccess(currentURL, PluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess.isModified())
                    editorAccess.save();
                urls[0] = currentURL;
            }
        } else {
            urls[0] = ((VersionHistoryTableModel) table.getModel()).getURL(selection[1]);
            urls[1] = ((VersionHistoryTableModel) table.getModel()).getURL(selection[0]);
        }
        return urls;
    }

    private void openDiffer(URL[] urls) {
        String urlString = urls[0].getClass().toString();
        String className = "ro.sync.diff.ui.DiffFilesMainFrame";
        try {
            Class<?> clazz = Class.forName(className);
            try {
                Method[] methods = clazz.getDeclaredMethods();
                Method getInstanceMethod = null;
                Method invokerMethod = null;
                for (Method method : methods) {
                    if (method.getReturnType().toString().equals(clazz.toString()) &&
                            Modifier.toString(method.getModifiers()).contains("static"))
                    {
                        getInstanceMethod = method;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if ((parameterTypes.length == 2) && parameterTypes[0].toString().equals(urlString) &&
                            parameterTypes[1].toString().equals(urlString))
                    {
                        invokerMethod = method;
                    }
                }
                if ((getInstanceMethod != null) && (invokerMethod != null)) {
                    getInstanceMethod.setAccessible(true);
                    invokerMethod.setAccessible(true);
                    try {
                        Object fileDiffer = getInstanceMethod.invoke(null);
                        invokerMethod.invoke(fileDiffer, urls[0], urls[1]);
                    } catch(IllegalAccessException iae) {
                        logger.error("No access granted to getInstance method of File Differ!");
                    } catch(InvocationTargetException ite) {
                        logger.error("Target of getInstance method of File Differ not set!");
                    }
                } else {
                    logger.error("getInstance method of File Differ not found!");
                }
            } catch (SecurityException se) {
                logger.error("No access granted to File Differ methods!");
            }
        } catch (ClassNotFoundException cnfe) {
            logger.error("File Differ Class not found!");
        }
    }

    private URL obtainCurrentURLFromHistoryURL(URL url) {
        URL currentURL;
        StringBuilder urlStr = new StringBuilder(url.toString());
        int endOfProtocolPos = urlStr.indexOf(":");
        int endOfDBNamePos = urlStr.indexOf("/", endOfProtocolPos + 2);
        if (urlStr.substring(endOfProtocolPos + 2, endOfDBNamePos + 1).equals(BaseXByteArrayOutputStream.backupRepoBase)) {
            urlStr.delete(endOfProtocolPos + 1, endOfDBNamePos);
            urlStr.replace(0,endOfProtocolPos, CustomProtocolURLHandlerExtension.ARGON_REPO);
        } else if (urlStr.substring(endOfProtocolPos + 2, endOfDBNamePos + 1).equals(BaseXByteArrayOutputStream.backupRESTXYBase)) {
            urlStr.delete(endOfProtocolPos + 1, endOfDBNamePos);
            urlStr.replace(0,endOfProtocolPos, CustomProtocolURLHandlerExtension.ARGON_XQ);
        } else {
            urlStr.delete(endOfProtocolPos + 2, endOfProtocolPos + 2 + BaseXByteArrayOutputStream.backupDBBase.length());
        }
        int dotPos = urlStr.lastIndexOf(".");
        int endOfFileNamePos = urlStr.lastIndexOf("-", dotPos);
        endOfFileNamePos = urlStr.lastIndexOf("-", endOfFileNamePos - 1);
        endOfFileNamePos = urlStr.lastIndexOf("_", endOfFileNamePos - 1);
        urlStr.delete(endOfFileNamePos, dotPos);
        try {
            currentURL = new URL(urlStr.toString());
        } catch (MalformedURLException e1) {
            logger.error(e1);
            currentURL = url;
        }
        return currentURL;
    }
}
