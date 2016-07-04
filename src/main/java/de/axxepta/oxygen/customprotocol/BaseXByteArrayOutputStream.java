/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.workspace.ArgonOptionPage;
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
    public static final String backupDBBase = "~history_";
    public static final String backupRESTXYBase = "~history_~restxq/";
    public static final String backupRepoBase = "~history_~repo/";

    private final URL url;
    private BaseXSource source;
    private boolean versionUp = false;

    public BaseXByteArrayOutputStream(BaseXSource source, URL url) {
        super();
        this.url = url;
        this.source = source;
    }

    public BaseXByteArrayOutputStream(BaseXSource source, URL url, boolean versionUp) {
        super();
        this.url = url;
        this.source = source;
        this.versionUp = versionUp;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes;
        savedBytes = toByteArray();
        String useVersioning = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_VERSIONING, false);
        String path = CustomProtocolURLHandlerExtension.pathFromURL(this.url);
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.put(this.source, path, savedBytes, useVersioning, String.valueOf(versionUp));
            versionUp = false;
            //inform any interested party in save operation
            TopicHolder.saveFile.postMessage(this.url.toString());
        } catch (IOException ex) {
            logger.error(ex);
            throw (ex);
        }
    }

}