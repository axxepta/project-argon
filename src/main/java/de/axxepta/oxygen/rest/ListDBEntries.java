package de.axxepta.oxygen.rest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
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
  public static void main(String... args){
    try {
      ListDBEntries test = new ListDBEntries("test", "/");
    }
    catch (Exception er){}
  }

  /**
   * Example code.
   * @throws Exception ignored (shouldn't be)
   */

  public ListDBEntries(String db, String db_path) throws Exception {
    ArrayList<String> tList = new ArrayList<String>();

    // login data
    String user = "admin";
    String pass = "admin";
    String host = "localhost";
    int port = 8984;
    
    // build POST request
    TokenBuilder tb = new TokenBuilder();
    tb.add("<query xmlns='http://basex.org/rest'><text><![CDATA[");

    //Get file from resources folder
    String queryType = "/list-db-entries.xq";
    //String queryType = "D:\\cygwin\\home\\Markus\\code\\java\\project-argon\\src\\main\\resources\\xquery\\list-db-entries.xq";
    //String queryType ="xquery/list-restxq-entries.xq";
    ClassLoader classLoader = getClass().getClassLoader();
    //File qFile = new File(classLoader.getResource(queryType).getFile());
    URL furl = ListDBEntries.class.getResource(queryType);
    //System.out.println(furl.toURI());
    //File qFile = new File(furl.toString());

    //TODO: waaaay to hacky...
    Reader reader = new InputStreamReader(getClass().getResourceAsStream(queryType));

    int intValueOfChar;
    String query = "";
    while ((intValueOfChar = reader.read()) != -1) {
      query += (char) intValueOfChar;
    }
    reader.close();

    //String db = "test2";
    //String db_path = "/";
    String path = "./dba";
    //tb.add(new IOFile(qFile).read());
    tb.add(query);

    if (queryType.equals("/list-db-entries.xq"))
    {
      tb.add("]]></text><variable name=\"db\" value=\"" + db + "\"/><variable name=\"path\" value=\"" + db_path + "\"/></query>");
    } else {
      tb.add("]]></text><variable name=\"path\" value=\"" + path + "\"/></query>");
    }
    //JOptionPane.showMessageDialog(null, tb, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);

    // send request, receive response
    String basicAuth = "Basic " + new String(Base64.encode(user + ':' + pass));
    URL url = new URL("http://" + host + ':' + port + "/rest");
    // will always be HttpURLConnection if URL starts with "http://"
    //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    sun.net.www.protocol.http.HttpURLConnection conn = new sun.net.www.protocol.http.HttpURLConnection(url, null);
    conn.setRequestProperty("Authorization", basicAuth);
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.getOutputStream().write(tb.finish());
    String result = Token.string(new IOStream(conn.getInputStream()).read());
    // short-cut to convert result to BaseX XML node (-> interpret result as XQuery)
    ANode root = (ANode) query(result, null);

    //JOptionPane.showMessageDialog(null, result, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
    // this demonstrates how you can loop through the children of the root element
    // - similar to DOM, but more efficient and light-weight
    // - strings are usually represented as UTF8 byte arrays (BaseX term: "tokens")
    for(ANode resource : root.children()) {
      String type = name(resource);
      System.out.println("- " + value(resource) + " (" + type + "):");

      // output different results, depending on resource type
      switch(type) {
      case "document":
        // most efficient approach (directly access node attributes):
        System.out.println("  nodes: " + attribute(resource, "nodes"));
        // the XQuery approach:
        System.out.println("  nodes: " + query("@nodes/number", resource));
        break;
      case "binary":
        System.out.println("  size: " + attribute(resource, "size"));
        break;
      case "directory":
        break;
      }
    }

    for(ANode resource : root.children()) {
      String databaseEntry = value(resource);
      tList.add(databaseEntry);
     // JOptionPane.showMessageDialog(null, databaseEntry, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
    }
    for(ANode resource : root.children()) {
      String type = name(resource);
      tList.add(type);
    //  JOptionPane.showMessageDialog(null, type, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
    }
    this.result = tList;
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
