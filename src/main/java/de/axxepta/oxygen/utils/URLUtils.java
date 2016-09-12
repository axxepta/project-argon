package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;

import java.net.URL;

/**
 * @author Markus on 09.12.2015.
 */
public final class URLUtils {

    private URLUtils() {}

    public static boolean isXML(String file) {
        return (file.toLowerCase().endsWith(".xml") ||
                file.toLowerCase().endsWith(".svg") ||
                file.toLowerCase().endsWith(".dita") ||
                file.toLowerCase().endsWith(".ditaval") ||
                file.toLowerCase().endsWith(".ditamap") );
    }

    public static boolean isXML(URL url) {
        return isXML(url.toString());
    }

    public static boolean isQuery(String file) {
        return (file.toLowerCase().endsWith(".xq") ||
                file.toLowerCase().endsWith(".xqm") ||
                file.toLowerCase().endsWith(".xql") ||
                file.toLowerCase().endsWith(".xqy") ||
                file.toLowerCase().endsWith(".xquery") );
    }

    /**
     * Tests whether the file extension indicates a non-XML file type, cannot exclude false negatives (file might be XML anyway)
     * @param url URL of file
     * @return true if non-xml type for sure
     */
    public static boolean isBinary(URL url) {
        return isBinary(url.toString());
    }

    /**
     * Tests whether the file extension indicates a non-XML file type, cannot exclude false negatives (file might be XML anyway)
     * @param file name of file
     * @return true if non-xml type for sure
     */
    public static boolean isBinary(String file) {
        return ((file.toLowerCase().endsWith(".gif") ||
                file.toLowerCase().endsWith(".png") ||
                file.toLowerCase().endsWith(".eps") ||
                file.toLowerCase().endsWith(".tiff") ||
                file.toLowerCase().endsWith(".jpg") ||
                file.toLowerCase().endsWith(".jpeg") ||
                file.toLowerCase().endsWith(".doc") ||
                file.toLowerCase().endsWith(".docx") ||
                file.toLowerCase().endsWith(".ppt") ||
                file.toLowerCase().endsWith(".xls") ||
                file.toLowerCase().endsWith(".xlsx") ||
                file.toLowerCase().endsWith(".dll") ||
                file.toLowerCase().endsWith(".exe") ||
                file.toLowerCase().endsWith(".htm") ||     // store only XML as text
                file.toLowerCase().endsWith(".html") ||
                file.toLowerCase().endsWith(".css") ||
                file.toLowerCase().endsWith(".txt") ||
                isQuery(file)) && !isXML(file));
    }

    public static boolean isQuery(URL url) {
        return isQuery(url.toString());
    }

    public static boolean isArgon(URL url) {
        return url.toString().toLowerCase().startsWith(CustomProtocolURLHandlerExtension.ARGON);
    }

}
