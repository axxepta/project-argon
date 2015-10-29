package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Markus on 12.10.2015.
 */
public class ArgonProtocolHandler extends URLStreamHandler {

    BaseXSource source;
    private static final Logger logger = LogManager.getLogger(ArgonProtocolHandler.class);

    public ArgonProtocolHandler(BaseXSource source) {
        this.source = source;
    }

    private static class ArgonConnection extends URLConnection {

        BaseXSource source;

        protected ArgonConnection(URL url, BaseXSource source) {
            super(url);
            this.source = source;
            // Allow output
            setDoOutput(true);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            Connection connection = BaseXConnectionWrapper.getConnection();
            logger.info("Requested input stream: " + url.toString());
            return new ByteArrayInputStream(connection.get(source,
                    CustomProtocolURLHandlerExtension.pathFromURL(this.url)));
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            ArgonEditorsWatchMap.addURL(url);
            return new BaseXByteArrayOutputStream(this.source, url);
        }

        @Override
        public void connect() throws IOException {
            this.connected = true;
        }
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ArgonConnection(url, this.source);
    }

}
