package de.axxepta.oxygen.core;

import java.util.ArrayList;

/**
 * Created by daltiparmak on 12.04.15.
 */
public interface ConnectorInterface {

    void setRestApiClient();

    void setRestApiClient(String username, String password);

    ArrayList<String> getResources();

}
