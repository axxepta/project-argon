package de.axxepta.oxygen.customprotocol;

import ro.sync.net.protocol.convert.ConversionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

/**
 * @author Markus on 21.10.2016.
 */
public class ArgonProcessor implements ConversionProvider {

    public void convert(String systemID, String originalSourceSystemID, InputStream is, OutputStream os,
                        LinkedHashMap<String, String> properties) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }
}
