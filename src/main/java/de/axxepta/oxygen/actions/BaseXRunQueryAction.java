package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.DialogTools;
import de.axxepta.oxygen.utils.Lang;
import de.axxepta.oxygen.utils.URLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.editor.ContentTypes;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 08.10.2015.
 */
public class BaseXRunQueryAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(BaseXRunQueryAction.class);
    private static final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    private static final String COMMENT_ON = "(:";
    private static final String COMMENT_OFF = ":)";

    public BaseXRunQueryAction (String name, Icon icon){
        super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        WSEditor editorAccess = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);

        if (editorAccess != null) {
            if (URLUtils.isQuery(editorAccess.getEditorLocation())) {

                StringBuilder contentBuilder = new StringBuilder();
                String[] parameterNames = getContentAndExtractExtVars(editorAccess, contentBuilder);
                String editorContent = contentBuilder.toString();

                String[] arguments;
                if (parameterNames.length > 0) {
                    List<String> argumentList = askForArguments(parameterNames);
                    if (argumentList.size() == 0)
                        return;
                    arguments = argumentList.toArray(new String[argumentList.size()]);
                } else
                    arguments = new String[0];

                String queryRes;
                try {
                    queryRes = ConnectionWrapper.query(editorContent, arguments);
                } catch (Exception er) {
                    logger.error("query to BaseX failed");
                    queryRes = "";
                }

                workspace.createNewEditor("xml", ContentTypes.XML_CONTENT_TYPE, queryRes);

            } else {
                workspace.showInformationMessage(Lang.get(Lang.Keys.msg_noquery));
            }

        } else {
            workspace.showInformationMessage(Lang.get(Lang.Keys.msg_noeditor));
        }
    }

    private static String[] getContentAndExtractExtVars(WSEditor editorAccess, StringBuilder content) {
        List<String> extVars = new ArrayList<>();
        MutableBoolean inComment = new MutableBoolean(false);
        try (InputStream editorStream = editorAccess.createContentInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(editorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    extractVarFromLine(extVars, line, inComment);
                    content.append(line).append("\n");
                }
            }
        } catch (IOException ioe) {
            logger.error("Failed extracting editor content: ", ioe.getMessage());
        }
        return extVars.toArray(new String[extVars.size()]);
    }

    private static void extractVarFromLine(List<String> extVars, String line, MutableBoolean inComment) {
        if (inComment.val) {
            if (line.contains(COMMENT_OFF)) {
                line = line.substring(line.indexOf(COMMENT_OFF) + 2);
                inComment.val = false;
            }
        }
        if (line.contains(COMMENT_ON)) {
            if (line.contains(COMMENT_OFF)) {
                String comment = line.substring(line.indexOf(COMMENT_ON), line.indexOf(COMMENT_OFF) + 2);
                line = line.replace(comment, "");
            } else {
                inComment.val = true;
            }
        }
        if (line.contains("declare variable") && line.contains(" external;")) {
            String[] words = line.split(" ");
            for (String word : words) {
                if (word.startsWith("$")) {
                    extVars.add(word);
                    break;
                }
            }
        }
    }

    private List<String> askForArguments(String[] parameters) {
        List<String> arguments = new ArrayList<>();

        JFrame parentFrame = (JFrame) (new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame();
        JDialog parametersDialog = DialogTools.getOxygenDialog(parentFrame, Lang.get(Lang.Keys.dlg_externalquery));

        JPanel content = new JPanel(new BorderLayout(10,10));

        JPanel parameterPanel = new JPanel(new GridLayout(parameters.length, 2));
        JTextField[] parameterTextFields = new JTextField[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterPanel.add(new JLabel(parameters[i]));
            parameterTextFields[i] = new JTextField();
            parameterPanel.add(parameterTextFields[i]);
        }
        content.add(parameterPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton(Lang.get(Lang.Keys.cm_ok));
        okBtn.addActionListener(e -> {
            for (int i = 0; i < parameters.length; i++) {
                arguments.add(parameters[i]);
                arguments.add(parameterTextFields[i].getText());
                parametersDialog.dispose();
            }
        });
        btnPanel.add(okBtn, BorderLayout.WEST);
        JButton cancelBtn = new JButton(Lang.get(Lang.Keys.cm_cancel));
        cancelBtn.addActionListener(e -> parametersDialog.dispose());
        btnPanel.add(cancelBtn, BorderLayout.EAST);
        content.add(btnPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(parametersDialog, content, parentFrame);
        return arguments;
    }


    private static class MutableBoolean {
        boolean val;
        MutableBoolean(boolean value) {
            this.val = value;
        }
    }

}
