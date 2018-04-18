package de.axxepta.oxygen.utils;

import java.util.Date;

/**
 * @author Markus on 28.07.2016.
 */
public final class StringUtils {

    public static final String LF = System.getProperty("line.separator");

    private StringUtils() {}

    public static boolean isEmpty(String s) {
        return (s == null) || (s.equals(""));
    }

}
