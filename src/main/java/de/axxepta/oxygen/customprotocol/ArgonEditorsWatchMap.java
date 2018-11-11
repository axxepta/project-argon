package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.MsgTopic;
import de.axxepta.oxygen.api.TopicHolder;
import de.axxepta.oxygen.api.event.ListDirEvent;
import de.axxepta.oxygen.core.ObserverInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.axxepta.oxygen.api.TopicHolder.listDir;

/**
 * @author Markus on 28.10.2015.
 * The enwraped map contains information about which Argon URLs are opened in editors
 */
public class ArgonEditorsWatchMap implements ObserverInterface<MsgTopic> {

    private static final ArgonEditorsWatchMap instance = new ArgonEditorsWatchMap();

    private final Map<URL, EditorInfo> editorMap = new HashMap<>();
    /**
     * contains all Argon resources with locks, locks owned by current user marked with true value
     */
    private final Map<URL, Boolean> lockMap = new HashMap<>();


    private ArgonEditorsWatchMap() {
    }

    public static ArgonEditorsWatchMap getInstance() {
        return instance;
    }

    public void init() {
        TopicHolder.newDir.register(this);
        update(listDir, "");
    }

    private boolean isLockInMap(URL url) {
        return !(lockMap.get(url) == null);
    }

    public boolean hasOwnLock(URL url) {
        return isLockInMap(url) && lockMap.get(url);
    }

    public boolean hasOtherLock(URL url) {
        return isLockInMap(url) && !lockMap.get(url);
    }

    public void addURL(URL url) {
        if (!isURLInMap(url)) {
            editorMap.put(url, new EditorInfo(false));
        }
    }

    public void addURL(URL url, boolean checkedOut) {
        if (isURLInMap(url)) {
            editorMap.get(url).setCheckedOut(checkedOut);
        } else {
            editorMap.put(url, new EditorInfo(checkedOut));
        }
        if (checkedOut) {
            lockMap.put(url, true);
        }
    }

    public void removeURL(URL url) {
        editorMap.remove(url);
        if (isLockInMap(url) && lockMap.get(url)) {
            lockMap.remove(url);
        }
    }

    private boolean isURLInMap(URL url) {
        return !(editorMap.get(url) == null);
    }

    public String getEncoding(URL url) {
        if (isURLInMap(url)) {
            return "UTF-8";
        } else {
            return "";
        }
    }

    public boolean askedForCheckIn(URL url) {
        if (isURLInMap(url)) {
            return editorMap.get(url).isAskedForCheckIn();
        } else {
            return true;
        }
    }

    public void setAskedForCheckIn(URL url, boolean asked) {
        if (isURLInMap(url)) {
            editorMap.get(url).setAskedForCheckIn(asked);
        }
    }

    @Override
    public void update(MsgTopic type, Object... message) {
        //
    }


    private static class EditorInfo {

        boolean checkedOut;
        boolean askedForCheckIn = false;

        EditorInfo(boolean checkedOut) {
            this.checkedOut = checkedOut;
        }

        boolean isAskedForCheckIn() {
            return askedForCheckIn;
        }

        void setAskedForCheckIn(boolean askedForCheckIn) {
            this.askedForCheckIn = askedForCheckIn;
        }

        void setCheckedOut(boolean checkedOut) {
            this.checkedOut = checkedOut;
        }
    }

}
