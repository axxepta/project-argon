package de.axxepta.oxygen.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class BasexWrapper {

    public static void main(String[] args){


        try {
            Client client = Client.create();
            /*
            WebResource webResource = client.resource("http://127.0.0.1:8984/rest");

            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            String output = response.getEntity(String.class);

            System.out.println("Server response : \n");
            System.out.println(output);
            */
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
}
