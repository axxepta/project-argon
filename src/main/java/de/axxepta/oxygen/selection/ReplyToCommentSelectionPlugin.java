package de.axxepta.oxygen.selection;

import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.exml.plugin.selection.SelectionPluginExtension;
import ro.sync.exml.plugin.selection.SelectionPluginResult;
import ro.sync.exml.plugin.selection.SelectionPluginResultImpl;

import javax.swing.*;
import javax.swing.text.BadLocationException;

/**
 * Selection plugin to respond to comments in text mode - select whole comment and use context menu/plugin/argon
 */
public class ReplyToCommentSelectionPlugin implements SelectionPluginExtension {

    public SelectionPluginResult process(SelectionPluginContext selectionPluginContext) {
        // ToDo: set name of entry in context menu
        String selection = selectionPluginContext.getSelection();

        StringBuilder modified = new StringBuilder(selection);
        int commentStart = selection.lastIndexOf("<?oxy_comment_start");
        if (commentStart != -1) {
            int commentEnd = selection.indexOf("<?oxy_comment_end", commentStart);
            if (commentEnd > commentStart) {
                // identify position of end of comment attribute in processing instruction
                int endCommentEnd = selection.indexOf("comment=", commentStart);
                if (endCommentEnd > commentEnd) {
                    endCommentEnd = selection.indexOf("\"", endCommentEnd+9);
                    if (endCommentEnd != -1 ) {
                        String reply = JOptionPane.showInputDialog(null, "Reply to comment:", "Review", JOptionPane.PLAIN_MESSAGE);
                        modified.insert(endCommentEnd, " --------------------------- "
                                +"Response [" + System.getProperty("user.name") + "]: " + reply);
                    }
                }
            }
        }

        return new SelectionPluginResultImpl(modified.toString());
    }

}
