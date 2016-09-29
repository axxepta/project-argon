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

    private final URL url;
    private BaseXSource source;
    private String encoding = "";
    private boolean useGlobalVersioninng = true;
    private boolean versionUp = false;
    private boolean binary = false;
    private String owner = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_USERNAME, false);

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

    public BaseXByteArrayOutputStream(String owner, boolean binary, URL url) {
        super();
        this.url = url;
        this.binary = binary;
        this.owner = owner;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
    }

    public BaseXByteArrayOutputStream(String owner, URL url, String encoding) {
        super();
        this.url = url;
        this.encoding = encoding;
        this.owner = owner;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
    }

    public BaseXByteArrayOutputStream(URL url, String encoding, boolean versionUp) {
        super();
        this.url = url;
        this.encoding = encoding;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
        this.versionUp = versionUp;
    }

    /**
     * allows to explicitly override the global versioning (swithc off only) for read-only databases
     * @param useGlobalVersioninng set to false if no versioning should be used for the current data transfer
     * @param url resource url to store to
     * @param encoding encoding of the byte array
     */
    public BaseXByteArrayOutputStream(boolean useGlobalVersioninng, URL url, String encoding) {
        super();
        this.url = url;
        this.encoding = encoding;
        this.source = CustomProtocolURLHandlerExtension.sourceFromURL(url);
        this.useGlobalVersioninng = useGlobalVersioninng;
    }

    @Override
    public void close() throws IOException {
        super.close();
        byte[] savedBytes;
        savedBytes = toByteArray();
        // if "Save" or "Save as URL" were called check for encoding
        if (!binary && encoding.equals("")) {
            encoding = ArgonEditorsWatchMap.getInstance().getEncoding(url);
            if (encoding.equals(""))
                XMLUtils.encodingFromBytes(savedBytes);
            if (!encoding.equals("UTF-8") && !encoding.equals(""))
                savedBytes = IOUtils.convertToUTF8(savedBytes, encoding);
        }
        if (encoding.equals("UTF-8") && (savedBytes[0] == (byte)0xEF)) {
            savedBytes = removeBOM(savedBytes, 3);
        }
        if (encoding.startsWith("UTF-16") && ((savedBytes[0] == (byte)0xFE) || (savedBytes[0] == (byte)0xFF))) {
            savedBytes = removeBOM(savedBytes, 3);
        }
        String useVersioning;
        if (useGlobalVersioninng)
            useVersioning = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_VERSIONING, false);
        else
            useVersioning = "false";
        String path = CustomProtocolURLHandlerExtension.pathFromURL(this.url);
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.put(this.source, path, savedBytes, binary, encoding, owner, useVersioning, String.valueOf(versionUp));
            versionUp = false;
            //inform any interested party in save operation
            TopicHolder.saveFile.postMessage(this.url.toString());
        } catch (IOException ex) {
            logger.error(ex);
            throw (ex);
        }
    }

    private static byte[] removeBOM(byte[] savedBytes, int BOMlength) {
        byte[] tempArray = new byte[savedBytes.length - BOMlength];
        System.arraycopy(savedBytes, BOMlength, tempArray, 0, savedBytes.length - BOMlength);
        return tempArray;
    }

}