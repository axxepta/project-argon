/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
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
    private static final String backupDB = "~history/";

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
        int fileSepPosition = Math.max(path.lastIndexOf("/"), path.lastIndexOf("."));
        String backupPath = backupDB + path.substring(0,fileSepPosition) +
                path.substring(fileSepPosition+1, path.length());
        System.out.println(backupPath);
        //Date
        try {
            Connection connection = BaseXConnectionWrapper.getConnection();
            connection.put(this.source, path, savedBytes);
            TopicHolder.saveFile.postMessage(this.url.getProtocol() + ":" + this.url.getPath());
/*            connection.put(BaseXSource.DATABASE, backupPath, savedBytes);*/
            connection.close();
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

}