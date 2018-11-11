package de.axxepta.oxygen.api;

import java.util.Locale;

/**
 * Resource types.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public enum BaseXSource {

    /**
     * Database.
     */
    DATABASE("argon"),
//    /**
//     * RESTXQ.
//     */
    RESTXQ("argonquery"),
    /**
     * Repository.
     */
    REPO("argonrepo");

    private final String protocol;

    BaseXSource(String protocol) {

        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns a source.
     *
     * @param string string representation
     * @return enumeration
     */
    public static BaseXSource get(final String string) {
        return BaseXSource.valueOf(string.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}