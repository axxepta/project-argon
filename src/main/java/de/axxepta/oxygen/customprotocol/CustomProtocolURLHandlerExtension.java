package de.axxepta.oxygen.customprotocol;

import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.rest.BaseXRequest;
import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension;

import javax.swing.*;


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

  /**
   * Gets the handler for the custom protocol
   */
    public URLStreamHandler getURLStreamHandler(String protocol) {
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
                try {
                    new BaseXRequest("unlock", BaseXSource.DATABASE, pathFromURL(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to unlock file", "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                }

            }

            @Override
            public void updateLock(URL url, int i) throws LockException {
                try {
                    Connection connection = (new BaseXConnectionWrapper()).getConnection();
                    if (connection != null) {
                        boolean isLocked;
                        try {
                            isLocked = connection.locked(BaseXSource.DATABASE, pathFromURL(url));
                        } catch (Exception er) {
                            isLocked = false;
                        }
                        if (!isLocked) {
                            try {
                                connection.lock(BaseXSource.DATABASE, pathFromURL(url));
                            } catch (IOException er) {
                                er.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Failed to lock file", "BaseX Connection Error", JOptionPane.PLAIN_MESSAGE);
                }
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
        try {
            Connection connection = (new BaseXConnectionWrapper()).getConnection();
            if (connection != null) {
                boolean isLocked = connection.locked(BaseXSource.DATABASE, pathFromURL(url));
                if (isLocked) {
                    return true;
                } else {
                    // just got write access, lock resource for me now
                    if (!connection.lockedByUser(BaseXSource.DATABASE, pathFromURL(url))) {
                        getLockHandler().updateLock(url, 100);
                    }
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception er) {
            return true;
        }
    }

    protected static String pathFromURL(URL url) {
        String urlString = url.toString();
        int ind1 = urlString.indexOf(":");
        //return urlString.substring(ind1 + 2);
        if (urlString.substring(ind1 + 1, ind1 + 2).equals("/"))
            return urlString.substring(ind1 + 2);
        else
            return urlString.substring(ind1 + 1);
    }
}
