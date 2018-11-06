package de.axxepta.oxygen.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class Resource {

    public final String name;
    public final BaseXType type;
    public final boolean locked;
    public final List<Resource> children;

    public Resource(@JsonProperty("name") String name,
                    @JsonProperty("type") BaseXType type,
                    @JsonProperty("locked") boolean locked,
                    @JsonProperty("children") List<Resource> children) {
        this.name = name;
        this.type = type;
        if (type == BaseXType.DIRECTORY) {
            this.locked = false;
            this.children = children;
        } else {
            this.locked = locked;
            this.children = Collections.emptyList();
        }
    }
}
