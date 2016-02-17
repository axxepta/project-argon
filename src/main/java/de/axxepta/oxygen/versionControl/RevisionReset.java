package de.axxepta.oxygen.versioncontrol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.workspace.api.editor.WSEditor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Markus on 17.02.2016.
 */
public class RevisionReset extends VersionRevisionUpdater {

    private static final Logger logger = LogManager.getLogger(RevisionReset.class);

    public RevisionReset(WSEditor editorAccess, String type) {
        super(editorAccess, type);
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
