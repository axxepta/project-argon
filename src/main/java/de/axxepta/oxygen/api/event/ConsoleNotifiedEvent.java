package de.axxepta.oxygen.api.event;

import de.axxepta.oxygen.api.MsgTopic;

public class ConsoleNotifiedEvent extends MsgTopic {
    public ConsoleNotifiedEvent() {
        super("MESSAGE_TO_CONSOLE");
    }
}
