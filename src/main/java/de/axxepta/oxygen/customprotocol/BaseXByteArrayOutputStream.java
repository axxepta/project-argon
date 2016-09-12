package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.utils.IOUtils;
import de.axxepta.oxygen.utils.XMLUtils;
import de.axxepta.oxygen.workspace.ArgonOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;


public class BaseXByteArrayOutputStream extends ByteArrayOutputStream {

    private static final Logger logger = LogManager.getLogger(BaseXByteArrayOutputStream.class);
    public static final String BACKUP_DB_BASE = "~history_";

    public static final String BACKUP_RESTXQ_BASE = "~history_~restxq/";
    public static final String BACKUP_REPO_BASE = "~history_~repo/";

    public static final String META_DB_BASE = "~meta_";
    public static final String META_RESTXQ_BASE = "~meta_~restxq/";
    public static final String META_REPO_BASE = "~meta_~repo/";

    private final URL url;
    private BaseXSource source;
    private String encoding = "";
    private boolean versionUp = false;
    private boolean binary = false;

    BaseXByteArrayOutputStream(URL url) {
        super();
        this.url = url;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
    }

    public BaseXByteArrayOutputStream(URL url, String encoding) {
        super();
        this.url = url;
        this.encoding = encoding;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
    }

    public BaseXByteArrayOutputStream(boolean binary, URL url) {
        super();
        this.url = url;
        this.binary = binary;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
    }

    public BaseXByteArrayOutputStream(URL url, String encoding, boolean versionUp) {
        super();
        this.url = url;
        this.encoding = encoding;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
        this.versionUp = versionUp;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes;
        savedBytes = toByteArray();
        // if "Save" or "Save as URL" were called check for encoding
        if (!binary && !encoding.equals("")) {
            encoding = ArgonEditorsWatchMap.getEncoding(url);
            if (encoding.equals(""))
                XMLUtils.encodingFromBytes(savedBytes);
            if (!encoding.equals("UTF-8") && !encoding.equals(""))
                savedBytes = IOUtils.convertToUTF8(savedBytes, encoding);
        }
        String useVersioning = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_VERSIONING, false);
        String path = CustomProtocolURLHandlerExtension.pathFromURL(this.url);
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.put(this.source, path, savedBytes, binary, encoding, useVersioning, String.valueOf(versionUp));
            versionUp = false;
            //inform any interested party in save operation
            TopicHolder.saveFile.postMessage(this.url.toString());
        } catch (IOException ex) {
            logger.error(ex);
            throw (ex);
        }
    }

}