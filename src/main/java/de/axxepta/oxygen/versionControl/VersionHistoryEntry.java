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

    VersionHistoryEntry(URL url, int version, int revision, Date changeDate) {
        this.url = url;
        this.version = version;
        this.revision = revision;
        this.changeDate = changeDate;
    }

    Object[] getDisplayVector() {
        return new Object[] {version, revision, changeDate};
    }

    protected URL getURL() {
        return url;
    }

}
