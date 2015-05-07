package de.axxepta.oxygen.rest;

import com.sun.jersey.core.util.Base64;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class BasexWrapper extends RestWrapper{

    public ArrayList<String> tempList = new ArrayList<String>();

    @Override
    public ArrayList<String> getResources() throws IOException, SAXException, ParserConfigurationException {


        URL url = null;
        try {
            url = new URL("http://127.0.0.1:8984/rest");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        BasexWrapper req = new BasexWrapper();

        try {
            req.getHTTPXml(url);
        } catch (XPathFactoryConfigurationException e) {
            e.printStackTrace();
        }

        return req.tempList;
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

}
