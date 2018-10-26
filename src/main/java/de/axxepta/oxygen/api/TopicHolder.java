package de.axxepta.oxygen.api;

import de.axxepta.oxygen.api.event.*;

/**
 * @author Max on 01.09.2015.
 */
public class TopicHolder {

    //    public static MsgTopic openFile = new openFileEvent();
//    public static MsgTopic changeFile = new changeFileEvent();
    public static final SaveFileEvent saveFile = new SaveFileEvent();
    public static final NewDirEvent newDir = new NewDirEvent();
    public static final DeleteFileEvent deleteFile = new DeleteFileEvent();
    public static final ChangedEditorStatusEvent changedEditorStatus = new ChangedEditorStatusEvent();
    public static final TemplateUpdateRequestedEvent templateUpdateRequested = new TemplateUpdateRequestedEvent();
    public static final ChangedServerStatusEvent changedServerStatus = new ChangedServerStatusEvent();
    public static final TreeNodeRequestedEvent treeNodeRequested = new TreeNodeRequestedEvent();
    public static final ConsoleNotifiedEvent consoleNotified = new ConsoleNotifiedEvent();
    public static final ListDirEvent listDir = new ListDirEvent();
}
