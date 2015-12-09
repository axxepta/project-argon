package de.axxepta.oxygen.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * @author Markus on 03.11.2015.
 * Note: Couldn't use class ResourceBundle because ResourceBundle.getBundle searches for property files in the Oxygen.jar,
 * so had to implement a Bundle class which mimics ResouceBundle behavior
 */
public class Lang {

    private static final Logger logger = LogManager.getLogger(Lang.class);

    private static final String PATH = "/Argon";
    private static final Map<Locale, Bundle> availableResourceBundles = new HashMap<>();
    private static final String MISSING_KEY = "?";
    private static final String MISSING_RESOURCE = "??";

    private static Bundle currentBundle = null;

    public static void init() {
        init(Locale.UK);
    }

    public static void init(Locale locale) {
        loadBundle(Locale.GERMANY);
        loadBundle(Locale.UK);
        setLocale(locale);
    }

    private static void loadBundle(final Locale locale) {
        try
        {
            final Bundle bundle = new Bundle(PATH, locale);
            availableResourceBundles.put(locale, bundle);
        }
        catch (final IOException ex)
        {
            logger.warn("Failed to read resource '" + PATH + "' for locale: '" + locale + "'", ex);
        }
        catch (final NullPointerException ex) {
            logger.warn("Missing recource '" + PATH + "' for locale: '" + locale + "'", ex);
        }
    }

    public static void setLocale(Locale locale) {
        if (locale.equals(Locale.GERMANY) || locale.equals(Locale.GERMAN))
            currentBundle = availableResourceBundles.get(Locale.GERMANY);
        else
            currentBundle = availableResourceBundles.get(Locale.UK);
    }

    public static String get(Keys key){
        if (currentBundle != null) {
            String val = currentBundle.getString(key.name());
            if (val != null)
                return val;
            else
                return MISSING_KEY + key;
        } else {
            return MISSING_RESOURCE + key;
        }
    }

    public enum Keys {
        tree_root, tree_DB, tree_repo, tree_restxq, cm_checkout, cm_checkin, cm_adddb, cm_add, cm_delete,
        cm_rename, cm_newversion, cm_refresh, cm_search
    }

}

class Bundle {
    private Properties bundleMap;

    public Bundle (String path, Locale locale) throws IOException {
        StringBuilder propFile = new StringBuilder(path);
        if (locale.equals(Locale.GERMANY))
            propFile.append("_de_DE");
        else
            propFile.append("_en_GB");
        propFile.append(".properties");
        Reader inReader = new InputStreamReader(getClass().getResourceAsStream(propFile.toString()));
        bundleMap = new Properties();
        bundleMap.load(inReader);
        inReader.close();
    }

    public String getString(String key) {
        return bundleMap.getProperty(key);
    }
}