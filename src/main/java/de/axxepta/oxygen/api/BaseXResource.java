package de.axxepta.oxygen.api;

/**
 * BaseX resource.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public final class BaseXResource {
    /** Entry name. */
    public final String name;
    /** Entry type. */
    public final BaseXType type;
    /** Source. */
    public final BaseXSource source;

    /**
     * Constructor.
     * @param name name
     * @param type type
     * @param source source
     */
    public BaseXResource(final String name, final BaseXType type, final BaseXSource source) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}