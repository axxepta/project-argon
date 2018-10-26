package de.axxepta.oxygen.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Resource types.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public enum BaseXType {
    /**
     * Directory.
     */
    DIRECTORY,
    /**
     * Resource.
     */
    RESOURCE;

    @JsonCreator
    public static BaseXType deserialize(String name) {
        return BaseXType.valueOf(name);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

}