package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class ChangedEditorStatusEvent extends MsgTopic {
    public ChangedEditorStatusEvent() {
        super("EDITOR_STATUS_CHANGED");
    }
}
