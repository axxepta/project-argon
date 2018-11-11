package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.ArgonConst;
import de.axxepta.oxygen.api.BaseXSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;

public class CustomProtocolURLUtils {

    private static final Logger logger = LogManager.getLogger(CustomProtocolURLUtils.class);

    public static String pathFromURL(URL url) {
        String urlString = "";
        try {
            urlString = java.net.URLDecoder.decode(url.toString(), "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException ex) {
            logger.error("URLDecoder error decoding " + url.toString(), ex.getMessage());
        }
        return pathFromURLString(urlString);
    }

    public static String pathFromURLString(String urlString) {
        String[] urlComponents = urlString.split(":/*");
        if (urlComponents.length < 2) {
            return "";
            // ToDo: exception handling
        } else {
            return urlComponents[1];
        }
    }

    public static BaseXSource sourceFromURL(URL url) {
        return sourceFromURLString(url.toString());
    }


    public static BaseXSource sourceFromURLString(String urlString) {
        String protocol;
        int ind1 = urlString.indexOf(":");
        if (ind1 == -1)     // no proper URL string, but used someplace
            protocol = urlString;
        else
            protocol = urlString.substring(0, ind1);
        switch (protocol) {
            case ArgonConst.ARGON_XQ: return BaseXSource.RESTXQ;
            case ArgonConst.ARGON_REPO: return BaseXSource.REPO;
            case ArgonConst.ARGON: return BaseXSource.DATABASE;
            default: return null;
        }
    }

    /*
    public static BaseXSource sourceFromURLString(String urlString) {
        final URI uri = URI.create(urlString);
        if (uri.getScheme() == null) {
            return null;
        }
        switch (uri.getScheme()) {
//            case ArgonConst.ARGON_XQ:
//                return BaseXSource.RESTXQ;
            case ArgonConst.ARGON_REPO:
                return BaseXSource.REPO;
            case ArgonConst.ARGON:
                return BaseXSource.DATABASE;
            default:
                return null;
        }
    }*/
}
