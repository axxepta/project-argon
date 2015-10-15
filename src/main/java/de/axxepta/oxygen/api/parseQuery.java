package de.axxepta.oxygen.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Markus on 15.10.2015.
 */
public class parseQuery {

    public static void main(String... args) throws IOException {
        String user = "admin";
        String pass = "admin";
        String host = "localhost";
        int port = 8984;
        Connection connection = new RestConnection(host, port, user, pass);;

        System.out.print("Query text: ");
        String query = (new BufferedReader(new InputStreamReader(System.in))).readLine();

        ArrayList<String> result = new ArrayList<>();
        try {
            connection.parse(query);
        } catch(BaseXQueryException ex) {
            result.add(Integer.toString(ex.getLine()));
            result.add(Integer.toString(ex.getColumn()));
            result.add(ex.getInfo());
        }

        for (int i=0; i<result.size(); i++) {
            System.out.println(result.get(i));
        }


    }
}
