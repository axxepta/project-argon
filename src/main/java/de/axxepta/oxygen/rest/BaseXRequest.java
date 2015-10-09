package de.axxepta.oxygen.rest;

import de.axxepta.oxygen.api.BaseXResource;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.api.ConnectionUtils;
import de.axxepta.oxygen.workspace.BaseXOptionPage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Markus on 05.10.2015.
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
                    connection.parse(source, path);
                    break;
                case "unlock":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.unlock(source, path);
                    break;
                case "look":
                    // ToDo: make connection.request public? use binding instead of compound query
                    result = new ArrayList<>();
                    check = false;
                    StringBuilder query;
                    query = new StringBuilder("let $xpath := '" + path + "'\n");
                    switch (source) {
                        case DATABASE: query.append(ConnectionUtils.getQuery("search-database")) ;
                            break;
                        case RESTXQ: query.append(ConnectionUtils.getQuery("search-restxq"));
                            break;
                        default: query.append(ConnectionUtils.getQuery("search-repo"));
                    }
                    answer = connection.xquery(query.toString());
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
