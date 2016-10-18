package de.axxepta.oxygen.api;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Utility methods.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public final class ConnectionUtils {
    /** Database variable. */
    public static final String DATABASE = "DB";
    /** Path variable. */
    public static final String PATH = "PATH";
    /** Path variable for moved/renamed resources. */
    public static final String NEWPATH = "NEWPATH";
    /** Source variable. */
    public static final String SOURCE = "SOURCE";
    /** Resource variable. */
    public static final String RESOURCE = "RESOURCE";
    /** Query string variable */
    public static final String XQUERY = "XQUERY";
    /** Search filter variable */
    public static final String FILTER = "FILTER";
    /** Create database chop option */
    public static final String CHOP = "CHOP";
    /** Create database ftindex option */
    public static final String FTINDEX = "FTINDEX";
    /** Use version control option */
    public static final String VERSIONIZE = "VERSIONIZE";
    /** Increase file version option */
    public static final String VERSION_UP = "VERSION-UP";
    /** Encoding variable */
    public static final String ENCODING = "ENCODING";
    /** Binary option */
    public static final String BINARY = "BINARY";
    /** File owner variable */
    public static final String OWNER = "OWNER";

    /** Private constructor (prevents instantiation). */
    private ConnectionUtils() { }

    /**
     * Returns the contents of the specified query file as string.
     * @param path path to resource
     * @return string
     * @throws IOException I/O exception
     */
    public static String getQuery(final String path) throws IOException {
        final String resource = path + ".xq";
        return(getAPIResource(resource));
    }

    public static String getAPIResource(final String path) throws IOException {
        final String resource = "/api/" + path;
        final InputStream is = ConnectionUtils.class.getResourceAsStream(resource);
        if(is == null) throw new IOException("Resource not found: " + resource);
        return Token.string(new IOStream(is).read());
    }

    /**
     * Prepares a resource for being sent to the server. Encodes non-XML input to Base64.
     * @param resource resource
     * @return resulting array
     */
    public static String prepare(final byte[] resource, boolean binary) {
        return Token.string(binary ? org.basex.util.Base64.encode(resource) : resource);
    }
}