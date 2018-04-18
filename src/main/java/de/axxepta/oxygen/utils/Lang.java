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
            logger.warn("Missing resource '" + PATH + "' for locale: '" + locale + "'", ex);
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
        tree_root, tree_DB, tree_repo, tree_restxq, cm_open, cm_checkout, cm_checkin, cm_adddb, cm_add, cm_addsimple,
        cm_addfile, cm_delete, cm_rename, cm_newversion, cm_showversion, cm_refresh, cm_search, cm_searchsimple, cm_find,
        cm_newdir, cm_save, cm_ok, cm_cancel, cm_tofile, cm_todb, cm_export, cm_checkinselected, cm_exit, cm_saveas,
        cm_replycomment, cm_runquery, cm_yes, cm_no, cm_all, cm_always, cm_never, cm_compare, cm_reset, cm_overwrite,
        cm_nosave,
        lbl_filename, lbl_filetype, lbl_filestocheck, lbl_delete, lbl_dir, lbl_searchpath, lbl_elements, lbl_text,
        lbl_attributes, lbl_attrbvalues, lbl_scope, lbl_options, lbl_whole, lbl_casesens, lbl_snippet,
        lbl_search1, lbl_search2, lbl_search3, lbl_search4, lbl_overwrite, lbl_version, lbl_revision, lbl_date, lbl_closed,
        lbl_connection, lbl_host, lbl_user, lbl_pwd, lbl_vcs, lbl_fe, lbl_dboptions, lbl_chop, lbl_ftindex, lbl_textindex,
        lbl_attributeindex, lbl_tokenindex, lbl_fixdefault, tt_fe,
        title_connection, title_connection2, title_history,
        warn_failednewdb, warn_failednewfile, warn_faileddeletedb, warn_faileddelete,
        warn_failedexport, warn_failednewversion, warn_norename, warn_resource, warn_storing, warn_locked, warn_nosnippet,
        warn_nofile, warn_failedsearch, warn_failedlist, warn_notransfer, warn_transfernoread,
        warn_transfernowrite1, warn_transfernowrite2, warn_connectionsettings1, warn_connectionsettings2, warn_settingspath1,
        warn_settingspath2, warn_settingspath3,
        msg_dbexists1, msg_dbexists2, msg_fileexists1, msg_fileexists2, msg_noquery, msg_noeditor, msg_checkpriordelete,
        msg_noupdate1, msg_noupdate2, msg_norename1, msg_norename2, msg_norename3, msg_transferlocked, msg_nameexists,
        msg_settingsexists, msg_noscopeselected,
        dlg_addfileinto, dlg_externalquery, dlg_checkedout, dlg_delete, dlg_newdir, dlg_replycomment, dlg_saveas, dlg_open,
        dlg_snippet, dlg_foundresources, dlg_overwrite, dlg_closed, dlg_overwritesetting, dlg_newsetting
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