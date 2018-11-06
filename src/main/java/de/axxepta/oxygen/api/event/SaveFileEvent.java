package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class SaveFileEvent extends MsgTopic {
    public SaveFileEvent() {
        super("SAVE_FILE");
    }
}
