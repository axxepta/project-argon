package de.axxepta.oxygen.rest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;

import org.basex.core.Context;
import org.basex.io.IOFile;
import org.basex.io.IOStream;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.basex.query.value.node.ANode;
import org.basex.util.Base64;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

import javax.swing.*;

/**
 * Example for retrieving and processing data via REST from BaseX.
 *
 * @author Christian Gruen, 2015
 */
public class ListDBEntries {
    // BaseX database context
    Context ctx = new Context();
    private ArrayList<String> result;
    // login data
    String user = "admin";
    String pass = "admin";
    String host = "localhost";
    int port = 8984;

    public static void main(String... args) {
        try {
            ListDBEntries test = new ListDBEntries("restxq", "test", "/");
        } catch (Exception er) {
        }
    }

    public ListDBEntries(String queryType, String db, String db_path) throws Exception {
        ArrayList<String> tList = new ArrayList<String>();

        // build POST request
        TokenBuilder tb;
        tb = getFileQuery(queryType, db, db_path);

        // send request, receive response
        String result = postRequest(tb);

        // short-cut to convert result to BaseX XML node (-> interpret result as XQuery)
        ANode root = (ANode) query(result, null);

        // - strings are usually represented as UTF8 byte arrays (BaseX term: "tokens")
        for (ANode resource : root.children()) {
            String databaseEntry = value(resource);
            tList.add(databaseEntry);
        }
        for (ANode resource : root.children()) {
            String type = name(resource);
            tList.add(type);
        }
        this.result = tList;
        //JOptionPane.showMessageDialog(null, tList, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
    }


    private TokenBuilder getFileQuery(String qType, String db, String db_path) throws Exception {
        // build POST request
        TokenBuilder tb = new TokenBuilder();
        tb.add("<query xmlns='http://basex.org/rest'><text><![CDATA[");

        //Get file from resources folder
        if(qType.equals("db"))
        {
            qType = "/list-db-entries.xq";
        }
        else
        {
            qType = "/list-restxq-entries.xq";
        }

        //String queryType = "D:\\cygwin\\home\\Markus\\code\\java\\project-argon\\src\\main\\resources\\xquery\\list-db-entries.xq";
        //String queryType ="xquery/list-restxq-entries.xq";

    //////// throws exception, but works anyway!
        //ClassLoader classLoader = getClass().getClassLoader();
        //File qFile = new File(classLoader.getResource(queryType).getFile());
        //tb.add(new IOFile(qFile).read());
    //////////

    //////// throws path exception, but works anyway!
        //File qFile = new File(ListDBEntries.class.getResource(queryType).getFile());
        //tb.add(new IOFile(qFile).read());
    ////////////

    ////// doesn't work
        //URL furl = ListDBEntries.class.getResource(queryType);
        //System.out.println(furl.toURI());
        //File qFile = new File(furl.toString());
        //tb.add(new IOFile(qFile).read());
    ////////

    ///////// works, BUT...   --and throws an exception at first call
        //TODO: waaaay to hacky...
    /*    Reader reader = new InputStreamReader(getClass().getResourceAsStream(queryType));

        int intValueOfChar;
        String query = "";
        while ((intValueOfChar = reader.read()) != -1) {
          query += (char) intValueOfChar;
        }
        reader.close();
        tb.add(query);*/
    ////////

    ////////  works, but still a bit hacky--and throws an exception at first call
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(qType));
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String lineFromBuffer;
        while((lineFromBuffer=br.readLine())!=null)
        {
            sb.append(lineFromBuffer);
        }
        br.close();
        isr.close();
        tb.add(sb.toString());
    //////

        if (qType.equals("/list-db-entries.xq")) {
            tb.add("]]></text><variable name=\"db\" value=\"" + db + "\"/><variable name=\"path\" value=\"" + db_path + "\"/></query>");
        } else {
            tb.add("]]></text><variable name=\"path\" value=\"" + db_path + "\"/></query>");
        }

        return tb;
    }

    private String postRequest(TokenBuilder tb) throws Exception {
        String basicAuth = "Basic " + new String(Base64.encode(this.user + ':' + this.pass));
        URL url = new URL("http://" + this.host + ':' + this.port + "/rest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(tb.finish());
        os.close();
        String res = Token.string(new IOStream(conn.getInputStream()).read());
        return res;
    }

  /**
   * Convenience method for running an XQuery expression and returns the result.
   * @param query query
   * @param item initial context item (can be null)
   * @return name of element
   * @throws QueryException query exception
   */
  Value query(String query, Value item) throws QueryException {
    final QueryProcessor qp = new QueryProcessor(query, ctx);
    if(item != null) qp.context(item);
    return qp.value();
  }

  /**
   * Convenience method for returning the name of an XML node.
   * @param node node
   * @return name of element
   */
  String name(ANode node) {
    return Token.string(node.name());
  }

  /**
   * Convenience method for returning the string value of an XML node.
   * @param node node
   * @return string value
   */
  String value(ANode node) {
    return Token.string(node.string());
  }

  /**
   * Convenience method for returning the attribute value of an XML element.
   * @param node element
   * @param name name of attribute
   * @return attribute value
   */
  String attribute(ANode node, String name) throws Exception {
    return Token.string(node.attribute(name));
  }

  public ArrayList<String> getResult(){
    return this.result;
  }

}
