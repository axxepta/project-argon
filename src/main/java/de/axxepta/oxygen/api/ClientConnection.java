package de.axxepta.oxygen.api;

import static de.axxepta.oxygen.api.ConnectionUtils.*;

import java.io.*;
import java.util.*;

import de.axxepta.oxygen.api.BaseXClient.*;

/**
 * BaseX client implementation for the Argon connection interface.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public final class ClientConnection implements Connection {
    /** Connection instance. */
    private final BaseXClient client;

    /**
     * Constructor.
     * @param server server name
     * @param port connection port
     * @param user user string
     * @param password password string
     * @throws IOException I/O exception
     */
    public ClientConnection(final String server, final int port, final String user,
                            final String password) throws IOException {
        client = new BaseXClient(server, port, user, password);
    }

    @Override
    public BaseXResource[] list(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("list-" + source));
        query.bind(PATH, path, "");

        final ArrayList<BaseXResource> list = new ArrayList<>();
        while(query.more()) {
            final String type = query.next(), name = query.next();
            list.add(new BaseXResource(name, BaseXType.get(type), source));
        }
        return list.toArray(new BaseXResource[list.size()]);
    }

    @Override
    public void init() throws IOException {
        final Query query = client.query(getQuery("init"));
        byte[] resource = getAPIResource("MetaTemplate.xml").getBytes("UTF-8");
        query.bind(RESOURCE, prepare(resource), "");
        query.execute();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public void create(final String database, final String chop, final String ftindex) throws IOException {
        final Query query = client.query(getQuery("create-database"));
        query.bind(DATABASE, database, "");
        query.execute();
    }

    @Override
    public byte[] get(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("get-" + source));
        query.bind(PATH, path, "");
        return query.binary();
    }

    @Override
    public void put(final BaseXSource source, final String path, final byte[] resource)
            throws IOException {

        final Query query = client.query(getQuery("put-" + source));
        query.bind(PATH, path, "");
        query.bind(RESOURCE, prepare(resource), "");
        query.execute();
    }

    @Override
    public void delete(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("delete-" + source));
        query.bind(PATH, path, "");
        query.execute();
    }

    @Override
    public void rename(final BaseXSource source, final String path, final String newPath) throws IOException {
        final Query query = client.query(getQuery("rename-" + source));
        query.bind(PATH, path, "");
        query.bind(NEWPATH, newPath, "");
        query.execute();
    }

    @Override
    public ArrayList<String> search(final BaseXSource source, final String path, final String filter) throws IOException {
        final ArrayList<String> list = new ArrayList<>();
        final Query query = client.query(getQuery("search-" + source));
        query.bind(PATH, path, "");
        query.bind(FILTER, filter, "");
        while(query.more()) list.add(query.next());
        return list;
    }

    @Override
    public String xquery(final String query) throws IOException {
        final Query qu = client.query(query);
        try {
            return qu.execute();
        } catch(final IOException ex) {
            throw BaseXQueryException.get(ex);
        }
    }

    @Override
    public void parse(final String xquery) throws IOException {
        final Query query = client.query(getQuery("parse"));
        query.bind(XQUERY, xquery, "");
        try {
            query.execute();
        } catch(final IOException ex) {
            throw BaseXQueryException.get(ex);
        }
    }

    @Override
    public void parse(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("parse-" + source));
        query.bind(PATH, path, "");
        try {
            query.execute();
        } catch(final IOException ex) {
            throw BaseXQueryException.get(ex);
        }
    }

    @Override
    public void lock(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("lock"));
        query.bind(SOURCE, source.toString(), "");
        query.bind(PATH, path, "");
        query.execute();
    }

    @Override
    public void unlock(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("unlock"));
        query.bind(SOURCE, source.toString(), "");
        query.bind(PATH, path, "");
        query.execute();
    }

    @Override
    public boolean locked(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("locked"));
        query.bind(SOURCE, source.toString(), "");
        query.bind(PATH, path, "");
        return query.execute().equals("true");
    }

    @Override
         public boolean lockedByUser(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("lockedByUser"));
        query.bind(SOURCE, source.toString(), "");
        query.bind(PATH, path, "");
        return query.execute().equals("true");
    }

    @Override
    public boolean noLockSet(final BaseXSource source, final String path) throws IOException {
        final Query query = client.query(getQuery("no-lock-set"));
        query.bind(SOURCE, source.toString(), "");
        query.bind(PATH, path, "");
        return query.execute().equals("true");
    }

    @Override
    public String[] users() throws IOException {
        final ArrayList<String> list = new ArrayList<>();
        final Query query = client.query(getQuery("users"));
        while(query.more()) list.add(query.next());
        return list.toArray(new String[list.size()]);
    }
}