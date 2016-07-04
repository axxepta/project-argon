package de.axxepta.oxygen.customprotocol;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus on 28.10.2015.
 * The enwraped map contains information about which Argon URLs are opened in editors and their read-only status
 * (i.e. whether the user was asked to reload the URL after another user closed it, which should be asked only once)
 */
public class ArgonEditorsWatchMap {

    static private Map<URL, Boolean> URLMap;

    public static void init() {
        URLMap = new HashMap<>();
    }

    public static void addURL(URL url) {
        if (!isURLInMap(url) || !askedForAccess(url))
            URLMap.put(url, false);
    }

    public static void removeURL(URL url) {
        URLMap.remove(url);
    }

    public static void setAsked(URL url) {
        URLMap.put(url, true);
    }

    static boolean askedForAccess(URL url) {
        if (URLMap.get(url) == null) {
            return false;
        } else {
            return URLMap.get(url);
        }
    }

    static boolean isURLInMap(URL url) {
        return !(URLMap.get(url) == null);
    }

}
