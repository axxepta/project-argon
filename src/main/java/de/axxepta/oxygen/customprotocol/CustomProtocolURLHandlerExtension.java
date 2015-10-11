package de.axxepta.oxygen.customprotocol;

import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.rest.BaseXConnectionWrapper;
import de.axxepta.oxygen.rest.BaseXRequest;
import de.axxepta.oxygen.rest.ListDBEntries;
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
    private static final String ARGON = "argon";

  /**
   * Gets the handler for the custom protocol
   */
    public URLStreamHandler getURLStreamHandler(String protocol) {
    // If the protocol is argon return its handler
        if (protocol.equals(ARGON)) {
            URLStreamHandler handler = new CustomProtocolHandler();
            return handler;
        }
        return null;
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#getLockHandler()
   */
    public LockHandler getLockHandler() {
        //return null;

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
                Connection connection = (new BaseXConnectionWrapper()).getConnection();
                if (connection != null) {
                    try {
                        connection.lock(BaseXSource.DATABASE, pathFromURL(url));
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
            }

/*            @Override
            public void unlock(URL url) throws LockException {
                ArrayList<String> dbPath = tempFile(url);
                try {
                    ListDBEntries fileDummy = new ListDBEntries("delete", dbPath.get(0), dbPath.get(1));
                } catch (Exception er) {
                    er.printStackTrace();
                }
            }

            @Override
            public void updateLock(URL url, int i) throws LockException {
                // add url to locked file list
                ArrayList<String> dbPath = tempFile(url);
                try {
                    ListDBEntries fileDummy = new ListDBEntries("lock", dbPath.get(0), dbPath.get(1));
                } catch (Exception er) {
                    er.printStackTrace();
                }
            }*/

        });
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#isLockingSupported(java.lang.String)
   */
    public boolean isLockingSupported(String protocol) {
        //return false;
        return protocol.equals(ARGON);
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension#canCheckReadOnly(java.lang.String)
   */
    public boolean canCheckReadOnly(String protocol) {
        //return false;
        return protocol.equals(ARGON);
    }

  /**
   * @see ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension#isReadOnly(java.net.URL)
   */
    public boolean isReadOnly(URL url) {
        try {
            Connection connection = (new BaseXConnectionWrapper()).getConnection();
            if (connection != null) {
                return connection.locked(BaseXSource.DATABASE, pathFromURL(url));
            } else {
                return false;
            }
        } catch (Exception er) {
            return false;
        }

/*        ArrayList<String> dbPath = tempFile(url);
        try {
            ListDBEntries isOpened = new ListDBEntries("locked", dbPath.get(0), dbPath.get(1));
            return (isOpened.getAnswer().equals("true"));
        } catch (Exception er) {
            return false;
        }*/

        //return false;
    }

    /**
     * returns ArrayList containing db name and path of file (with tilde added at file name beginning) in db
     */
    private ArrayList<String> tempFile(URL url) {
        ArrayList<String> dbFile = new ArrayList<>();
        String urlString = url.toString();
        int ind1 = urlString.indexOf(":");
        int ind2 = urlString.indexOf("/", ind1 + 2);
        dbFile.add(urlString.substring(ind1 + 2, ind2));
        ind1 = urlString.lastIndexOf("/");
        dbFile.add(urlString.substring(ind2+1,ind1+1) + "~" + urlString.substring(ind1+1));
        return dbFile;
    }

    private String pathFromURL(URL url) {
        String urlString = url.toString();
        int ind1 = urlString.indexOf(":");
        return urlString.substring(ind1 + 2);
    }
}
