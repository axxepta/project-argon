package de.axxepta.oxygen.utils;

import java.io.File;

/**
 * @author Markus on 19.02.2016.
 */
public class FileUtils {
    public static boolean directoryExists(File dir) {
        if (dir == null)
            return false;
        return dir.exists() && dir.isDirectory();
    }
}
