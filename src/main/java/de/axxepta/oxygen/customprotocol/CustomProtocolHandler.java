package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.workspace.BaseXOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basex.util.Base64;
import ro.sync.exml.workspace.api.PluginWorkspace;

import java.io.*;
import java.net.*;

//Import log4j classes.

/**
 * Handler for the file2 protocol
 */
public class CustomProtocolHandler extends URLStreamHandler {
	
	 // Define a static logger variable so that it references the
    // Logger instance named "CustomProtocolHandler".
    private static final Logger logger = LogManager.getLogger(CustomProtocolHandler.class);

    /**
     * Connection class for file2
     */
    private static class BaseXConnection extends URLConnection {


        /**
         * Construct the connection
         *
         * @param url
         *            The URL
         */
        protected BaseXConnection(URL url) {

            super(url);
            // Allow output
            setDoOutput(true);
        }

        private PluginWorkspace pluginWorkspace = ro.sync.exml.workspace.api.PluginWorkspaceProvider.getPluginWorkspace();
        

        /**
         * Returns an input stream that reads from this open connection.
         *
         * @return the input stream
         */
        @Override
        public InputStream getInputStream() throws IOException {
            logger.info("-- get Input Stream --: " + url.toString());

            String host = pluginWorkspace.getOptionsStorage().getOption(
                    BaseXOptionPage.KEY_BASEX_HOST,
                    null);
            int httpPort = Integer.parseInt(pluginWorkspace.getOptionsStorage().getOption(
                    BaseXOptionPage.KEY_BASEX_HTTP_PORT,
                    null));
            String username = pluginWorkspace.getOptionsStorage().getOption(
                    BaseXOptionPage.KEY_BASEX_USERNAME,
                    null);
            String password = pluginWorkspace.getOptionsStorage().getOption(
                    BaseXOptionPage.KEY_BASEX_PASSWORD,
                    null);

            // send request, receive response
            String basicAuth = "Basic "
                    + Base64.encode(username + ':' + password);

            String argonUrlString = url.toString();
            String[] parts = argonUrlString.split("argon:");
            String restPath = parts[1];

            URL url = new URL("http://" + host + ':' + httpPort + "/rest"
                    + restPath);

            // will always be HttpURLConnection if URL starts with "http://"
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);

            return conn.getInputStream();
        }

        /**
         * Returns an output stream that writes to this connection.
         *
         * @return the output stream
         */
        @Override
        public OutputStream getOutputStream() throws IOException {

            logger.info("-- get Output Stream --");
            return new BaseXByteArrayOutputStream(url);
        }

        /**
         * Opens a communications link to the resource referenced by this URL,
         * if such a connection has not already been established.
         */
        @Override
        public void connect() throws IOException {
            this.connected = true;
        }

        /**
         * @see java.net.URLConnection#getContentLength()
         */
        @Override
        public int getContentLength() {
        /*
            File file = getCanonicalFileFromFileUrl(url);
            logger.debug(String.format("ContentLength %d", + file.length()));
            return (int) file.length();
        */ return -1;
        }
    }

    /**
     * Creates and opens the connection
     *
     * @param u
     *            The URL
     * @return The connection
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new BaseXConnection(u);
    }

}
