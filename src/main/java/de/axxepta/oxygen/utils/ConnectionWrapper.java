package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
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
            return connection.listAll(source, path);
        }
    }

    public static boolean isLocked(BaseXSource source, String path) {
        boolean isLocked = false;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            if (connection.locked(source, path))
                isLocked = true;
        } catch (Throwable ie) {
            isLocked = true;
            logger.debug("Querying LOCKED returned: ", ie.getMessage());
        }
        return isLocked;
    }

    public static boolean isLockedByUser(BaseXSource source, String path) {
        boolean isLockedByUser = false;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            isLockedByUser = connection.lockedByUser(source, path);
        } catch (Throwable ioe) {
            logger.debug(ioe);
        }
        return isLockedByUser;
    }

    public static void lock(BaseXSource source, String path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.lock(source, path);
        } catch (Throwable ioe) {
            logger.error("Failed to lock resource " + path + " in " + source.toString() + ": " + ioe.getMessage());
        }
    }

    public static boolean resourceExists(BaseXSource source, String resource) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.exists(source, resource);
        } catch (Throwable ioe) {
            logger.error("Failed to ask BaseX for existence of resource " + resource + ": " + ioe.getMessage());
            return false;
        }
    }

    public static boolean pathContainsLockedResource(BaseXSource source, String path) {
        byte[] lockFile;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            lockFile = connection.get(BaseXSource.DATABASE, ArgonConst.ARGON_DB + "/" + ArgonConst.LOCK_FILE);
            Document dom = XMLUtils.docFromByteArray(lockFile);
            XPathExpression expression = XMLUtils.getXPathExpression("*//" + source.toString());
            NodeList lockedResources = (NodeList) expression.evaluate(dom, XPathConstants.NODESET);
            String[] pathComponents = path.split("/");
            for (int i = 0; i < lockedResources.getLength(); i++) {
                String[] resourceComponents = lockedResources.item(i).getTextContent().split("/");
                if (resourceComponents.length >= pathComponents.length) {
                    boolean isEqual = true;
                    for (int k = 0; k < pathComponents.length; k++) {
                        if (!pathComponents[k].equals(resourceComponents[k]))
                            isEqual = false;
                    }
                    if (isEqual)
                        return true;
                }
            }
        } catch (IOException ioe) {
            logger.error("Failed to obtain lock list: " + ioe.getMessage());
            return true;
        } catch (ParserConfigurationException | SAXException | XPathExpressionException xe) {
            logger.error("Failed to parse lock file XML: " + xe.getMessage());
            return true;
        }
        return false;
    }

}
