package de.axxepta.oxygen.actions;

import javax.swing.text.Document;

/**
 * @author Markus on 04.12.2015.
 * Argon type version and revision information can be updated in the passed object and returned as byte streams.
 * If the passed object is a document, as side effect the updating is also inserted into it.
 * For XML type files version/revision information is contained in processing instructions, for Xqueries in comments
 */
public class VersionRevisionUpdater {

    private Document doc;
    private byte[] documentBuffer;
    private String type;

    public VersionRevisionUpdater(Document doc, String type) {
        this.doc = doc;
        this.type = type;
    }

    public VersionRevisionUpdater(byte[] docBuf, String type) {
        this.documentBuffer = docBuf;
        this.type = type;
    }

    public byte[] updateVersion() {

        if (doc != null) {
            // see ReplyAuthorCommentAction for how to change document
        }
        return documentBuffer;
    }

    public byte[] updateRevision() {

        if (doc != null) {

        }
        return documentBuffer;
    }

}