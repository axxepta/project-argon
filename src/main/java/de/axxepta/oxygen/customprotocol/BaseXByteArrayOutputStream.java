/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import de.axxepta.oxygen.actions.VersionRevisionUpdater;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.utils.URLUtils;
import de.axxepta.oxygen.workspace.BaseXOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;


/**
 * @author Daniel Altiparmak
 */
public class BaseXByteArrayOutputStream extends ByteArrayOutputStream {

    private static final Logger logger = LogManager.getLogger(BaseXByteArrayOutputStream.class);
    private static final String backupDB = "~history_";

    private final URL url;
    private BaseXSource source;
    private boolean revisionUpdated;
    private boolean fromTransferHandler;
    private long[] verRev = {1,1};

    public BaseXByteArrayOutputStream(BaseXSource source, URL url) {
        super();
        this.url = url;
        this.source = source;
        this.revisionUpdated = false;
        this.fromTransferHandler = false;
    }

    public BaseXByteArrayOutputStream(BaseXSource source, URL url, boolean revisionUpdated, long[] verRev) {
        super();
        this.url = url;
        this.source = source;
        this.revisionUpdated = revisionUpdated;
        this.verRev[0] = verRev[0];     this.verRev[1] = verRev[1];
        this.fromTransferHandler = false;
    }

    public BaseXByteArrayOutputStream(BaseXSource source, URL url, boolean fromTransferHandler) {
        super();
        this.url = url;
        this.source = source;
        this.revisionUpdated = false;
        this.fromTransferHandler = fromTransferHandler;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes;
        VersionRevisionUpdater updater;
        boolean useVersioning =
                Boolean.parseBoolean(BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_VERSIONING, false));
        if (!useVersioning || revisionUpdated || !(URLUtils.isXML(url) || (URLUtils.isQuery(url)))) {
            savedBytes = toByteArray();
        } else {
            String fileType = URLUtils.isXML(url) ? VersionRevisionUpdater.XML : VersionRevisionUpdater.XQUERY;
            if (fromTransferHandler)
                updater = new VersionRevisionUpdater(toByteArray(), fileType);
            else        // get document from current editor window for direct update (called by SAVE or SAVE TO URL commands)
                updater = new VersionRevisionUpdater(fileType);
            savedBytes = updater.update(false);
            this.verRev = updater.getVersionAndRevision();
        }
        String path = CustomProtocolURLHandlerExtension.pathFromURL(this.url);
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.put(this.source, path, savedBytes);
            TopicHolder.saveFile.postMessage(this.url.getProtocol() + ":" + this.url.getPath());
            if (useVersioning) {
                // ToDo: catch IOException for getBackupPath() independently
                String backupPath = getBackupPath(path);
                connection.put(BaseXSource.DATABASE, backupPath, savedBytes);
            }
        } catch (IOException ex) {
            logger.error(ex);
            throw(ex);
        }
    }

    private String getBackupPath(String path) throws IOException {
        StringBuilder backupPath;
        backupPath = new StringBuilder(backupDB);
        if (source.equals(BaseXSource.REPO))
            backupPath.append("~repo/");
        if (source.equals(BaseXSource.RESTXQ))
            backupPath.append("~restxq/");
        Date date = new Date();
        StringBuilder dateRevisionStr = new StringBuilder("_");
        dateRevisionStr.append(date.getYear()+1900);     dateRevisionStr.append("-");
        if ((date.getMonth()+1)<10)
            dateRevisionStr.append("0");
        dateRevisionStr.append(date.getMonth()+1);    dateRevisionStr.append("-");
        if ((date.getDate())<10)
            dateRevisionStr.append("0");
        dateRevisionStr.append(date.getDate());     dateRevisionStr.append("_");
        if ((date.getHours())<10)
            dateRevisionStr.append("0");
        dateRevisionStr.append(date.getHours());     dateRevisionStr.append("-");
        if ((date.getMinutes())<10)
            dateRevisionStr.append("0");
        dateRevisionStr.append(date.getMinutes());
        // ToDo: version and revision for non-XML/non-Query files??
        dateRevisionStr.append("_v");               dateRevisionStr.append(this.verRev[0]);
        dateRevisionStr.append("r");                dateRevisionStr.append(this.verRev[1]);
        if (path.contains(".")) {
            int fileSepPosition = path.lastIndexOf(".");
            backupPath.append(path.substring(0,fileSepPosition));
            backupPath.append(dateRevisionStr);
            backupPath.append(".");
            backupPath.append(path.substring(fileSepPosition+1, path.length()));
        } else {
            backupPath.append(path);
            backupPath.append(dateRevisionStr);
        }
        System.out.println(backupPath);
        // ToDo: insert create for non-existing databases in connection.put
        new BaseXRequest("create", BaseXSource.DATABASE, backupDB);
        return backupPath.toString();
    }

}