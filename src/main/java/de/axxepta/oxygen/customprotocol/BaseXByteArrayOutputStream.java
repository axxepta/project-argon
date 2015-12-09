/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import de.axxepta.oxygen.actions.VersionRevisionUpdater;
import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.utils.URLUtils;
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
    private static final String backupDB = "~history";

    private final URL url;
    private BaseXSource source;
    private boolean revisionUpdated;

    public BaseXByteArrayOutputStream(BaseXSource source, URL url) {
        super();
        this.url = url;
        this.source = source;
        this.revisionUpdated = false;
    }

    public BaseXByteArrayOutputStream(BaseXSource source, URL url, boolean revisionUpdated) {
        super();
        this.url = url;
        this.source = source;
        this.revisionUpdated = true;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes;
        VersionRevisionUpdater updater;
        if (revisionUpdated || !(URLUtils.isXML(url) || (URLUtils.isQuery(url)))) {
            savedBytes = toByteArray();
        } else {
            if (URLUtils.isXML(url))
                updater = new VersionRevisionUpdater(toByteArray(), "XML");
            else
                updater = new VersionRevisionUpdater(toByteArray(), "Query");
            savedBytes = updater.updateRevision();
        }
        String path = CustomProtocolURLHandlerExtension.pathFromURL(this.url);
        String backupPath = getBackupPath(path);
        try {
            Connection connection = BaseXConnectionWrapper.getConnection();
            connection.put(this.source, path, savedBytes);
            TopicHolder.saveFile.postMessage(this.url.getProtocol() + ":" + this.url.getPath());
            connection.put(BaseXSource.DATABASE, backupPath, savedBytes);
            connection.close();
        } catch (IOException ex) {
            logger.error(ex);
            throw(ex);
        }
        if (!revisionUpdated) {
            // ToDo: update editor window - check TransferHandler
        }
    }

    private String getBackupPath(String path) throws IOException {
        // ToDo: change databsase name
        StringBuilder backupPath = new StringBuilder(backupDB + "/");
        if (source.equals(BaseXSource.REPO))
            backupPath.append("~repo/");
        if (source.equals(BaseXSource.RESTXQ))
            backupPath.append("~restxq/");
        Date date = new Date();
        StringBuilder dateStr = new StringBuilder("_");
        dateStr.append(date.getYear()+1900);     dateStr.append("-");
        if ((date.getMonth()+1)<10)
            dateStr.append("0");
        dateStr.append(date.getMonth()+1);    dateStr.append("-");
        if ((date.getDate())<10)
            dateStr.append("0");
        dateStr.append(date.getDate());     dateStr.append("_");
        if ((date.getHours())<10)
            dateStr.append("0");
        dateStr.append(date.getHours());     dateStr.append("-");
        if ((date.getMinutes())<10)
            dateStr.append("0");
        dateStr.append(date.getMinutes());
        if (path.contains(".")) {
            int fileSepPosition = path.lastIndexOf(".");
            backupPath.append(path.substring(0,fileSepPosition));
            backupPath.append(dateStr);
            backupPath.append(".");
            backupPath.append(path.substring(fileSepPosition+1, path.length()));
        } else {
            backupPath.append(path);
            backupPath.append(dateStr);
        }
        System.out.println(backupPath);
        // ToDo: insert create for non-existing databases in connection.put
        new BaseXRequest("create", BaseXSource.DATABASE, backupDB);
        return backupPath.toString();
    }

}