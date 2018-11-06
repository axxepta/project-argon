package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class TemplateUpdateRequestedEvent extends MsgTopic {
    public TemplateUpdateRequestedEvent() {
        super("TEMPLATE_UPDATE_REQUESTED");
    }
}
