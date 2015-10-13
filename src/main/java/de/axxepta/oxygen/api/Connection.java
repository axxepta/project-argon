package de.axxepta.oxygen.api;

import java.io.*;

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
    BaseXResource[] list(final BaseXSource source, final String path) throws IOException;

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
     * @throws IOException I/O exception
     */
    void put(final BaseXSource source, final String path, final byte[] resource) throws IOException;

    /**
     * Deletes a resource.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void delete(final BaseXSource source, final String path) throws IOException;

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
     * Checks if the specified resource is locked.
     * @param source data source
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean locked(final BaseXSource source, final String path) throws IOException;

    /**
     * Returns active users (i.e., users who have currently locked any files).
     * @return users
     * @throws IOException I/O exception
     */
    String[] users() throws IOException;
}