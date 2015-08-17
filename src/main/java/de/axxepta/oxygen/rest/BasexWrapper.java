package de.axxepta.oxygen.rest;

import com.sun.jersey.core.util.Base64;
import org.basex.core.Context;
import org.basex.io.IOFile;
import org.basex.io.IOStream;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.basex.query.value.node.ANode;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class BasexWrapper extends RestWrapper{
    // BaseX database context
    Context ctx = new Context();
    public ArrayList<String> tempList = new ArrayList<String>();

    @Override
    public ArrayList<String> getResources() throws IOException, SAXException, ParserConfigurationException {
        ArrayList<String> tList = new ArrayList<String>();

        URL url = null;
        try {
            url = new URL("http://127.0.0.1:8984/rest");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            BasexWrapper req = new BasexWrapper();
            try {
                req.getHTTPXml(url);
                tList = req.tempList;
            } catch (XPathFactoryConfigurationException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return tList;
    }


    private void getHTTPXml(URL url) throws ParserConfigurationException, IOException,
            SAXException, XPathFactoryConfigurationException {

        String userpass = "admin" + ":" + "admin";
        String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));

        sun.net.www.protocol.http.HttpURLConnection conn = new sun.net.www.protocol.http.HttpURLConnection(url, null);
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("ACCEPT", "application/xml");

        InputStream xml = conn.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(xml);

        /// optional namespace spec: xmlns:prefix:URI
        String nsPrefix = null;
        String nsUri = null;
        String namespace = "xmlns:rest=\"http://basex.org/rest\"";
        if (namespace.startsWith("xmlns:")){
            String[] nsDef = namespace.substring("xmlns:".length()).split("=");
            if (nsDef.length == 2) {
                nsPrefix = nsDef[0];
                nsUri = nsDef[1];
            }
        }

        // Find nodes by XPATH
        System.setProperty("javax.xml.xpath.XPathFactory:" + XPathConstants.DOM_OBJECT_MODEL, "org.apache.xpath.jaxp.XPathFactoryImpl");

        XPathFactory xFactory = null;
        xFactory = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL);

        System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, " org.apache.xpath.jaxp.XPathFactoryImpl");


        System.out.println(xFactory.toString());
        XPath xpath = xFactory.newXPath();
        System.err.println("Loaded XPath Provider " + xpath.getClass().getName() +" using factory " + xpath.getClass().getName());

        // namespace?
        if (nsPrefix != null) {
            final String myPrefix = nsPrefix;
            final String myUri = nsUri;
            xpath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    return myPrefix.equals(prefix) ?  myUri : null;
                }

                public String getPrefix(String namespaceURI) {
                    return null; // we are not using this.
                }

                public Iterator getPrefixes(String namespaceURI) {
                    return null; // we are not using this.
                }
            });
        }

        String expression = "//databases/database";

        XPathExpression expr = null;
        try {
            expr = xpath.compile(expression);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        NodeList nodes = null;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        for (int i=0; i<nodes.getLength(); i++) {
            String database =  nodes.item(i).getTextContent();
            this.tempList.add(database);
        }

        if (nodes.getLength() < 1) {
            System.out.println("Can't find node by XPATH: " + expression);
        }
        else {
            System.out.println(nodes.getLength()
                    + " nodes found for "
                    + expression);
        }

        conn.disconnect();
    }

    // ToDo: check for optional parameters db and db_path!
    public ArrayList<String> ListDBEntries(String reqType, String... paras) throws Exception {
        ArrayList<String> tList = new ArrayList<String>();

        // login data
        String user = "admin";
        String pass = "admin";
        String host = "localhost";
        String db, db_path;
        int port = 8984;
        File qFile;
        // build POST request
        TokenBuilder tb = new TokenBuilder();
        tb.add("<query xmlns='http://basex.org/rest'><text><![CDATA[");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        if (reqType.equals("db-entries")) {

            //reqType = "xquery/list-db-entries.xq";
            reqType = "D:\\cygwin\\home\\Markus\\code\\java\\project-argon\\src\\main\\resources\\xquery\\list-db-entries.xq";
            db = paras[0];
            db_path = paras[1];
            try {
                //qFile = new File(classLoader.getResource(reqType).getFile());
                qFile = new File(reqType);
                tb.add(new IOFile(qFile).read());
            } catch (Exception e1){
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "xq file not found", "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
            }
            tb.add("]]></text><variable name=\"db\" value=\"" + db + "\"/><variable name=\"path\" value=\"" + db_path + "\"/></query>");
        } else {
            //reqType = "xquery/list-restxq-entries.xq";
            reqType = "D:\\cygwin\\home\\Markus\\code\\java\\project-argon\\src\\main\\resources\\xquery\\list-restxq-entries.xq";
            //qFile = new File(classLoader.getResource(reqType).getFile());
            qFile = new File(reqType);
            tb.add(new IOFile(qFile).read());
            tb.add("]]></text></query>");
        }

        // send request, receive response
        String basicAuth = "Basic " + new String(org.basex.util.Base64.encode(user + ':' + pass));
        URL url = new URL("http://" + host + ':' + port + "/rest");
        // will always be HttpURLConnection if URL starts with "http://"
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(tb.finish());
        String result = Token.string(new IOStream(conn.getInputStream()).read());
        // short-cut to convert result to BaseX XML node (-> interpret result as XQuery)
        ANode root = (ANode) query(result, null);
        JOptionPane.showMessageDialog(null, result, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
        // this demonstrates how you can loop through the children of the root element
        // - similar to DOM, but more efficient and light-weight
        // - strings are usually represented as UTF8 byte arrays (BaseX term: "tokens")
        for(ANode resource : root.children()) {
            String type = name(resource);
            String databaseEntry = value(resource);
            tList.add(databaseEntry);
            JOptionPane.showMessageDialog(null, databaseEntry, "ListDBEntries", JOptionPane.PLAIN_MESSAGE);
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
        return tList;
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

}
