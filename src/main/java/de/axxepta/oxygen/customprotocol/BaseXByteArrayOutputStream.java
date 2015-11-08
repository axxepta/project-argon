/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.rest.BaseXRequest;
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


    public BaseXByteArrayOutputStream(BaseXSource source, URL url) {
        super();
        this.url = url;
        this.source = source;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes = toByteArray();
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
        }
    }

    private String getBackupPath(String path) throws IOException {
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