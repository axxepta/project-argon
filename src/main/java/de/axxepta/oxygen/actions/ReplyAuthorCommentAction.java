package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.selection.ReplyToCommentSelectionPlugin;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.exml.plugin.selection.SelectionPluginExtension;
import ro.sync.exml.plugin.selection.SelectionPluginResult;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.WSTextBasedEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;

/**
 * Created by Markus on 08.10.2015.
 */
public class ReplyAuthorCommentAction extends AbstractAction {

    private StandalonePluginWorkspace pluginWorkspaceAccess;

    public ReplyAuthorCommentAction(final StandalonePluginWorkspace pluginWorkspaceAccess){
        this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
            WSEditorPage editorPage = editorAccess.getCurrentPage();
            AuthorAccess authorAccess = ((WSAuthorEditorPage) editorPage).getAuthorAccess();
            // store current cursor position for later reset
            int currentOnset = authorAccess.getEditorAccess().getSelectionStart();
            int currentOffset = authorAccess.getEditorAccess().getCaretOffset();
            // change to Text mode
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
            WSTextEditorPage textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
            // check whether current cursor position is in a comment
            int currentPos = textPage.getSelectionStart();
            Document doc = textPage.getDocument();
            String docStr;
            try {
                docStr = doc.getText(0,doc.getLength()-1);
            } catch (BadLocationException ex) {
                docStr = "";
            }
            int commentStart = docStr.lastIndexOf("<?oxy_comment_start",currentPos);
            if (commentStart != -1) {
                int commentEnd = docStr.indexOf("<?oxy_comment_end", commentStart);
                if (commentEnd > currentPos) {
                    // identify position of end of comment attribute in processing instruction
                    int endCommentEnd = docStr.indexOf("comment=", commentStart);
                    endCommentEnd = docStr.indexOf("\"", endCommentEnd+9);
                    // add response to comment, hiding that you're working in text mode
                    editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
                    String reply = JOptionPane.showInputDialog(null, "Reply to comment:", "Review", JOptionPane.PLAIN_MESSAGE);
                    editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
                    if (reply != null) {
                        // ToDo: add message?
                        try {
                            doc.insertString(endCommentEnd, " --------------------------- "
                                    +"Response [" + System.getProperty("user.name") + "]: " + reply, null);
                        } catch (BadLocationException ex) {/*ToDo? should be in safe range*/}
                    }
                }
            }
            // back to Author mode
            editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
            ((WSAuthorEditorPage) editorPage).select(currentOnset, currentOffset);

        }
    }
}