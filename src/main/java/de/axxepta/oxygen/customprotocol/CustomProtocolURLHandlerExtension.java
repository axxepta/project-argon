package de.axxepta.oxygen.customprotocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension;
//import sun.misc.Lock;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Plugin extension - custom protocol URL handler extension
 */
public class CustomProtocolURLHandlerExtension implements URLStreamHandlerWithLockPluginExtension, URLHandlerReadOnlyCheckerExtension {

    private static final Logger logger = LogManager.getLogger(CustomProtocolURLHandlerExtension.class);
    public static final Cache<URL, Boolean> readOnlyCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    private final LockHandler lockHandler = new LockHandler() {

        @Override
        public void unlock(URL url) throws LockException {
            logger.debug("lock handler: unlock called for " + url.toString());
            readOnlyCache.put(url, Boolean.TRUE);
        }

        @Override
        public void updateLock(URL url, int i) throws LockException {
            logger.debug("lock handler: update lock called");

        }
    };

    /**
     * Gets the handler for the custom protocol
     */
    @Override
    public URLStreamHandler getURLStreamHandler(String protocol) {
        //BaseXConnectionWrapper.refreshDefaults();
        URLStreamHandler handler;
        switch (protocol.toLowerCase()) {
            case ArgonConst.ARGON:
                handler = new ArgonProtocolHandler(BaseXSource.DATABASE);
                return handler;
           case ArgonConst.ARGON_XQ:
               handler = new ArgonProtocolHandler(BaseXSource.RESTXQ);
               return handler;
            case ArgonConst.ARGON_REPO:
                handler = new ArgonProtocolHandler(BaseXSource.REPO);
                return handler;
            default:
                return null;
        }
    }

    @Override
    public LockHandler getLockHandler() {
        return lockHandler;
    }

    @Override
    public boolean isLockingSupported(String protocol) {
        return protocol.toLowerCase().equals(ArgonConst.ARGON);
    }

    @Override
    public boolean canCheckReadOnly(String protocol) {
        return protocol.toLowerCase().equals(ArgonConst.ARGON);
    }

    @Override
    public boolean isReadOnly(URL url) {

        //return ConnectionWrapper.isLockedByUser(CustomProtocolURLUtils.sourceFromURL(url), CustomProtocolURLUtils.pathFromURL(url));

        // FIXME there should be a way to put/remove from cache on checkout/checkin operations
        try {
            return readOnlyCache.get(url, () -> !ConnectionWrapper.isLockedByUser(CustomProtocolURLUtils.sourceFromURL(url), CustomProtocolURLUtils.pathFromURL(url)));
        } catch (ExecutionException e) {
            logger.error("Failed to get read-only status from cache: " + e.getMessage(), e);
            return true;
        }
    }

    public void lock(URL url) {
        readOnlyCache.put(url, Boolean.FALSE);
    }

    public void unlock(URL url) {
        readOnlyCache.put(url, Boolean.TRUE);
    }
}
