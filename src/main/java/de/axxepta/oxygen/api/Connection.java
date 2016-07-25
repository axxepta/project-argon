package de.axxepta.oxygen.api;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.basex.util.*;

/**
 * Connection API.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public interface Connection extends Closeable {
    /**
     * Returns resources of the given data source and path.
     * @param source data source
     * @param path path
     * @return entries
     * @throws IOException I/O exception
     */
    List<BaseXResource> list(final BaseXSource source, final String path) throws IOException;

    /**
     * Sets up a database for user management and copies a meta data template file into it.
     * @throws IOException I/O exception
     */
    void init() throws IOException;

    /**
     * Creates a new database.
     * @param database new database name
     * @param chop chop option as string
     * @param ftindex ftindex option as string
     * @throws IOException I/O exception
     */
    void create(final String database, final String chop, final String ftindex) throws IOException;

    /**
     * Creates a new database.
     * @param database new database name
     * @throws IOException I/O exception
     */
    void drop(final String database) throws IOException;

    /**
     * Returns a resource in its binary representation.
     * Texts are encoded as UTF-8 and can be converted via {@link Token#string(byte[])}.
     * @param source data source
     * @param path path
     * @return entry
     * @throws IOException I/O exception
     */
    byte[] get(final BaseXSource source, final String path) throws IOException;

    /**
     * Stores a resource.
     * Textual resources must be encoded to UTF-8 via {@link Token#token(String)}.
     * @param source data source
     * @param path path
     * @param resource resource to be stored
     * @param versionize flag whether version control should be used
     * @param versionUp flag whether version should be raised as String
     * @throws IOException I/O exception
     */
    void put(final BaseXSource source, final String path, final byte[] resource, String versionize, String versionUp) throws IOException;

    /**
     * Deletes a resource.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void delete(final BaseXSource source, final String path) throws IOException;

    /**
     * Renames a resource.
     * @param source data source
     * @param path path
     * @param newPath new path
     * @throws IOException I/O exception
     */
    void rename(final BaseXSource source, final String path, final String newPath) throws IOException;

    /**
     * Searches for resources containing a filter string in it's name.
     * @param source data source
     * @param path path
     * @param filter search filter
     * @return resources
     * @throws IOException I/O exception
     */
    ArrayList<String> search(final BaseXSource source, final String path, final String filter) throws IOException;

    /**
     * Evaluates a query.
     * @param query query to be evaluated
     * @return result (string representation)
     * @throws IOException I/O exception
     */
    String xquery(final String query) throws IOException;

    /**
     * Parses a query.
     * @param xquery query text
     * @throws IOException I/O exception
     */
    void parse(final String xquery) throws IOException;

    /**
     * Parses a query.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void parse(final BaseXSource source, final String path) throws IOException;

    /**
     * Locks a resource.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void lock(final BaseXSource source, final String path) throws IOException;

    /**
     * Unlocks a resource.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void unlock(final BaseXSource source, final String path) throws IOException;

    /**
     * Checks if the specified resource is locked for asking user.
     * @param source data source
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean locked(final BaseXSource source, final String path) throws IOException;

    /**
     * Checks if the specified resource is locked by asking user.
     * @param source data source
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean lockedByUser(final BaseXSource source, final String path) throws IOException;

    /**
     * Checks if the specified resource has no locking information at all
     * @param source data source
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean noLockSet(final BaseXSource source, final String path) throws IOException;

    /**
     * Returns active users (i.e., users who have currently locked any files).
     * @return users
     * @throws IOException I/O exception
     */
    String[] users() throws IOException;
}
