package de.axxepta.oxygen.selection;

import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.exml.plugin.selection.SelectionPluginExtension;
import ro.sync.exml.plugin.selection.SelectionPluginResult;
import ro.sync.exml.plugin.selection.SelectionPluginResultImpl;

/**
 * Created by Markus on 09.10.2015.
 */
public class ReplyToCommentSelectionPlugin implements SelectionPluginExtension {

    public SelectionPluginResult process(SelectionPluginContext selectionPluginContext) {
        // ToDO: export part of ReplyAuthorCommentAction as subroutine, call it here to add response to comments from context menu
        // ToDo: set name of entry in context menu
        String sel = selectionPluginContext.getSelection();
        return new SelectionPluginResultImpl(sel);
    }

}
