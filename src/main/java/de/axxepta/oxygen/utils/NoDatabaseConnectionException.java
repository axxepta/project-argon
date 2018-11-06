package de.axxepta.oxygen.utils;

import java.io.IOException;

public class NoDatabaseConnectionException extends IOException {

    public NoDatabaseConnectionException() {
        super("No database connection");
    }

}
