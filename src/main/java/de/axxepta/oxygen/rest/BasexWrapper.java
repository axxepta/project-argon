package de.axxepta.oxygen.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by daltiparmak on 10.04.15.
 */
public class BasexWrapper extends RestWrapper{

    @Override
    public ArrayList<String> getResources() {

        ArrayList<String> tempList = new ArrayList<String>();

        WebResource webResource = this.client.resource("http://127.0.0.1:8984/rest");
        ClientResponse response = webResource.type("application/xml").get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        try {
            String xml = response.getEntity(String.class);

            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource ipsource = new InputSource(new StringReader(xml));

            String expression = "/databases/database";

            NodeList nodes = (NodeList) xpath.evaluate(expression, ipsource, XPathConstants.NODESET);

            for (int i=0; i<nodes.getLength(); i++) {
                String database =  nodes.item(i).getTextContent();
                tempList.add(database);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempList;
    }

    public static Document loadXML(String xml) throws Exception
    {
        DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
        DocumentBuilder bldr = fctr.newDocumentBuilder();
        InputSource insrc = new InputSource(new StringReader(xml));
        return bldr.parse(insrc);
    }


/*
    public static void main(String[] args){


        try {

            String username = "admin";
            String password = "admin";


            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter(username, password));

            WebResource webResource = client.resource("http://admin:admin@127.0.0.1:8984/rest");

            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            String output = response.getEntity(String.class);

            System.out.println("Server response : \n");
            System.out.println(output);
            System.out.println("Server response : \n");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
*/
}
