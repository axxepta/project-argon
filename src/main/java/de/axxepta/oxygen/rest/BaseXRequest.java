package de.axxepta.oxygen.rest;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.tree.TreeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper class for request to BaseX, connection details are "inherited" from the included connection
 */
public class BaseXRequest {
    private ArrayList<String> result;
    private String answer;
    private boolean check;

    public BaseXRequest(final String request, final BaseXSource source, final String path,
                        final String... params) throws IOException {
        Connection connection = BaseXConnectionWrapper.getConnection();
        if (connection != null) {
            switch (request) {
                case "create": result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.create(path, params[0], params[1]);
                    break;
                case "list":
                    answer = "";
                    check = false;
                    result = new ArrayList<>();
                    BaseXResource[] resources = connection.list(source, path);
                    for (BaseXResource resource : resources) {
                        String databaseEntry = resource.name;
                        result.add(databaseEntry);
                    }
                    for (BaseXResource resource : resources) {
                        String type = resource.type.toString();
                        result.add(type);
                    }
                    break;
                case "delete":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.delete(source, path);
                    break;
                case "rename":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    connection.rename(source, path, params[0]);
                    break;
                case "query":
                    result = new ArrayList<>();
                    answer = "";
                    check = false;
                    answer = "<response>\n" + connection.xquery(path) + "\n</response>";
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
                    StringBuilder regEx = new StringBuilder("");
                    for (int i=0; i<params[0].length(); i++) {
                        char c = params[0].charAt(i);
                        switch (c) {
                            case '*': regEx.append(".*"); break;
                            case '?': regEx.append('.'); break;
                            case '.': regEx.append("\\."); break;
                            default: regEx.append(c);
                        }
                    }
                    String regExString = regEx.toString();
                    result = connection.search(source, path, regExString);
                    for (int i=result.size()-1; i>-1; i--) {
                        String foundPath = result.get(i);
                        String foundFile = TreeUtils.fileStringFromPathString(foundPath);
                        Matcher matcher = Pattern.compile(regExString).
                                matcher(foundFile);
                        if (!matcher.find())
                            result.remove(foundPath);
                    }
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
