package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class ChangedServerStatusEvent extends MsgTopic {
    public ChangedServerStatusEvent() {
        super("SERVER_STATUS_CHANGED");
    }
}
