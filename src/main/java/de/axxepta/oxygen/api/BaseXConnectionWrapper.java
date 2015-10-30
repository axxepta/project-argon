package de.axxepta.oxygen.api;

import de.axxepta.oxygen.workspace.BaseXOptionPage;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Wrapper class for connection with BaseX server, loading authentication data from Options
 */
public class BaseXConnectionWrapper {

    static Connection connection;

    public static void refreshFromOptions(boolean defaults){

        String host = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_HOST, defaults);
        String user = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_USERNAME, defaults);
        String pass = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_PASSWORD, defaults);
        int port = Integer.parseInt(BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_HTTP_PORT, defaults));
        int tcpport = Integer.parseInt(BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_TCP_PORT, defaults));

        String connType;
        if (BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_CONNECTION, defaults).equals("HTTP")) {
            connType = "REST";
        } else {
            connType = "CLIENT";
        }

        if (connType.equals("REST")) {
            try {
                connection = new RestConnection(host, port, user, pass);
            } catch (MalformedURLException er) {
                connection = null;
            }
        } else {
            try {
                connection = new ClientConnection(host, tcpport, user, pass);
            } catch (IOException er) {
                connection = null;
            }
        }
    }

    public static void refreshDefaults() {
        try {
            connection = new RestConnection("localhost", 8984, "admin", "admin");
        } catch (MalformedURLException er) {
            connection = null;
        }
    }

    public static Connection getConnection(){
        return connection;
    }
}
