package de.axxepta.oxygen.utils;

import java.net.URL;

/**
 * @author Markus on 09.12.2015.
 */
public class URLUtils {

    public static boolean isXML(String file) {
        return (file.toLowerCase().endsWith(".xml") ||
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

    public static boolean isQuery(URL url) {
        return isQuery(url.toString());
    }

}
