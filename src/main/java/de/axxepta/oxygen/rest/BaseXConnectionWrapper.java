package de.axxepta.oxygen.rest;

import de.axxepta.oxygen.api.ClientConnection;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.RestConnection;
import de.axxepta.oxygen.workspace.BaseXOptionPage;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Wrapper class for connection with BaseX server, loading authentication data from Options
 */
public final class BaseXConnectionWrapper {

    Connection connection;

    public BaseXConnectionWrapper(){
        String host = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_HOST);
        String user = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_USERNAME);
        String pass = BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_PASSWORD);
        int port = Integer.parseInt(BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_HTTP_PORT));
        int tcpport = Integer.parseInt(BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_TCP_PORT));
        String ConnType;
        if (BaseXOptionPage.getOption(BaseXOptionPage.KEY_BASEX_CONNECTION).equals("HTTP")) {
            ConnType = "REST";
        } else {
            ConnType = "CLIENT";
        }

        if (ConnType.equals("REST")) {
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

    public Connection getConnection(){
        return this.connection;
    }
}
