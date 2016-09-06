package de.axxepta.oxygen.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Markus on 25.08.2016.
 */
public final class XMLUtils {

    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private XMLUtils() {}

    public static Document docFromByteArray(byte[] is) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(is));
    }

    public static XPathExpression getXPathExpression(String xPath) throws XPathExpressionException {
        XPath xpath = xPathFactory.newXPath();
        return xpath.compile(xPath);
    }

}
