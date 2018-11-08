package de.axxepta.oxygen.api;

import de.axxepta.oxygen.core.ClassFactory;
import de.axxepta.oxygen.utils.ConnectionWrapper;
import de.axxepta.oxygen.workspace.ArgonOptionPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static de.axxepta.oxygen.workspace.ArgonOptionPage.*;

/**
 * Wrapper class for connection with BaseX server, loading authentication data from Options
 */
public class BaseXConnectionWrapper {

    private static final Logger logger = LogManager.getLogger(BaseXConnectionWrapper.class);
    static Connection connection;
    private static String host = null;

    public static void refreshFromOptions(boolean defaults) {
        final String host = ArgonOptionPage.getOption(KEY_BASEX_HOST, defaults);
        BaseXConnectionWrapper.host = host;
        final String user = ArgonOptionPage.getOption(KEY_BASEX_USERNAME, defaults);
        final String pass = ArgonOptionPage.getOption(KEY_BASEX_PASSWORD, defaults);
        final int port = Integer.parseInt(ArgonOptionPage.getOption(KEY_BASEX_HTTP_PORT, defaults));

        logger.info("refreshFromOptions " + host + " " + port + " " +  user + " " +  pass);
        try {
            connection = ClassFactory.getInstance().getRestConnection(host, port, user, pass);
        } catch (MalformedURLException er) {
            connection = null;
        }

        ConnectionWrapper.init();

    }

    public static void refreshDefaults() {
        try {
            connection = ClassFactory.getInstance().getRestConnection("localhost:8984/rest", 8984, "admin", "admin");
        } catch (MalformedURLException er) {
            connection = null;
        }
    }

    public static void refreshDefaults(String host, int port, String user, String password) {
        try {
            connection = ClassFactory.getInstance().getRestConnection(host, port, user, password);
        } catch (MalformedURLException er) {
            connection = null;
        }
    }

    public static Connection getConnection() {
        if (host == null) {
            refreshFromOptions(false);
        }
        return connection;
    }
}
