package de.axxepta.oxygen.api;

import org.basex.util.Token;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Markus on 27.04.2016.
 */
public interface ArgonWordConnection {

    /**
     * Returns recursively all resources of the given path.
     * @param path path
     * @return entries
     * @throws IOException I/O exception
     */
    BaseXResource[] list(final String path) throws IOException;

    /**
     * Creates a new database.
     * @param database new database name
     * @param chop chop option as string
     * @param ftindex ftindex option as string
     * @throws IOException I/O exception
     */
    void create(final String database, final String chop, final String ftindex) throws IOException;

    /**
     * Returns a resource in its binary representation.
     * Texts are encoded as UTF-8 and can be converted via {@link Token#string(byte[])}.
     * @param path path
     * @return entry
     * @throws IOException I/O exception
     */
    byte[] get(final String path) throws IOException;

    /**
     * Stores a resource.
     * Textual resources must be encoded to UTF-8 via {@link Token#token(String)}.
     * @param path path
     * @param resource resource to be stored
     * @param saveOriginalFormat store docx as is or convert to Dita
     * @throws IOException I/O exception
     */
    void put(final String path, final byte[] resource, final boolean saveOriginalFormat) throws IOException;

    /**
     * Deletes a resource.
     * @param path path
     * @throws IOException I/O exception
     */
    void delete(final String path) throws IOException;

    /**
     * Renames a resource.
     * @param path path
     * @param newPath new path
     * @throws IOException I/O exception
     */
    void rename(final String path, final String newPath) throws IOException;

    /**
     * Searches for resources containing a filter string in it's name.
     * @param path path
     * @param filter search filter
     * @return resources
     * @throws IOException I/O exception
     */
    ArrayList<String> search(final String path, final String filter) throws IOException;

    /**
     * Locks a resource.
     * @param source data source
     * @param path path
     * @throws IOException I/O exception
     */
    void lock(final BaseXSource source, final String path) throws IOException;

    /**
     * Unlocks a resource.
     * @param path path
     * @throws IOException I/O exception
     */
    void unlock(final String path) throws IOException;

    /**
     * Checks if the specified resource is locked for asking user.
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean locked(final String path) throws IOException;

    /**
     * Checks if the specified resource has no locking information at all
     * @param path path
     * @return result of check
     * @throws IOException I/O exception
     */
    boolean noLockSet(final String path) throws IOException;

}
