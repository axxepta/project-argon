/**
 *
 */
package de.axxepta.oxygen.customprotocol;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import de.axxepta.oxygen.api.BaseXClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Daniel Altiparmak
 *
 *
 */
public class BaseXByteArrayOutputStream extends ByteArrayOutputStream {

    private static final Logger logger = LogManager.getLogger(BaseXByteArrayOutputStream.class);
    private final URL url;

    public BaseXByteArrayOutputStream(URL url) {
        super();
        this.url = url;
    }

    @Override
    public void close() throws IOException {
        super.close();


        byte[] savedBytes = toByteArray();

        final BaseXClient session = new BaseXClient("localhost", 1984, "admin","admin");
        InputStream bais = new ByteArrayInputStream(savedBytes);

        String argonUrlPath = this.url.getPath();
        argonUrlPath = argonUrlPath.replaceFirst("/", "");
        String[] parts = argonUrlPath.split("/", 2);
        String database = parts[0];
        String path = parts[1];

        logger.info("Database: " + database);
        logger.info("Path: " + path);

        // open BaseX database
        session.execute(MessageFormat.format("open {0}", database));
        logger.info(session.info());

        try {
            // replace document
            session.replace(path, bais);
            logger.info(session.info());

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            logger.error(e1);
        } finally {
            session.close();
        }

    }
}