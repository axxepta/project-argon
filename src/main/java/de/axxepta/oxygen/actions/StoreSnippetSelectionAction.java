package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus on 26.07.2016.
 */
public class StoreSnippetSelectionAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(StoreSnippetSelectionAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();
        String selection;
        WSEditor editorAccess = workspace.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
            WSEditorPage editorPage = editorAccess.getCurrentPage();
            AuthorAccess authorAccess = ((WSAuthorEditorPage) editorPage).getAuthorAccess();
            selection = authorAccess.getEditorAccess().getSelectedText();
        } else if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_TEXT)) {
            WSTextEditorPage textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
            selection = textPage.getSelectedText();
        } else return;

        JFrame parentFrame = (JFrame) (new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame();
        JDialog storeSnippetDialog = DialogTools.getOxygenDialog(parentFrame, "Store snippet");

        JPanel content = new JPanel(new BorderLayout(10,10));

        JPanel namePanel = new JPanel(new GridLayout());
        JLabel nameLabel = new JLabel("URL", JLabel.LEFT);
        namePanel.add(nameLabel);
        JTextField newFileNameTextField = new JTextField();
        //newFileNameTextField.getDocument().addDocumentListener(new FileNameFieldListener(newFileNameTextField, false));
        namePanel.add(newFileNameTextField);
        content.add(namePanel, BorderLayout.NORTH);

        Action saveFile = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String urlString = newFileNameTextField.getText();
                if (urlString.startsWith("argon")) {
                    BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURLString(urlString);
                    try {
                        byte[] bytes = selection.getBytes("UTF-8");
                        try {
                            URL url = new URL(urlString);
                            try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url)) {
                                os.write(bytes);
                            } catch (IOException ioe) {
                                workspace.showErrorMessage("Failed to store snippet to BaseX: " + ioe.getMessage());
                            }
                        } catch (MalformedURLException mue) {
                            workspace.showErrorMessage("Malformed URL");
                        }
                    } catch (UnsupportedEncodingException use) {
                        logger.error(use.getMessage());
                    }
                } else {
                    try {
                        storeString(selection, urlString);
                    } catch (IOException ioe) {
                        workspace.showErrorMessage("Failed to store snippet to file: " + ioe.getMessage());
                    }
                }
                storeSnippetDialog.dispose();
            }
        };

        newFileNameTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        newFileNameTextField.getActionMap().put("confirm", saveFile);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton(saveFile);
        btnPanel.add(addBtn, BorderLayout.WEST);
        JButton cancelBtn = new JButton(new CloseDialogAction(Lang.get(Lang.Keys.cm_cancel), storeSnippetDialog));
        btnPanel.add(cancelBtn, BorderLayout.EAST);
        content.add(btnPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(storeSnippetDialog, content, parentFrame);
    }


    private static void storeString(String text, String filePath) throws IOException {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
