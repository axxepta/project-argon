package de.axxepta.oxygen.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.UnsupportedEncodingException;

/**
 * @author Markus on 04.12.2015.
 * Argon type version and revision information can be updated in the passed object and returned as byte streams.
 * If the passed object is a WSEditor, as SIDE EFFECT the updating is also inserted into the document of the editor.
 * The same is true, if no object is passed to the constructor. The document is then obtained from the current editor
 *  window.
 * For XML type files version/revision information is contained in processing instructions, for Xqueries in comments
 */
public class VersionRevisionUpdater {

    private static final Logger logger = LogManager.getLogger(VersionRevisionUpdater.class);

    public static final String XML = "XML";
    public static final String XQUERY = "XQUERY";

    private Document doc = null;
    private byte[] documentBuffer;
    private StringBuilder docBuilder;
    private String type;
    private long[] verRev = {0,0};
    private boolean updated;

    private boolean fromDocument = false;
    private WSEditor editorAccess;
    private boolean editorInAuthorMode;
    private WSEditorPage editorPage;
    private WSTextEditorPage textPage;
    private int currentOnset;
    private int currentOffset;


    public VersionRevisionUpdater(String type) {
        this.fromDocument = true;
        this.editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        this.doc = getDocumentFromEditor();
        try {
            this.docBuilder = new StringBuilder(this.doc.getText(0, doc.getLength()-1));
        } catch (BadLocationException ex) {
            this.docBuilder = new StringBuilder("");
            logger.error(ex);
        }
        this.type = type;
    }

    public VersionRevisionUpdater(WSEditor editorAccess, String type) {
        this.fromDocument = true;
        this.editorAccess = editorAccess;
        this.doc = getDocumentFromEditor();
        try {
            this.docBuilder = new StringBuilder(this.doc.getText(0, doc.getLength()-1));
        } catch (BadLocationException ex) {
            this.docBuilder = new StringBuilder("");
            logger.error(ex);
        }
        this.type = type;
    }

    public VersionRevisionUpdater(byte[] documentBuffer, String type) {
        this.documentBuffer = documentBuffer;
        try {
            this.docBuilder = new StringBuilder(new String(this.documentBuffer, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            this.docBuilder = new StringBuilder("");
            logger.error(ex);
        }
        this.type = type;
    }

    public byte[] updateVersion() {
        if (!updated) {
            int[] historyTagPosition = obtainVersionAndRevision();
            if (fromDocument) {
                // see ReplyAuthorCommentAction for how to change document
                resetEditor();
            }
            updated = true;
        }
        return documentBuffer;
    }

    public byte[] updateRevision() {
        if (!updated) {

            if (fromDocument) {
                // see ReplyAuthorCommentAction for how to change document
                resetEditor();
            }
            updated = true;
        }
        return documentBuffer;
    }

    public long[] getVersionAndRevision() {
        return verRev;
    }

    private Document getDocumentFromEditor() {
        editorInAuthorMode = false;
        if (editorAccess.getCurrentPageID().equals(EditorPageConstants.PAGE_AUTHOR)) {
            editorInAuthorMode = true;
            editorPage = editorAccess.getCurrentPage();
            AuthorAccess authorAccess = ((WSAuthorEditorPage) editorPage).getAuthorAccess();
            // store current cursor position for later reset
            currentOnset = authorAccess.getEditorAccess().getSelectionStart();
            currentOffset = authorAccess.getEditorAccess().getCaretOffset();
            // change to Text mode
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
        }
        textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
        if (!editorInAuthorMode) {
            currentOnset = textPage.getSelectionStart();
            currentOffset = textPage.getCaretOffset();
        }
        return textPage.getDocument();
    }

    private void resetEditor() {
        if (editorInAuthorMode) {
            editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
            ((WSAuthorEditorPage) editorPage).select(currentOnset, currentOffset);
        } else {
            textPage.select(currentOnset, currentOffset);
        }
    }

    private int[] getXMLHistoryTagPosition() {
        int[] pos = new int[2];
        pos[0] = docBuilder.indexOf("<?argon_history");
        if (pos[0] == -1) {
            // ToDo: identify position for insertion of non-existing processing instruction (after XML definition)
        } else {
            pos[1] = docBuilder.indexOf(">", pos[0]);
            if (pos[1] < pos[0])
                pos[1] = pos[0];
        }
        return pos;
    }

    private void extractVersionAndRevision(int[] position) {
        String[] attributes = {"version=", "revision="};
        for (int i=0; i<2; i++) {
            int ind1 = docBuilder.indexOf(attributes[i], position[0]);
            if ((ind1 > 0) && (ind1 < position[1])) {
                int ind2 = docBuilder.indexOf("\"", ind1);
                ind1 = docBuilder.indexOf("\"", ind2 + 1);
                if ((ind1 > 0) && (ind2 > 0) && (ind1 < position[1]) && (ind2 < position[1])) {
                    try {
                        verRev[i] = Integer.parseInt(docBuilder.substring(ind2 + 1, ind1));
                    } catch (NumberFormatException ex) {/* ignore value of attribute */}
                }
            }
        }
    }

    private int[] getQueryHistoryTagPosition() {
        int[] pos = new int[2];
        pos[0] = docBuilder.indexOf("(: argon_history");
        if (pos[0] == -1) {
            // ToDO
        } else {
            pos[1] = docBuilder.indexOf(":)", pos[0]);
            if (pos[1] < pos[0])
                pos[1] = pos[0];
        }
        return pos;
    }

    private int[] obtainVersionAndRevision() {
        int[] position;
        if (type.equals(XML))
            position = getXMLHistoryTagPosition();
        else
            position = getQueryHistoryTagPosition();
        if (position[0] != position[1])
            extractVersionAndRevision(position);
        return position;
    }

}