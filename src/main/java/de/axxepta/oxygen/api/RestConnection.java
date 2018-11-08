package de.axxepta.oxygen.api;

import de.axxepta.oxygen.customprotocol.CustomProtocolURLHandlerExtension;
import de.axxepta.oxygen.versioncontrol.VersionHistoryEntry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basex.io.IOStream;
import org.basex.util.Base64;
import org.basex.util.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.basex.util.http.HttpMethod;

import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.*;
import java.util.*;

import static de.axxepta.oxygen.api.ConnectionUtils.*;
import static org.basex.util.http.HttpMethod.GET;

/**
 * BaseX REST implementation for the Argon connection interface.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public class RestConnection implements Connection {

    private static final Logger logger = LogManager.getLogger(RestConnection.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.findAndRegisterModules();
    }
    /**
     * URI.
     */
    protected final String url;
    protected final URI uri;
    protected final String basicAuth;
    private final CustomProtocolURLHandlerExtension lockCache = new CustomProtocolURLHandlerExtension();

    /**
     * Constructor.
     *
     * @param server   server name
     * @param port     connection port
     * @param user     user string
     * @param password password string
     */
    public RestConnection(final String server, final int port, final String user,
                          final String password) throws URISyntaxException {

        basicAuth = "Basic " + Base64.encode(user + ':' + password);
        //TODO: scheme should not be assumed http only
        //url = "http://" + user + ":" + password + "@" + server;
        url = "http://" + server + ":" + port + "/rest";

        uri = new URIBuilder(url).build();
    }

    @Override
    public void close() throws IOException {
        // nothing to do (connections are not kept open)
    }

    @Override
    public List<BaseXResource> list(final BaseXSource source, final String path) throws IOException {

        logger.debug("list " + source + " [" +  path + "]");

        //final Resource resource = getResourceMetadata(path);
        //final List<Resource> list = resource.children;
        final String result = Token.string(request(getQuery("list-" + source), PATH, path));

        logger.debug("result " + result);

        final ArrayList<BaseXResource> list = new ArrayList<>();
        if (!result.isEmpty()) {
            final String[] results = result.split("\r?\n");
            for (int r = 0, rl = results.length; r < rl; r += 2) {
                list.add(new BaseXResource(results[r + 1], BaseXType.valueOf(results[r]), source));
            }
        }
        return list;
    }

    private Resource getResourceMetadata(String path) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final byte[] request = request("list/" + path);
        return mapper.readValue(request, Resource.class);
    }

    @Override
    public List<BaseXResource> listAll(final BaseXSource source, final String path) throws IOException {

        logger.info("listAll " + source + " [" +  path + "]");
        final String result = Token.string(request(getQuery("listall-" + source), PATH, path));
        final ArrayList<BaseXResource> list = new ArrayList<>();
        if (!result.isEmpty()) {
            final String[] results = result.split("\r?\n");
            for (int r = 0, rl = results.length; r < rl; r += 2) {
                list.add(new BaseXResource(results[r + 1], BaseXType.valueOf(results[r]), source));
            }
        }
        return list;
    }

    @Override
    public void init() throws IOException {
        logger.info("init");
//        request(getQuery("init"), RESOURCE, prepare(getAPIResource(ArgonConst.META_TEMPLATE).getBytes("UTF-8"), false));
    }

    @Override
    public void create(final String database, final String chop, final String ftindex, final String textindex,
                       final String attrindex, final String tokenindex) throws IOException {
        request(getQuery("create-database"), DATABASE, database, CHOP, chop, FTINDEX, ftindex, TEXTINDEX, textindex,
                ATTRINDEX, attrindex, TOKENINDEX, tokenindex);
    }

    @Override
    public void drop(String database) throws IOException {
        request(getQuery("drop-database"), DATABASE, database);
    }

    @Override
    public byte[] get(final BaseXSource source, final String inPath, boolean export) throws IOException {
        final String path = inPath.startsWith("/") ? inPath.substring(1) : inPath;
//        return request(getQuery("get-" + source), PATH, path);
        return request("file/" + path);
    }

    @Override
    public void put(final BaseXSource source, final String inPath, final byte[] resource, boolean binary, String encoding,
                    String owner, String versionize, String versionUp)
            throws IOException {
        final String path = inPath.startsWith("/") ? inPath.substring(1) : inPath;
//        request(getQuery("put-" + source),
//                PATH, path,
//                RESOURCE, prepare(resource, binary),
//                BINARY, Boolean.toString(binary),
//                ENCODING, encoding,
//                OWNER, owner,
//                VERSIONIZE, versionize,
//                VERSION_UP, versionUp);
        final String action = "file";
        final URI uri = addParameter(this.uri.resolve(action + "/" + path), "version=" + versionUp);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpEntity entity = new ByteArrayEntity(resource);
            final HttpPut putRequest = new HttpPut(uri);
            putRequest.setEntity(entity);
            logger.info("Update " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 || statusCode == 204) {
                logger.info("Update successful");
            } else {
                logger.info("Update failed");
                readError(response);
            }
        }
    }

    private URI addParameter(final URI base, final String query) {
        try {
            return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath(),
                    query, base.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void readError(HttpResponse response) throws IOException {
//        try (InputStream error = response.getEntity().getContent()) {
//            final ObjectMapper mapper = new ObjectMapper();
//            mapper.readValue(error, Map.class);
//            throw new IOException("Got " + response.getStatusLine().getStatusCode());
//        }
        try (BufferedReader error = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            error.lines().forEach(logger::error);
        }
    }

    @Override
    public void newDir(final BaseXSource source, final String path) throws IOException {
        if (!source.equals(BaseXSource.DATABASE))
            request(getQuery("newdir-" + source), PATH, path);
    }

    @Override
    public void delete(final BaseXSource source, final String path) throws IOException {
        request(getQuery("delete-" + source), PATH, path);
        final String action = "file";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpDelete putRequest = new HttpDelete(uri.resolve(action + "/" + path));
            logger.info("Delete " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 || statusCode == 202 || statusCode == 204) {
                logger.info("Delete successful");
            } else {
                logger.info("Delete failed");
                readError(response);
            }
        }
    }

    @Override
    public boolean exists(final BaseXSource source, final String path) throws IOException {
//        final byte[] result = request(getQuery("exists-" + source), PATH, path);
//        return Token.string(result).equals("true");
        final String action = "list";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet putRequest = new HttpGet(uri.resolve(action + "/" + path));
            logger.info("Check existence " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() == 200) { // 204
                logger.info("Found metadata");
                return true;
            } else if (response.getStatusLine().getStatusCode() == 404) {
                return false;
            } else {
                logger.info("Checking existence failed");
                readError(response);
                return false;
            }
        }
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
    public String xquery(final String query, final String... args) throws IOException {
        throw new UnsupportedOperationException();
//        try {
//            return Token.string(request(query, args));
//        } catch (final IOException ex) {
//            throw BaseXQueryException.get(ex);
//        }
    }

    @Override
    public List<VersionHistoryEntry> getHistory(final String path) throws IOException {
        final String action = "history";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet putRequest = new HttpGet(uri.resolve(action + "/" + path));
            final HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() == 200) { // 204
                return Arrays.asList(mapper.readValue(response.getEntity().getContent(), VersionHistoryEntry[].class));
            } else if (response.getStatusLine().getStatusCode() == 404) {
                return Collections.emptyList();
            } else {
                readError(response);
                return Collections.emptyList();
            }
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
        final String action = "lock";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPut putRequest = new HttpPut(uri.resolve(action + "/" + path));
            logger.info("Lock " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() == 200) { // 204
                logger.info("Locking successful");
                lockCache.lock(new URL(source.getProtocol() + ":" + path));
            } else {
                logger.info("Locking failed");
                readError(response);
            }
//            request("lock/" + path, HttpMethod.PUT);
        }
    }

    @Override
    public void unlock(final BaseXSource source, final String path) throws IOException {
//        request(getQuery("unlock"), SOURCE, source.toString(), PATH, path);
        final String action = "lock";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpDelete putRequest = new HttpDelete(uri.resolve(action + "/" + path));
            logger.info("Unlock " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() == 200) { // 204
                logger.info("Unlocking successful");
                lockCache.unlock(new URL(source.getProtocol() + ":" + path));
            } else {
                logger.info("Unlocking failed");
                readError(response);
            }
        }
//        request("lock/" + path, HttpMethod.DELETE);
    }

    @Override
    public boolean locked(final BaseXSource source, final String path) throws IOException {
//        final ObjectMapper mapper = new ObjectMapper();
//        final byte[] request = request("list/" + path);
//        return mapper.readValue(request, Resource.class).locked;
        return lockedByUser(source, path);
    }

    @Override
    public boolean lockedByUser(final BaseXSource source, final String path) throws IOException {
//        final byte[] result = request(getQuery("lockedByUser"), SOURCE, source.toString(), PATH, path);
//        return Token.string(result).equals("true");
        final String action = "lock";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet putRequest = new HttpGet(uri.resolve(action + "/" + path));
            logger.info("Check lock " + putRequest.getURI());
            final HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() == 200) { // 204
                logger.info("Found lock");
                // FIXME this really should get info who has the lock
//                try (InputStream error = entity.getContent()) {
//                    final ObjectMapper mapper = new ObjectMapper();
//                    mapper.readValue(error, Map.class);
//                    throw new IOException("Got " + response.getStatusLine().getStatusCode());
//                }
                return true;
            } else if (response.getStatusLine().getStatusCode() == 404) {
                return false;
            } else {
                logger.info("Checking lock failed");
                readError(response);
                return true;
            }
        }
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
     *
     * @param query name of the query
     * @param bindings keys and values
     * @return string result, or {@code null} for a failure.
     * @throws IOException I/O exception
     */
    protected byte[] request(final String query, final String... bindings) throws IOException {

        URIBuilder ub = null;
        URI queryUri = null;
        HttpURLConnection conn = null;

        try {
            ub = new URIBuilder(url);

            ub.addParameter("run", query);
            ub.addParameter("method","text");

            for (int b = 0, bl = bindings.length; b < bl; b += 2) {
                ub.addParameter(bindings[b], bindings[b + 1]);
            }

            queryUri = ub.build();

        }catch(URISyntaxException ex) {
            logger.error("Connection failed to set uri: ", ex.getMessage());
            throw BaseXQueryException.get(ex.getMessage());
        }

        logger.debug("uri: " + queryUri.toString());


        try {
            conn = ConnectionUtils.getConnection(queryUri.toURL());
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod(GET.name());

            return new IOStream(conn.getInputStream()).read();
        } catch (final IOException ex) {
            logger.error("Connection failed to set query: ", ex.getMessage());
            final String msg = Token.string(new IOStream(conn.getErrorStream()).read());
            throw BaseXQueryException.get(msg);
        } finally {
            conn.disconnect();
        }
    }

    //  conn.setRequestMethod(POST.name());
//            conn.setRequestProperty(HttpText.CONTENT_TYPE, MediaType.APPLICATION_XML.toString());

    // build and send query
//            final TokenBuilder tb = new TokenBuilder();
//            tb.add("<query xmlns='http://basex.org/rest'>\n");
//            tb.add("<text>").add(toEntities(body)).add("</text>\n");
//            for (int b = 0, bl = bindings.length; b < bl; b += 2) {
//                tb.add("<variable name='").add(bindings[b]).add("' value='");
//                tb.add(toEntities(bindings[b + 1])).add("'/>\n");
//            }
//            tb.add("</query>");
//            try (final OutputStream out = conn.getOutputStream()) {
//                out.write(tb.finish());
//                out.close();
//            }


    /**
     * Executes the specified HTTP request and returns the result.
     *
     * @return string result, or {@code null} for a failure.
     * @throws IOException I/O exception
     */
    protected byte[] request(final String path) throws IOException {
        return request(path, GET);
    }

    /**
     * Executes the specified HTTP request and returns the result.
     *
     * @return string result, or {@code null} for a failure.
     * @throws IOException I/O exception
     */
    protected byte[] request(final String path, final HttpMethod method) throws IOException {

        logger.debug(method.name() + " " + uri.resolve(path).toURL());

        HttpURLConnection conn = null;
        try {
            conn = ConnectionUtils.getConnection(uri.resolve(path).toURL());
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod(method.name());
            return new IOStream(conn.getInputStream()).read();
        } catch (final IOException ex) {
            logger.error("Connection failed to set query: ", ex.getMessage());
            final String msg = Token.string(new IOStream(conn.getErrorStream()).read());
            throw BaseXQueryException.get(msg);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Encodes entities in a string.
     *
     * @param string input string
     * @return resulting string
     */
    protected static String toEntities(final String string) {
        return string.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").
                replace("<", "&lt;").replace(">", "&gt;");
    }

}