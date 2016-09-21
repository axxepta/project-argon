package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author Markus on 08.09.2016.
 */
public final class ConnectionWrapper {

    private static final Logger logger = LogManager.getLogger(ConnectionWrapper.class);

    private ConnectionWrapper() {}

    public static void save(URL url, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, "UTF-8")) {
            os.write(bytes);
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(URL url, byte[] bytes, String encoding, boolean versionUp) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, encoding, versionUp)) {
            os.write(bytes);
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(URL url, byte[] bytes, String encoding) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, encoding)) {
            os.write(bytes);
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(boolean binary, URL url, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(binary, url)) {
            os.write(bytes);
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static List<BaseXResource> list(BaseXSource source, String path) throws IOException {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.list(source, path);
        }
    }

    public static List<BaseXResource> listAll(BaseXSource source, String path) throws IOException {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.listall(source, path);
        }
    }

    public static boolean isLocked(BaseXSource source, String path) {
        boolean isLocked = false;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            if (connection.locked(source, path))
                isLocked = true;
        } catch (IOException ie) {
            isLocked = true;
            logger.debug("Querying LOCKED returned: ", ie.getMessage());
        }
        return isLocked;
    }

    public static void lock(BaseXSource source, String path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(source, path);
        } catch (IOException ioe) {
            logger.error("Failed to lock resource " + path + " in " + source.toString() + ": " + ioe.getMessage());
        }
    }

    public static boolean resourceExists(BaseXSource source, String resource) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.exists(source, resource);
        } catch (IOException ioe) {
            logger.error("Failed to ask BaseX for existence of resource " + resource + ": " + ioe.getMessage());
            return false;
        }
    }

}
