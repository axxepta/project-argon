package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
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

    private final BaseXSource source;
    private static final Logger logger = LogManager.getLogger(ArgonProtocolHandler.class);

    public ArgonProtocolHandler(BaseXSource source) {
        this.source = source;
    }

    private static class ArgonConnection extends URLConnection {

        final BaseXSource source;

        ArgonConnection(URL url, BaseXSource source) {
            super(url);
            this.source = source;
            // Allow output
            setDoOutput(true);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            ByteArrayInputStream inputStream;
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                logger.info("Requested new InputStream: " + this.url.toString());
                inputStream = new ByteArrayInputStream(connection.get(source,
                        CustomProtocolURLUtils.pathFromURL(this.url), false));
                // ToDo: try to call OptionPage -> if not accessible, not in editor context (e.g., publishing process), don't add URL to watch map
                ArgonEditorsWatchMap.getInstance().addURL(url);
            } catch (IOException io) {
                logger.debug("Failed to obtain InputStream: ", io.getMessage());
                throw new IOException(io);
            }
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new BaseXByteArrayOutputStream(url);
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
