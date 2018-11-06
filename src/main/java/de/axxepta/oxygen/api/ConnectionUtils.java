package de.axxepta.oxygen.api;

import org.basex.io.IOStream;
import org.basex.util.Token;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility methods.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public final class ConnectionUtils {
    /**
     * Database variable.
     */
    public static final String DATABASE = "DB";
    /**
     * Path variable.
     */
    public static final String PATH = "PATH";
    /**
     * Path variable for moved/renamed resources.
     */
    public static final String NEWPATH = "NEWPATH";
    /**
     * Source variable.
     */
    public static final String SOURCE = "SOURCE";
    /**
     * Resource variable.
     */
    public static final String RESOURCE = "RESOURCE";
    /**
     * Query string variable
     */
    public static final String XQUERY = "XQUERY";
    /**
     * Search filter variable
     */
    public static final String FILTER = "FILTER";
    /**
     * Create database chop option
     */
    public static final String CHOP = "OPT_CHOP";
    /**
     * Create database ftindex option
     */
    public static final String FTINDEX = "OPT_FTINDEX";
    /**
     * Create database ftindex option
     */
    public static final String TEXTINDEX = "OPT_TEXTINDEX";
    /**
     * Create database ftindex option
     */
    public static final String ATTRINDEX = "OPT_ATTRINDEX";
    /**
     * Create database ftindex option
     */
    public static final String TOKENINDEX = "OPT_TOKENINDEX";
    /**
     * Use version control option
     */
    public static final String VERSIONIZE = "VERSIONIZE";
    /**
     * Increase file version option
     */
    public static final String VERSION_UP = "VERSION-UP";
    /**
     * Encoding variable
     */
    public static final String ENCODING = "ENCODING";
    /**
     * Binary option
     */
    public static final String BINARY = "BINARY";
    /**
     * File owner variable
     */
    public static final String OWNER = "OWNER";

    /**
     * Private constructor (prevents instantiation).
     */
    private ConnectionUtils() {
    }

    /**
     * Returns the contents of the specified query file as string.
     *
     * @param path path to resource
     * @return string
     * @throws IOException I/O exception
     */
    public static String getQuery(final String path) throws IOException {
        final String resource = "argon/" + path + ".xq";
        //only return the name of the query
        return resource;
        //return (getAPIResource(resource));
    }

    /*
    public static String getAPIResource(final String path) throws IOException {
        final String resource = "/api/" + path;
        final InputStream is = ConnectionUtils.class.getResourceAsStream(resource);
        if (is == null) throw new IOException("Resource not found: " + resource);
        return Token.string(new IOStream(is).read());
    }
    */

    /**
     * Prepares a resource for being sent to the server. Encodes non-XML input to Base64.
     *
     * @param resource resource
     * @return resulting array
     */
    public static String prepare(final byte[] resource, boolean binary) {
        return Token.string(binary ? org.basex.util.Base64.encode(resource) : resource);
    }

    public static HttpURLConnection getConnection(URL url) throws IOException {
        if ("http".equals(url.getProtocol())
                || "https".equals(url.getProtocol())) {
            try {
                Constructor constructor = Class.forName("sun.net.www.protocol.http.HttpURLConnection").
                        getConstructor(URL.class, Proxy.class);
                return (HttpURLConnection) constructor.newInstance(new Object[]{url, null});
            } catch (InvocationTargetException ex) {
                //Constructor threw an IO Exception
                throw (IOException) ex.getTargetException();
            } catch (Throwable th) {
                //Probably SUN class disappeared, use connection from URL.
            }
        }
        //Probably SUN class disappeared, use connection from URL.
        return (HttpURLConnection) url.openConnection();
    }
}