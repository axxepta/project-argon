package de.axxepta.oxygen.api;

/**
 * @author Max on 01.09.2015.
 */
public class TopicHolder {

//    public static MsgTopic openFile = new MsgTopic("OPEN_FILE");
//    public static MsgTopic changeFile = new MsgTopic("CHANGE_FILE");
    public static MsgTopic saveFile = new MsgTopic("SAVE_FILE");
    public static MsgTopic deleteFile = new MsgTopic("DELETE_FILE");
    public static MsgTopic changedEditorStatus = new MsgTopic("EDITOR_STATUS_CHANGED");
    public static MsgTopic changedServerStatus = new MsgTopic("SERVER_STATUS_CHANGED");
    public static MsgTopic treeNodeRequested = new MsgTopic("REQUESTED_TREE_NODE");
    public static MsgTopic consoleNotified = new MsgTopic("MESSAGE_TO_CONSOLE");

}
