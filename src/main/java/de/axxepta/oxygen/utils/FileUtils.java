package de.axxepta.oxygen.utils;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.api.Connection;
import de.axxepta.oxygen.customprotocol.CustomProtocolURLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Markus on 19.02.2016.
 */
public final class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static boolean directoryExists(File dir) {
        if (dir == null)
            return false;
        return dir.exists() && dir.isDirectory();
    }

    public static void createDirectory(String name) {
        try {
            Path newPath = Paths.get(name);
            Files.createDirectory(newPath);
        } catch (IOException use) {
            logger.error("Error parsing path name to URI");
        }
    }

    public static void copyFromBaseXToFile(String fileToCopy, String destinationFile) throws IOException {
        BaseXSource source = CustomProtocolURLUtils.sourceFromURLString(fileToCopy);
        String path = CustomProtocolURLUtils.pathFromURLString(fileToCopy);

        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            byte[] bytesToCopy = connection.get(source, path, true);
            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(bytesToCopy);
            } catch (IOException ioe) {
                throw new IOException("Argon: Copying file to file system failed: " + destinationFile);
            }
        } catch (IOException ioe) {
            throw new IOException("Argon: Getting file to copy from database failed: " + destinationFile);
        }
    }

}
