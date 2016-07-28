package de.axxepta.oxygen.utils;

/**
 * @author Markus on 28.07.2016.
 */
public final class StringUtils {

    private StringUtils() {}

    public static boolean isEmpty(String s) {
        return (s == null) || (s.equals(""));
    }

}
