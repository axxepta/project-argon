package de.axxepta.oxygen.rest;

import com.sun.jersey.core.util.Base64;
import org.basex.io.IOStream;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import javax.swing.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class BasexWrapper extends RestWrapper{
    // BaseX database context
    //Context ctx = new Context();
    public ArrayList<String> tempList = new ArrayList<String>();
    String user;
    String pass;
    String host;
    int port;

    public BasexWrapper(){
        this.host = "127.0.0.1";
        this.user = "admin";
        this.pass = "admin";
        this.port = 8984;
    }

    private String getURL(){
        String url = "http://" + this.host + ':' + this.port + "/rest";
        return url;
    }

    private String getAuth(){
        String userpass = this.user + ":" + this.pass;
        String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
        return basicAuth;
    }

    @Override
    public ArrayList<String> getResources() throws IOException, SAXException, ParserConfigurationException {
        ArrayList<String> tList = new ArrayList<String>();

        URL url = null;
        try {
            url = new URL(getURL());
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

        //String userpass = "admin" + ":" + "admin";
        //String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
        String basicAuth = getAuth();

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
        System.err.println("Loaded XPath Provider " + xpath.getClass().getName() + " using factory " + xpath.getClass().getName());

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
