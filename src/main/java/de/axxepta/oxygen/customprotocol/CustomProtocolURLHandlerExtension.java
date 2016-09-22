package de.axxepta.oxygen.customprotocol;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLStreamHandler;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension;


/**
 * Plugin extension - custom protocol URL handler extension
 */
public class CustomProtocolURLHandlerExtension implements URLStreamHandlerWithLockPluginExtension, URLHandlerReadOnlyCheckerExtension {

  /**
   * The custom protocol name.
   */
    public static final String ARGON = "argon";
    public static final String ARGON_XQ = "argonquery";
    public static final String ARGON_REPO = "argonrepo";

    private static final Logger logger = LogManager.getLogger(CustomProtocolURLHandlerExtension.class);
  /**
   * Gets the handler for the custom protocol
   */
    public URLStreamHandler getURLStreamHandler(String protocol) {
        //BaseXConnectionWrapper.refreshDefaults();
        URLStreamHandler handler;
        switch (protocol.toLowerCase()) {
            case ARGON: handler = new ArgonProtocolHandler(BaseXSource.DATABASE);
                return handler;
            case ARGON_XQ: handler = new ArgonProtocolHandler(BaseXSource.RESTXQ);
                return handler;
            case ARGON_REPO: handler = new ArgonProtocolHandler(BaseXSource.REPO);
                return handler;
            default: return null;
        }
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#getLockHandler()
   */
    public LockHandler getLockHandler() {

        return (new LockHandler() {

            @Override
             public void unlock(URL url) throws LockException {
            }

            @Override
            public void updateLock(URL url, int i) throws LockException {
            }

        });
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#isLockingSupported(java.lang.String)
   */
    public boolean isLockingSupported(String protocol) {
        //return false;
        return (protocol.toLowerCase().equals(ARGON) ||
                protocol.toLowerCase().equals(ARGON_XQ) ||
                protocol.toLowerCase().equals(ARGON_REPO));
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension#canCheckReadOnly(java.lang.String)
   */
    public boolean canCheckReadOnly(String protocol) {
        //return false;
        return (protocol.toLowerCase().equals(ARGON) ||
                protocol.toLowerCase().equals(ARGON_XQ) ||
                protocol.toLowerCase().equals(ARGON_REPO));
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension#isReadOnly(java.net.URL)
   */
    public boolean isReadOnly(URL url) {
        //return false;
        return isInHiddenDB(url) || !ConnectionWrapper.isLockedByUser(BaseXSource.DATABASE, pathFromURL(url));
    }

    private static boolean isInHiddenDB(URL url) {
        String path = pathFromURL(url);
        int firstCharOfPath = (path.charAt(0) == '/') ? 1 : 0;
        return (path.charAt(firstCharOfPath) == '~');
    }

    public static String pathFromURL(URL url) {
        String urlString = "";
        try {
            urlString = java.net.URLDecoder.decode(url.toString(), "UTF-8");
        } catch(UnsupportedEncodingException| IllegalArgumentException ex) {
            logger.error("URLDecoder error decoding " + url.toString(), ex.getMessage());
        }
        return pathFromURLString(urlString);
    }

    public static String pathFromURLString(String urlString) {
        String[] urlComponents = urlString.split(":/*");
        if (urlComponents.length < 2)
            return "";
        // ToDo: exception handling
        else
            return urlComponents[1];
    }

    public static String protocolFromSource(BaseXSource source) {
        switch (source) {
            case RESTXQ: return ARGON_XQ;
            case REPO: return ARGON_REPO;
            default: return ARGON;
        }
    }

    public static String protocolFromURL(URL url) {
        String urlString = url.toString().toLowerCase();
        if (urlString.startsWith(ARGON_XQ))
            return ARGON_XQ;
        if (urlString.startsWith(ARGON_REPO))
            return ARGON_REPO;
        return ARGON;
    }

    public static BaseXSource sourceFromURL(URL url) {
        return sourceFromURLString(url.toString());

    }

    public static BaseXSource sourceFromURLString(String urlString) {
        String protocol;
        int ind1 = urlString.indexOf(":");
        if (ind1 == -1)     // no proper URL string, but used someplace
            protocol = urlString;
        else
            protocol = urlString.substring(0, ind1);
        switch (protocol) {
            case ARGON_XQ: return BaseXSource.RESTXQ;
            case ARGON_REPO: return BaseXSource.REPO;
            case ARGON: return BaseXSource.DATABASE;
            default: return null;
        }
    }

}
