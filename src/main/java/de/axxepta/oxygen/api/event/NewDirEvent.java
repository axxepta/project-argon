package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class NewDirEvent extends MsgTopic {
    public NewDirEvent() {
        super("NEW_DIR");
    }
}
