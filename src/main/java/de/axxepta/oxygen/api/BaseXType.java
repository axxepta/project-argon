package de.axxepta.oxygen.api;

import java.util.*;

/**
 * Resource types.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public enum BaseXType {
    /** Directory. */
    DIRECTORY,
    /** Resource. */
    RESOURCE;

    /**
     * Returns a resource.
     * @param string string representation
     * @return enumeration
     */
    public static BaseXType get(final String string) {
        return BaseXType.valueOf(string.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}