package de.axxepta.oxygen.rest;

import de.axxepta.oxygen.api.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Wrapper class for request to BaseX, connection details are "inherited" from the included connection
 */
public class BaseXRequest {
    private ArrayList<String> result;
    private String answer;
    private boolean check;

    public BaseXRequest(final String request, final BaseXSource source, final String path,
                        final String... params) throws Exception {
        Connection connection = (new BaseXConnectionWrapper()).getConnection();
        if (connection != null) {
            switch (request) {
                case "list":
                    answer = "";
                    check = false;
                    result = new ArrayList<>();
                    BaseXResource[] resources = connection.list(source, path);
                    for (int i=0; i< resources.length; i++) {
                        String databaseEntry = resources[i].name;
                        result.add(databaseEntry);
                    }
                    for (int i=0; i<resources.length; i++) {
                        String type = resources[i].type.toString();
                        result.add(type);
                    }
                    break;
                case "delete":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.delete(source, path);
                    break;
                case "query":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    answer = connection.xquery(path);
                    break;
                case "parse":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    try {
                        connection.parse(path);
                    } catch(BaseXQueryException ex) {
                        result.add(Integer.toString(ex.getLine()));
                        result.add(Integer.toString(ex.getColumn()));
                        result.add(ex.getInfo());
                        break;
                    }
                    check = true;
                    break;
                case "unlock":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.unlock(source, path);
                    break;
                case "look":
                    answer = "";
                    check = false;
                    result = connection.search(source, path, params[0]);
                    break;
                default: result = new ArrayList<>();
                    answer = "";
                    check = false;
            }
            try {
                connection.close();
            } catch (IOException er) {
                er.printStackTrace();
            }
        } else {
            result = new ArrayList<>();
            answer = "";
        }
    }

    public ArrayList<String> getResult() {
        return result;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isCheck() {
        return check;
    }
}
