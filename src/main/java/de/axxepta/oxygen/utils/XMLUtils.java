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

    static String encodingFromPrologue(String content) {
        String encoding = "";
        if (content.contains("<?xml ")) {
            int encPos = content.indexOf("encoding=");
            int encEndPos = content.indexOf("\"", encPos + 10);
            if (encPos != -1) {
                encoding = content.substring(encPos + 10, encEndPos);
            }
        }
        return encoding.toUpperCase();
    }

    public static String encodingFromBytes(byte[] content) {
        String encoding = "";
        if (content.length > 4) {
            if ((content[0] == (byte)0xFE) && (content[1] == (byte)0xFF)) {         // check for UTF-16BE BOM
                return "UTF-16BE";
            } else if ((content[0] == (byte)0xFF) && (content[1] == (byte)0xFE)) {  // check for UTF-16LE BOM
                return "UTF-16LE";
            } else if ((content[0] == (byte)0xEF) && (content[1] == (byte)0xBB) && (content[2] == (byte)0xBF)) {  // check for UTF-8 BOM
                return "UTF-8";
//            } else if ((content[0] == (byte)0x3c) && (content[content.length - 1] == (byte)0x3e)) {
                // ToDo: trim byte array and check for last byte
            } else if (content[0] == (byte)0x3c) {
                String contentString = IOUtils.returnUTF8String(content);
                encoding = encodingFromPrologue(contentString);
            }
        }
        return encoding;
    }
}
