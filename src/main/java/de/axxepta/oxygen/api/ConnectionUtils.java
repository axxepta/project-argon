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
    /** Path variable. */
    static final String PATH = "PATH";
    /** Source variable. */
    static final String SOURCE = "SOURCE";
    /** Resource variable. */
    static final String RESOURCE = "RESOURCE";

    /** Private constructor (prevents instantiation). */
    private ConnectionUtils() { }

    /**
     * Returns the contents of the specified query file as string.
     * @param path path to resource
     * @return string
     * @throws IOException I/O exception
     */
    static String getQuery(final String path) throws IOException {
        final String resource = "/api/" + path + ".xq";
        final InputStream is = ConnectionUtils.class.getResourceAsStream(resource);
        if(is == null) throw new IOException("Resource not found: " + resource);
        return Token.string(new IOStream(is).read());
    }

    /**
     * Prepares a resource for being sent to the server. Encodes non-XML input to Base64.
     * @param resource resource
     * @return resulting array
     */
    static String prepare(final byte[] resource) {
        return Token.string(Token.startsWith(resource, '<') ? resource :
                org.basex.util.Base64.encode(resource));
    }
}