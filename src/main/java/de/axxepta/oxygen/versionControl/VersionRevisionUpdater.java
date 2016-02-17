package de.axxepta.oxygen.versioncontrol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorXMLUtilAccess;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
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
    private boolean revisionReset;

    public VersionRevisionUpdater(String type) {
        this.fromDocument = true;
        this.editorAccess = PluginWorkspaceProvider.getPluginWorkspace().
                getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        this.doc = getDocumentFromEditor();
        try {
            this.docBuilder = new StringBuilder(this.doc.getText(0, doc.getLength()));
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
            this.docBuilder = new StringBuilder(this.doc.getText(0, doc.getLength()));
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

    // ToDo: proper exception handling!
    /**
     * Deliver byte array with increased revision (and version), if present changes are also made in doc field
     * @param updateVersion flag whether updating version or only revision
     * @return updated document as byte array
     */
    public byte[] update(boolean updateVersion) {
        if (!updated) {
            final int[] historyTagPosition = obtainVersionAndRevision();
            final int oldTagLength = historyTagPosition[1]-historyTagPosition[0];
            if (updateVersion)
                verRev[0] = verRev[0] + 1;
            verRev[1] = verRev[1] + 1;
            final String tag = getVersionRevisionTag();
            int tagLength = tag.length();
            if (oldTagLength == 0) {
                if (historyTagPosition[0] == 0) {
                    // ToDo: line end for Mac/Linux?
                    docBuilder.insert(historyTagPosition[0], "\n");
                    docBuilder.insert(historyTagPosition[0], tag);
                } else {
                    docBuilder.insert(historyTagPosition[0], tag);
                    docBuilder.insert(historyTagPosition[0], "\n");
                }
            } else {
                docBuilder.replace(historyTagPosition[0], historyTagPosition[1] + 1, tag);
            }
            if (fromDocument) {
                replaceTagInDocument(tagLength, oldTagLength, tag, historyTagPosition[0]);
                resetEditor();
            }
            updated = true;
        }
        return docBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public long[] getVersionAndRevision() {
        return verRev;
    }

    public String getVersionRevisionTag() {
        if (type.equals(XML))
            return String.format("<?argon_history version=\"%s\" revision=\"%s\"?>", verRev[0], verRev[1]);
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


            final AuthorDocumentController adc = authorAccess.getDocumentController();
            //editorAccess.toString()

            //editorAccess.reloadContent();

            final int startOffset = adc.getAuthorDocumentNode().getRootElement().getStartOffset();


            try {

                final AuthorDocumentFragment frag  = adc.createNewDocumentFragmentInContext("<?history version=\"\"?>", startOffset + 1);
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        adc.insertFragment(startOffset, frag);
                    }
                });

            } catch (AuthorOperationException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }


            // change to Text mode
            editorAccess.changePage(EditorPageConstants.PAGE_TEXT);
        }
        textPage = (WSTextEditorPage)editorAccess.getCurrentPage();
        //textPage.getDocument()
        if (!editorInAuthorMode) {
            currentOnset = textPage.getSelectionStart();
            currentOffset = textPage.getCaretOffset();
        }
        return textPage.getDocument();
    }

    private void resetEditor() {
        if (revisionReset) {
            if (editorInAuthorMode) {
                editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
                ((WSAuthorEditorPage) editorPage).select(0, 0);  // old doc could be much smaller
            } else {
                textPage.select(0, 0);
            }
        } else {
            if (editorInAuthorMode) {
                editorAccess.changePage(EditorPageConstants.PAGE_AUTHOR);
                ((WSAuthorEditorPage) editorPage).select(currentOnset, currentOffset);
            } else {
                textPage.select(currentOnset, currentOffset);
            }
            editorAccess.setModified(false);
        }
    }

    private void replaceTagInDocument(int tagLength, final int oldTagLength, final String tag, final int historyTagPosition) {
        if (tagLength != (oldTagLength + 1)){
            currentOnset = currentOnset + tagLength - oldTagLength - 1;
            currentOffset = currentOffset + tagLength - oldTagLength - 1;
        }
        if (oldTagLength != 0) {
            if (SwingUtilities.isEventDispatchThread()) {
                replaceTagInDoc(historyTagPosition, oldTagLength, tag);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            replaceTagInDoc(historyTagPosition, oldTagLength, tag);
                        }
                    });
                } catch (InvocationTargetException ite) {
                    logger.error(ite);
                } catch (InterruptedException ie) {
                    logger.error(ie);
                }
            }
        } else {
            if (SwingUtilities.isEventDispatchThread()) {
                insertTagInDoc(historyTagPosition, tag);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            insertTagInDoc(historyTagPosition, tag);
                        }
                    });
                } catch (InvocationTargetException ite) {
                    logger.error(ite);
                } catch (InterruptedException ie) {
                    logger.error(ie);
                }
            }
        }
    }

    private void replaceTagInDoc(int historyTagPosition0, int oldTagLength, String tag) {
        try {
            doc.remove(historyTagPosition0, oldTagLength + 1);
        } catch (BadLocationException el) {
            logger.error(el);
        }
        try {
            doc.insertString(historyTagPosition0, tag, null);
        } catch (BadLocationException el) {
            logger.error(el);
        }
    }

    private void insertTagInDoc(int historyTagPosition0, String tag) {
        try {
            if (historyTagPosition0 == 0) {
                doc.insertString(historyTagPosition0, "\n", null);
                doc.insertString(historyTagPosition0, tag, null);
            } else {
                doc.insertString(historyTagPosition0, tag, null);
                doc.insertString(historyTagPosition0, "\n", null);
            }
        } catch (BadLocationException el) {
            logger.error(el);
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
            pos[1] = docBuilder.indexOf("?>", pos[0]) + 1;
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
            pos[1] = docBuilder.indexOf(":)", pos[0]) + 1;
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

    // the methods below were added to process the reset to an old file revision

    private void replaceDocument(final String newDoc) {
        if (SwingUtilities.isEventDispatchThread()) {
            replaceWholeDocument(newDoc);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        replaceWholeDocument(newDoc);
                    }
                });
            } catch (InvocationTargetException ite) {
                logger.error(ite);
            } catch (InterruptedException ie) {
                logger.error(ie);
            }
        }
    }

    private void replaceWholeDocument(String newDoc) {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException el) {
            logger.error(el);
        }
        try {
            doc.insertString(0, newDoc, null);
        } catch (BadLocationException el) {
            logger.error(el);
        }
    }

    private void setVerRev(int ver, int rev) {
        verRev[0] = ver;
        verRev[1] = rev;
    }

    public void updateEditorToOldRevision(String newDocumentString, int version, int revision) {
        revisionReset = true;
        replaceDocument(newDocumentString);
        docBuilder = new StringBuilder(newDocumentString);
        final int[] historyTagPosition = obtainVersionAndRevision();
        setVerRev(version, revision);
        final int oldTagLength = historyTagPosition[1] - historyTagPosition[0];
        final String tag = getVersionRevisionTag();
        int tagLength = tag.length();
        replaceTagInDocument(tagLength, oldTagLength, tag, historyTagPosition[0]);
        resetEditor();
    }

}