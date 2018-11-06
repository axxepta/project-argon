package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class DeleteFileEvent extends MsgTopic {
    public DeleteFileEvent() {
        super("DELETE_FILE");
    }
}
