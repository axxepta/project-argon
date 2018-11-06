package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class TreeNodeRequestedEvent extends MsgTopic {
    public TreeNodeRequestedEvent() {
        super("REQUESTED_TREE_NODE");
    }
}
