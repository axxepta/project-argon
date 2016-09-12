package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.customprotocol.ArgonChooserDialog;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.utils.WorkspaceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorParentNode;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPageBase;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;

import static ro.sync.ecss.extensions.api.node.AuthorNode.NODE_TYPE_CDATA;
import static ro.sync.ecss.extensions.api.node.AuthorNode.NODE_TYPE_COMMENT;
import static ro.sync.ecss.extensions.api.node.AuthorNode.NODE_TYPE_PI;

/**
 * @author Markus on 26.07.2016.
 */
public class StoreSnippetSelectionAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(StoreSnippetSelectionAction.class);
    private final PluginWorkspace workspace = PluginWorkspaceProvider.getPluginWorkspace();

    @Override
    public void actionPerformed(ActionEvent e) {
        String selection = "";
        WSEditor editorAccess = workspace.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
            WSEditorPage editorPage = editorAccess.getCurrentPage();
            AuthorAccess authorAccess = ((WSAuthorEditorPage) editorPage).getAuthorAccess();
            //selection = authorAccess.getEditorAccess().getSelectedText();
            WSAuthorEditorPageBase baseAccess = authorAccess.getEditorAccess();
            int[] nodeSelection = baseAccess.
                    getBalancedSelection(baseAccess.getBalancedSelectionStart(), baseAccess.getBalancedSelectionEnd());
            AuthorParentNode selectedNode = (AuthorParentNode) baseAccess.
                    getFullySelectedNode(nodeSelection[0], nodeSelection[1]);
            try {
                selection = getAuthorText(((WSAuthorEditorPage) editorPage).getDocumentController(), selectedNode, 0);
            } catch (BadLocationException ble) {
                logger.error("Error while parsing XML from AuthorPage selection: ", ble.getMessage());
            }
        } else if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_TEXT)) {
            WSTextEditorPage textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
            selection = textPage.getSelectedText();
        } else return;

        String[] buttons = { "File", "Argon", "Cancel"};
        int[] responseIDs = { 0, 1, -1};
        int saveTo = workspace.showConfirmDialog("Save Snippet", "Save snippet to", buttons, responseIDs, 0);
        if (saveTo == -1)
            return;
        if (saveTo == 0)
            saveToFile(selection);
        else
            saveToArgon(selection);
    }

    private void saveToArgon(String text) {
        ArgonChooserDialog urlChooser = new ArgonChooserDialog((Frame)workspace.getParentFrame(),
                "Save File via BaseX Database Connection", ArgonChooserDialog.SAVE);
        URL[] urls =  urlChooser.selectURLs();
        if (urls != null) {
            URL url = urls[0];
            BaseXSource source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
            String path = CustomProtocolURLHandlerExtension.pathFromURL(url);
            if (WorkspaceUtils.newResourceOrOverwrite(source, path)) {
                try {
                    byte[] bytes = text.getBytes("UTF-8");
                    try {
                        ConnectionWrapper.save(url, bytes, "UTF-8");
                    } catch (IOException ioe) {
                        workspace.showErrorMessage("Failed to store snippet to BaseX: " + ioe.getMessage());
                    }

                } catch (UnsupportedEncodingException use) {
                    logger.error(use.getMessage());
                }
            }
        }
    }

    private void saveToFile(String text) {
        File file = workspace.chooseFile(null, "Save Snippet to File",  new String[] {"*"}, "All Files", true);
        if (file != null) {
            try {
                storeString(text, file);
            } catch (IOException ioe) {
                workspace.showErrorMessage("Failed to store snippet to File: " + ioe.getMessage());
            }
        }
    }

    private static void storeString(String text, File file) throws IOException {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getAuthorText(AuthorDocumentController ctrl, AuthorParentNode parent, int depth)
            throws BadLocationException {
        StringBuilder authorTextBuilder = new StringBuilder("");
        Segment seg = new Segment(new char[1024], 0, 1024);
        java.util.List<AuthorNode> contentNodes = parent.getContentNodes();
        AuthorNode prev = null;

        // tag of parent node
        if (depth == 0) {
            authorTextBuilder.append(getStartTag(parent));
        }

        for (AuthorNode authorNode : contentNodes) {
            if (prev == null) {
                // First child. A text can exist between the parent start and the child start.
                if (parent.getStartOffset() + 1 < authorNode.getStartOffset()) {
                    ctrl.getChars(
                            parent.getStartOffset() + 1,
                            authorNode.getStartOffset() - (parent.getStartOffset() + 1),
                            seg);
                    // We have a text here.
                    authorTextBuilder.append(seg);
                }
            } else {
                // A text can exist between the two brothers.
                if (prev.getEndOffset() + 1 < authorNode.getStartOffset()) {
                    ctrl.getChars(prev.getEndOffset() + 1, authorNode.getStartOffset() - prev.getEndOffset() - 1, seg);
                    // A text is between the two brothers.
                    authorTextBuilder.append(seg);
                }
            }

            authorTextBuilder.append(getStartTag(authorNode));
            if (authorNode instanceof AuthorParentNode) {
                authorTextBuilder.append(getAuthorText(ctrl, (AuthorParentNode) authorNode, depth+1));
            }
            authorTextBuilder.append(getEndTag(authorNode));

            prev = authorNode;
        }

        if (prev != null && parent.getEndOffset() > prev.getEndOffset() + 1) {
            // The last node. A text between him and the end of the parent.
            ctrl.getChars(prev.getEndOffset() + 1, parent.getEndOffset() - 1 - prev.getEndOffset(), seg);
            // A text is between the two brothers.
            authorTextBuilder.append(seg);
        }

        if (contentNodes.size() == 0) {
            authorTextBuilder.append(parent.getTextContent());
        }

        if (depth == 0)
            authorTextBuilder.append(getEndTag(parent));

        return authorTextBuilder.toString();
    }

    private String getStartTag(AuthorNode node) {
        StringBuilder tagBuilder = new StringBuilder("");
        tagBuilder.append("<");
        switch (node.getType()) {
            case NODE_TYPE_PI: {
                tagBuilder.append("?"); break;
            }
            case NODE_TYPE_COMMENT: {
                tagBuilder.append("!--"); break;
            }
            case NODE_TYPE_CDATA: {
                tagBuilder.append("![CDATA["); break;
            }
            default : {
                tagBuilder.append(node.getName());
                if (node instanceof AuthorElement) {
                    int nAttributes = ((AuthorElement) node).getAttributesCount();
                    for (int att = 0; att < nAttributes; att++) {
                        String attributeName = ((AuthorElement) node).getAttributeAtIndex(att);
                        String attributeValue = ((AuthorElement) node).getAttribute(attributeName).getValue();
                        tagBuilder.append(" ").append(attributeName).append("=\"").append(attributeValue).append("\"");
                    }
                }
                tagBuilder.append(">");
            }
        }
        return tagBuilder.toString();
    }

    private String getEndTag(AuthorNode node) {
        switch (node.getType()) {
            case NODE_TYPE_PI: return "?>";
            case NODE_TYPE_COMMENT: return "-->";
            case NODE_TYPE_CDATA: return "]]>";
            default: return "</" + node.getName() + ">";
        }
    }

}
