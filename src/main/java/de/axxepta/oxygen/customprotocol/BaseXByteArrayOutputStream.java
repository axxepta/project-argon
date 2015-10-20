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


/**
 * @author Daniel Altiparmak
 */
public class BaseXByteArrayOutputStream extends ByteArrayOutputStream {

    private static final Logger logger = LogManager.getLogger(BaseXByteArrayOutputStream.class);

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
        try {
            Connection connection = (new BaseXConnectionWrapper()).getConnection();
            connection.put(this.source,
                    CustomProtocolURLHandlerExtension.pathFromURL(this.url), savedBytes);
            TopicHolder.saveFile.postMessage(this.url.getProtocol() + ":" + this.url.getPath());
            connection.close();
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

}