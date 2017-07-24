package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.tree.TreeUtils;
import de.axxepta.oxygen.workspace.ArgonOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus on 08.09.2016.
 */
public final class ConnectionWrapper {

    private static final Logger logger = LogManager.getLogger(ConnectionWrapper.class);

    private ConnectionWrapper() {}

    public static void init() {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.init();
        } catch (IOException | NullPointerException ex) {
            logger.debug("Argon initialization failed!");
        }
    }

    public static void create(String db) throws IOException {
        String chop = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_CHOP, false).toLowerCase();
        String ftindex = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_FTINDEX, false).toLowerCase();
        String textindex = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_TEXTINDEX, false).toLowerCase();
        String attrindex = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_ATTRINDEX, false).toLowerCase();
        String tokenindex = ArgonOptionPage.getOption(ArgonOptionPage.KEY_BASEX_DB_CREATE_TOKENINDEX, false).toLowerCase();
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.create(db, chop, ftindex, textindex, attrindex, tokenindex);
            TopicHolder.newDir.postMessage(ArgonConst.ARGON + ":" + db);
        } catch (NullPointerException ex) {
            String error = ex.getMessage();
            if ((error == null) || error.equals("null"))
                throw new IOException("Database connection could not be established.");
        }
    }

    public static void save(URL url, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, "UTF-8")) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(URL url, byte[] bytes, String encoding, boolean versionUp) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, encoding, versionUp)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(URL url, byte[] bytes, String encoding) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(url, encoding)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(String owner, URL url, byte[] bytes, String encoding) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(owner, url, encoding)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(String owner, boolean binary, URL url, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(owner, binary, url)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(boolean binary, URL url, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(binary, url)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static void save(boolean binary, URL url, byte[] bytes, boolean versionUp) throws IOException {
        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(binary, url, versionUp)) {
            os.write(bytes);
        } catch (NullPointerException npe) {
            logger.info("Error saving to " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException ioe) {
            logger.error("IO error saving to " + url.toString() + ": ", ioe.getMessage());
            throw new IOException(ioe);
        }
    }

    public static InputStream getInputStream(URL url) throws IOException {
        ByteArrayInputStream inputStream;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            logger.info("Requested new InputStream: " + url.toString());
            inputStream = new ByteArrayInputStream(connection.get(CustomProtocolURLHandlerExtension.sourceFromURL(url),
                    CustomProtocolURLHandlerExtension.pathFromURL(url), false));
        } catch (NullPointerException npe) {
            logger.info("Error obtaining input stream from " + url.toString() + ": no database connection");
            throw new IOException("No database connection");
        } catch (IOException io) {
            logger.debug("Failed to obtain InputStream: ", io.getMessage());
            throw new IOException(io);
        }
        return inputStream;
    }

    public static List<BaseXResource> list(BaseXSource source, String path) throws IOException {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.list(source, path);
        } catch (NullPointerException npe) {
            logger.info("Error listing path " + path + ": no database connection");
            throw new IOException("No database connection");
        }
    }

    /**
     * lists all resources in the path, including directories
     * @param source source in which path resides
     * @param path path to list
     * @return list of all resources in path, entries contain full path as name, for databases without the database name
     * @throws IOException throws exception if connection returns an exception/error code
     */
    public static List<BaseXResource> listAll(BaseXSource source, String path) throws IOException {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.listAll(source, path);
        } catch (NullPointerException npe) {
            logger.info("Error listing path " + path + ": no database connection");
            throw new IOException("No database connection");
        }
    }

    public static boolean directoryExists(BaseXSource source, String path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            List<BaseXResource> resourceList = connection.list(source, path);
            return (resourceList.size() != 0);
        } catch (NullPointerException npe) {
            logger.info("Error checking for directory " + path + ": no database connection");
            return true;
        } catch (IOException ioe) {
            return false;
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

    public static void unlock(BaseXSource source, String path) {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.unlock(source, path);
        } catch (Throwable ioe) {
            logger.error("Failed to unlock resource " + path + " in " + source.toString() + ": " + ioe.getMessage());
        }
    }

    public static List<String> findFiles(BaseXSource source, String path, String filter, boolean caseSensitive) throws IOException {
        List<String> result;
        StringBuilder regEx = new StringBuilder(caseSensitive ? "" : "(?i)");
        for (int i = 0; i < filter.length(); i++) {
            char c = filter.charAt(i);
            switch (c) {
                case '*':
                    regEx.append(".*");
                    break;
                case '?':
                    regEx.append('.');
                    break;
                case '.':
                    regEx.append("\\.");
                    break;
                default:
                    regEx.append(c);
            }
        }
        String regExString = regEx.toString();
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            result = connection.search(source, path, regExString);
            for (int i = result.size() - 1; i > -1; i--) {
                String foundPath = result.get(i);
                String foundFile = TreeUtils.fileStringFromPathString(foundPath);
                Matcher matcher = Pattern.compile(regExString).matcher(foundFile);
                if (!matcher.find())
                    result.remove(foundPath);
            }
        } catch (NullPointerException npe) {
            logger.error("Error searching for files: no database connection");
            throw new IOException("No database connection");
        }
        return result;
    }

    public static List<String> parse(String path) throws IOException {
        List<String> result = new ArrayList<>();
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            connection.parse(path);
        } catch (BaseXQueryException ex) {
            result.add(Integer.toString(ex.getLine()));
            result.add(Integer.toString(ex.getColumn()));
            result.add(ex.getInfo());
        }
        return result;
    }

    public static String query(String query, String[] parameter) throws IOException {
        return "<response>\n" + sendQuery(query, parameter) + "\n</response>";
    }

    private static String sendQuery(String query, String[] parameter) throws IOException {
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            return connection.xquery(query, parameter);
        } catch (NullPointerException npe) {
            logger.info("Error sending query: no database connection");
            throw new IOException("No database connection");
        }
    }

    private static List<String> searchInFiles(String query, String path, String filter, boolean wholeMatch, boolean exactCase)
            throws IOException {
        String[] parameter = {"PATH", path, "FILTER", filter, "WHOLE", Boolean.toString(wholeMatch),
                "EXACTCASE", Boolean.toString(exactCase)};
        String result = sendQuery(query, parameter);
        String db_name = (path.split("/"))[0];
        final ArrayList<String> list = new ArrayList<>();
        if(!result.isEmpty()) {
            final String[] results = result.split("\r?\n");
            for(String res : results) {
                list.add("argon:" + db_name + "/" + res);
            }
        }
        return list;
    }

    public static List<String> searchAttributes(String path, String filter, boolean wholeMatch, boolean exactCase)
            throws IOException {
        return searchInFiles(ConnectionUtils.getQuery("search-attributes"), path, filter, wholeMatch, exactCase);
    }

    public static List<String> searchAttributeValues(String path, String filter, boolean wholeMatch, boolean exactCase)
            throws IOException {
        return searchInFiles(ConnectionUtils.getQuery("search-attrvalues"), path, filter, wholeMatch, exactCase);
    }

    public static List<String> searchElements(String path, String filter, boolean wholeMatch, boolean exactCase)
            throws IOException {
        return searchInFiles(ConnectionUtils.getQuery("search-elements"), path, filter, wholeMatch, exactCase);
    }

    public static List<String> searchText(String path, String filter, boolean wholeMatch, boolean exactCase)
            throws IOException {
        return searchInFiles(ConnectionUtils.getQuery("search-text"), path, filter, wholeMatch, exactCase);
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
            lockFile = connection.get(BaseXSource.DATABASE, ArgonConst.ARGON_DB + "/" + ArgonConst.LOCK_FILE, false);
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
        } catch (Throwable t) {
            return true;
        }
        return false;
    }

    /**
     * Adds new directory. Side effect: for databases an empty file .empty.xml will be added in the new directory to make
     * the new directory persistent in the database.
     * @param source BaseXSource in which new directory shall be added
     * @param path path of new directory
     */
    public static void newDir(BaseXSource source, String path) {
        if (source.equals(BaseXSource.DATABASE)) {
            String resource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<empty/>";
            String urlString = CustomProtocolURLHandlerExtension.protocolFromSource(source) + ":" +
                    path + "/" + ArgonConst.EMPTY_FILE;
            try {
                URL url = new URL(urlString);
                ConnectionWrapper.save(url, resource.getBytes(), "UTF-8");
            } catch (IOException e1) {
                logger.error(e1);
            }
        } else {
            try (Connection connection = BaseXConnectionWrapper.getConnection()) {
                connection.newDir(source, path);
            } catch (Throwable io) {
                logger.error("Failed to create new directory: " + io.getMessage());
            }
        }
    }

}
