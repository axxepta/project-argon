package de.axxepta.oxygen.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Markus on 08.09.2016.
 */
public final class IOUtils {

    private static final Logger logger = LogManager.getLogger(IOUtils.class);

    private IOUtils() {}

    static byte[] getBytesFromInputStream(InputStream stream) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream boStream = new ByteArrayOutputStream()) {
            int nRead;
            int bufferSize = 1024;
            byte[] readData = new byte[bufferSize];
            while ((nRead = stream.read(readData, 0, bufferSize)) != -1) {
                boStream.write(readData, 0, nRead);
            }
            boStream.flush();
            bytes = boStream.toByteArray();
        }
        return bytes;
    }

    public static byte[] convertToUTF8(byte[] bytes, String encoding) {
        try {
            return new String(bytes, encoding).getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            try {
                logger.error("Error converting output to UTF-8 : ", uee.getMessage());
                return "<error>Original encoding was not supported.</error>".getBytes("UTF-8");
            } catch (UnsupportedEncodingException uuee) {
                logger.error("WHAT THE FUCK?");
                return new byte[0];
            }
        }
    }

    static byte[] returnUTF8Array(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            logger.error("Error converting output to UTF-8 : ", uee.getMessage());
            return new byte[0];
        }
    }

    static String returnUTF8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            logger.error("Error converting output to UTF-8 : ", uee.getMessage());
            return "";
        }
    }

    public static boolean isXML(byte[] bytes) {
        if (bytes.length > 4) {
            if ((bytes[0] == (byte)0xFE) && (bytes[1] == (byte)0xFF)) {         // check for UTF-16BE BOM
                return checkXML(Arrays.copyOfRange(bytes, 2, bytes.length - 1), "UTF-16BE");
            } else if ((bytes[0] == (byte)0xFF) && (bytes[1] == (byte)0xFE)) {  // check for UTF-16LE BOM
                return checkXML(Arrays.copyOfRange(bytes, 2, bytes.length - 1), "UTF-16LE");
            } else if ((bytes[0] == (byte)0xEF) && (bytes[1] == (byte)0xBB) && (bytes[2] == (byte)0xBF)) {  // check for UTF-8 BOM
                return checkXML(Arrays.copyOfRange(bytes, 3, bytes.length - 1), "UTF-8");
            } else if ((bytes[0] == (byte)0x3c) && (bytes[bytes.length - 1] == (byte)0x3e)) { // check for UTF-8/ISO 8859-1 code starting with '<', ending with '>'
                return true;
            } else {
                return checkXML(bytes, "UTF-8");
            }
        } else
            return false;
    }

    private static boolean isXML(String wannabe) {
        String trimmed = wannabe.trim();
        return (trimmed.startsWith("<") && trimmed.endsWith(">"));
    }

    private static boolean checkXML(byte[] bytes, String encoding) {
        try {
            String wannabe = new String(bytes, encoding);
            return isXML(wannabe);
        } catch (UnsupportedEncodingException use) {
            return false;
        }
    }

}
