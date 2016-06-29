package de.axxepta.oxygen.api;

import static de.axxepta.oxygen.api.ConnectionUtils.*;
import static org.basex.util.http.HttpMethod.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.Base64;
import org.basex.util.http.*;

/**
 * BaseX REST implementation for the Argon connection interface.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public class RestConnection implements Connection {
    /** URI. */
    protected final IOUrl url;
    protected final URL sUrl;
    protected final String basicAuth;

    /**
     * Constructor.
     * @param server server name
     * @param port connection port
     * @param user user string
     * @param password password string
     */
    public RestConnection(final String server, final int port, final String user,
                          final String password) throws MalformedURLException {
        //url = new IOUrl("http://" + user + ":" + password + "@" + server + ":" + port + "/rest");
        url = new IOUrl("http://" + user + ":" + password + "@" + server );
        basicAuth = "Basic " + Base64.encode(user + ':' + password);
        sUrl = new URL("http://" + user + ":" + password + "@" + server );
    }

    @Override
    public void close() throws IOException {
        // nothing to do (connections are not kept open)
    }

    @Override
    public BaseXResource[] list(final BaseXSource source, final String path) throws IOException {
        final String result = Token.string(request(getQuery("list-" + source), PATH, path));
        final ArrayList<BaseXResource> list = new ArrayList<>();
        if(!result.isEmpty()) {
            final String[] results = result.split("\r?\n");
            for(int r = 0, rl = results.length; r < rl; r += 2) {
                list.add(new BaseXResource(results[r + 1], BaseXType.get(results[r]), source));
            }
        }
        return list.toArray(new BaseXResource[list.size()]);
    }

    @Override
    public void init() throws IOException {
        request(getQuery("init"), RESOURCE, prepare(getAPIResource("MetaTemplate.xml").getBytes("UTF-8")));
    }

    // CAVE: If new options are inserted, also change call in BaseXByteArrayOutputStream.getBackupPath
    @Override
    public void create(String database, String chop, String ftindex) throws IOException {
        request(getQuery("create-database"), DATABASE, database, CHOP, chop, FTINDEX, ftindex);
    }

    @Override
    public byte[] get(final BaseXSource source, final String path) throws IOException {
        return request(getQuery("get-" + source), PATH, path);
    }

    @Override
    public void put(final BaseXSource source, final String path, final byte[] resource)
            throws IOException {
        request(getQuery("put-" + source), PATH, path, RESOURCE, prepare(resource));
    }

    @Override
    public void delete(final BaseXSource source, final String path) throws IOException {
        request(getQuery("delete-" + source), PATH, path);
    }

    @Override
    public void rename(final BaseXSource source, final String path, final String newPath) throws IOException {
        request(getQuery("rename-" + source), PATH, path, NEWPATH, newPath);
    }

    @Override
    public ArrayList<String> search(final BaseXSource source, final String path, final String filter) throws IOException {
        final String result = Token.string(request(getQuery("search-" + source), PATH, path, FILTER, filter));
        String[] resultArr = result.isEmpty() ? new String[0] : result.split("\r?\n");
        return new ArrayList<>(Arrays.asList(resultArr));
    }

    @Override
    public String xquery(final String query) throws IOException {
        try {
            return Token.string(request(query));
        } catch(final IOException ex) {
            throw BaseXQueryException.get(ex);
        }
    }

    @Override
    public void parse(final String xquery) throws IOException {
        request(getQuery("parse"), XQUERY, xquery);
    }

    @Override
    public void parse(final BaseXSource source, final String path) throws IOException {
        request(getQuery("parse-" + source), PATH, path);
    }

    @Override
    public void lock(final BaseXSource source, final String path) throws IOException {
        request(getQuery("lock"), SOURCE, source.toString(), PATH, path);
    }

    @Override
    public void unlock(final BaseXSource source, final String path) throws IOException {
        request(getQuery("unlock"), SOURCE, source.toString(), PATH, path);
    }

    @Override
    public boolean locked(final BaseXSource source, final String path) throws IOException {
        final byte[] result = request(getQuery("locked"), SOURCE, source.toString(), PATH, path);
        return Token.string(result).equals("true");
    }

    @Override
    public boolean lockedByUser(final BaseXSource source, final String path) throws IOException {
        final byte[] result = request(getQuery("lockedByUser"), SOURCE, source.toString(), PATH, path);
        return Token.string(result).equals("true");
    }

    @Override
    public boolean noLockSet(final BaseXSource source, final String path) throws IOException {
        final byte[] result = request(getQuery("no-lock-set"), SOURCE, source.toString(), PATH, path);
        return Token.string(result).equals("true");
    }

    @Override
    public String[] users() throws IOException {
        final String result = Token.string(request(getQuery("users")));
        return result.isEmpty() ? new String[0] : result.split("\r?\n");
    }

    /**
     * Executes the specified HTTP request and returns the result.
     * @param body request body
     * @param bindings keys and values
     * @return string result, or {@code null} for a failure.
     * @throws IOException I/O exception
     */
    protected byte[] request(final String body, final String... bindings) throws IOException {
        //final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.connection();
        sun.net.www.protocol.http.HttpURLConnection conn = new sun.net.www.protocol.http.HttpURLConnection(sUrl, null);
        try {
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setDoOutput(true);
            conn.setRequestMethod(POST.name());
            conn.setRequestProperty(HttpText.CONTENT_TYPE, MediaType.APPLICATION_XML.toString());

            // build and send query
            final TokenBuilder tb = new TokenBuilder();
            tb.add("<query xmlns='http://basex.org/rest'>\n");
            tb.add("<text>").add(toEntities(body)).add("</text>\n");
            for(int b = 0, bl = bindings.length; b < bl; b += 2) {
                tb.add("<variable name='").add(bindings[b]).add("' value='");
                tb.add(toEntities(bindings[b + 1])).add("'/>\n");
            }
            tb.add("</query>");
            try(final OutputStream out = conn.getOutputStream()) {
                out.write(tb.finish());
                out.close();
            }

            return new IOStream(conn.getInputStream()).read();
        } catch(final IOException ex) {
            final String msg = Token.string(new IOStream(conn.getErrorStream()).read());
            throw BaseXQueryException.get(msg);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Encodes entities in a string.
     * @param string input string
     * @return resulting string
     */
    protected static String toEntities(final String string) {
        return string.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").
                replace("<", "&lt;").replace(">", "&gt;");
    }
}