package de.axxepta.oxygen.versioncontrol;

import java.net.URL;
import java.util.Date;

/**
 * @author Markus on 01.02.2016.
 */
public class VersionHistoryEntry {
    private URL url;
    private int version;
    private int revision;
    private Date changeDate;

    public VersionHistoryEntry(URL url, int version, int revision, Date changeDate) {
        this.url = url;
        this.version = version;
        this.revision = revision;
        this.changeDate = changeDate;
    }

    protected Object[] getDisplayVector() {
        Object[] displayVector = {version, revision, changeDate};
        return displayVector;
    }

    protected URL getURL() {
        return url;
    }

}
