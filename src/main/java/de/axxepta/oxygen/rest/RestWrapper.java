package de.axxepta.oxygen.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import de.axxepta.oxygen.core.ConnectorInterface;

/**
 * Created by daltiparmak on 12.04.15.
 */
public abstract class RestWrapper implements ConnectorInterface {

    public Client client;

    @Override
    public void setRestApiClient() {
        client = Client.create();
    }

    @Override
    public void setRestApiClient(String username, String password) {
        client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(username, password));
    }
}
