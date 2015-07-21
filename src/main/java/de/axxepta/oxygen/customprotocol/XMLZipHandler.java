package de.axxepta.oxygen.customprotocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handler for the "filexmlzip" protocol. Can be used to open/edit XML files directly from ZIPs
 */
public class XMLZipHandler extends URLStreamHandler {
  
  /**
   * The filexmlzip protocol
   */
  private static final String FILEZIP = "filexmlzip";
  
  /**
   * Connection class for XML files in archives.
   */
  private static class ArchiveXMLConnection extends URLConnection {
    /**
     * The wrapped URL connection.
     */
    private URLConnection zipURLConnection = null;
    /**
     * Construct the connection
     * 
     * @param url The URL
     */
    protected ArchiveXMLConnection(URL url) throws IOException{
      super(url);
      // Allow output
      setDoOutput(true);
      String str = url.toString();
      int index = str.indexOf(FILEZIP);
      //Convert it back to simple file URL.
      str = "file" + str.substring(index + FILEZIP.length());
      URL xmlURL = getXMLURLFromInsideZip(new URL(str));
      if(xmlURL != null) {
        zipURLConnection = xmlURL.openConnection();
      } else {
        throw new IOException("Could not find \".xml\" file inside archive.");
      }
    }

    /**
     * Returns an input stream that reads from this open connection.
     * 
     * @return the input stream
     */
    public InputStream getInputStream() throws IOException {
      return zipURLConnection.getInputStream();
    }

    /**
     * Returns an output stream that writes to this connection.
     * 
     * @return the output stream
     */
    public OutputStream getOutputStream() throws IOException {
      return zipURLConnection.getOutputStream();
    }

    /**
     * Opens a communications link to the resource referenced by this URL, if such a connection has
     * not already been established.
     */
    public void connect() throws IOException {
      zipURLConnection.connect();
    }
    
    /**
     * @see java.net.URLConnection#getContentLength()
     */
    @Override
    public int getContentLength() {
      return zipURLConnection.getContentLength();
    }
    
    /**
     * @see java.net.URLConnection#getContentType()
     */
    @Override
    public String getContentType() {
      //Recognized as XML type.
      return "text/xml";
    }
  }

  /**
   * Creates and opens the connection
   * 
   * @param u
   *          The URL
   * @return The connection
   */
  protected URLConnection openConnection(URL u) throws IOException {
    URLConnection connection = new ArchiveXMLConnection(u);
    return connection;
  }
  
  /**
   * Get the first found XML file from the archive.
   * @param zipURL The ZIP URL.
   * @return the first found XML file from the archive.
   */
  private static URL getXMLURLFromInsideZip(URL zipURL) {
    try {
      File zipFile = new File(URLDecoder.decode(zipURL.getFile(), "UTF8"));
      if(zipFile.exists()) {
        ZipFile dxpZip = new ZipFile(zipFile);
        try {
          Enumeration<? extends ZipEntry> entries = dxpZip.entries();
          while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryFileName = entry.getName();
            int pathIndex = entryFileName.lastIndexOf("/");
            if(pathIndex  != -1 && pathIndex < entryFileName.length() - 1) {
              entryFileName = entryFileName.substring(pathIndex + 1);
            }
            if(entryFileName.endsWith(".xml")) {
              //Found one
              URL entryURL = new URL("zip:" + zipURL.toString() + "!/" + entry.getName());
              return entryURL;
            }
          }
        } finally {
          dxpZip.close();
        }
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}