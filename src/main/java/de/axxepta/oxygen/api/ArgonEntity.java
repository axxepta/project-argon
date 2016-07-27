package de.axxepta.oxygen.api;

import java.util.Locale;

/**
 * @author Markus on 27.07.2016.
 */
public enum ArgonEntity {

    FILE,
    DIR,
    DB,
    DB_BASE,
    REPO,
    XQ,
    ROOT;

    public static ArgonEntity get(final String string) {
        return ArgonEntity.valueOf(string.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }

}
