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
import java.nio.charset.StandardCharsets;

/**
 * @author Markus on 04.12.2015.
 * Argon type version and revision information can be increased in the passed object and returned as byte streams.
 * If the passed object is a WSEditor (when update is called from context menu, but URL is opened in editor),
 * as SIDE EFFECT the updating is also inserted into the document of the editor.
 * The same is true, if no object is passed to the constructor. The document is then obtained from the current editor
 *  window (to be used when Save or Save As are called).
 * For XML type files version/revision information is contained in processing instructions, for Xqueries in comments,
 * in the format <?argon_history version="x" revision="y"/> or (:  argon_history version="x" revision="y" :)
 */
public class VersionRevisionUpdater {

    private static final Logger logger = LogManager.getLogger(VersionRevisionUpdater.class);

    public static final String XML = "XML";
    public static final String XQUERY = "XQUERY";

    private Document doc = null;
    private StringBuilder docBuilder;
    private String type;
    private long[] verRev = {1,0};
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
        try {
            this.docBuilder = new StringBuilder(new String(documentBuffer, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            this.docBuilder = new StringBuilder("");
            logger.error(ex);
        }
        this.type = type;
    }

    /**
     * Deliver byte array with increased revision (and version), if present changes are also made in doc field
     * @param updateVersion flag whether updating version or only revision
     * @return updated document as byte array
     */
    public byte[] update(boolean updateVersion) {
        if (!updated) {
            int[] historyTagPosition = obtainVersionAndRevision();
            int oldTagLength = historyTagPosition[1]-historyTagPosition[0];
            if (updateVersion)
                verRev[0] = verRev[0] + 1;
            verRev[1] = verRev[1] + 1;
            String tag = getVersionRevisionTag();
            int tagLength = tag.length();
            if (oldTagLength == 0) {
                if (historyTagPosition[0] == 0) {
                    docBuilder.insert(historyTagPosition[0], tag);
                    docBuilder.insert(historyTagPosition[0], "\n");
                } else {
                    docBuilder.insert(historyTagPosition[0], "\n");
                    docBuilder.insert(historyTagPosition[0], tag);
                }
            } else {
                docBuilder.replace(historyTagPosition[0], historyTagPosition[1], tag);
            }
            if (fromDocument) {
                // ToDo: check whether numbers of characters correspond in Document and StringBuilder
                if (tagLength != oldTagLength){
                    currentOnset = currentOnset + tagLength - oldTagLength;
                    currentOffset = currentOffset + tagLength - oldTagLength;
                }
                if (oldTagLength != 0) {
                    try {
                        doc.remove(historyTagPosition[0], oldTagLength);
                    } catch (BadLocationException el) {
                        logger.error(el);
                    }
                }
                try {
                    doc.insertString(historyTagPosition[0], tag, null);
                } catch (BadLocationException el) {
                    logger.error(el);
                }
                resetEditor();
            }
            updated = true;
        }
        return docBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public long[] getVersionAndRevision() {
        return verRev;
    }

    private String getVersionRevisionTag() {
        if (type.equals(XML))
            return String.format("<?argon_history version=\"%s\" revision=\"%s\" ?>", verRev[0], verRev[1]);
        else
            return String.format("(: argon_history version=\"%s\" revision=\"%s\" :)", verRev[0], verRev[1]);
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

    /**
     * Position of the Argon version/revision processing instruction is obtained from the docBuilder field
     * @return start and end position, {0,0} if no valid tag and no valid XML declaration are found, position
     *      after XML declaration if present (but no Argon tag present)
     */
    private int[] getXMLHistoryTagPosition() {
        int[] pos = {0, 0};
        int ind1 = docBuilder.indexOf("<?argon_history");
        if (ind1 == -1) {     // after XML declaration or (if not present) in first line
            ind1 = docBuilder.indexOf("<?xml");
            if (ind1 != -1) {
                ind1 = docBuilder.indexOf("?>", ind1 + 2);
                if (ind1 != -1) {
                    pos[0] = ind1 + 2;
                    pos[1] = pos[0];
                }
            }
        } else {
            pos[0] = ind1;
            pos[1] = docBuilder.indexOf("?>", pos[0]);
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
        if (pos[0] == -1) {     // first line in file
            pos[0] = 0; pos[1] = 0;
        } else {
            pos[1] = docBuilder.indexOf(":)", pos[0]);
            if (pos[1] < pos[0])
                pos[1] = pos[0];
        }
        return pos;
    }

    /**
     * Identifies position of the version/revision processing instruction tag (XML) or comment (XQuery) in the field
     * docBuilder, extracts version and revision (if present) and stores them into the field verRev.
     * @return diverging from what the name might let expect you the start and end position of the version/revision tag
     */
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