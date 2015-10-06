package de.axxepta.oxygen.rest;

import de.axxepta.oxygen.api.ClientConnection;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.RestConnection;
import de.axxepta.oxygen.workspace.BaseXOptionPage;

import java.io.IOException;

/**
 * Created by Markus on 05.10.2015.
 */
public final class BaseXConnectionWrapper {
    String user;
    String pass;
    String host;
    int port;
    int tcpport;
    Connection connection;

    public BaseXConnectionWrapper(){
        this.host = BaseXOptionPage.KEY_BASEX_HOST;
        this.user = BaseXOptionPage.KEY_BASEX_USERNAME;
        this.pass = BaseXOptionPage.KEY_BASEX_PASSWORD;
        this.port = Integer.parseInt(BaseXOptionPage.KEY_BASEX_HTTP_PORT);
        this.tcpport = Integer.parseInt(BaseXOptionPage.KEY_BASEX_TCP_PORT);

        String ConnType = "CLIENT";

        if (ConnType.equals("CLIENT")) {
            connection = new RestConnection(host, port, user, pass);
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
