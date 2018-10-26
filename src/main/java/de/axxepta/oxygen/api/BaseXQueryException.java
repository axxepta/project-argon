package de.axxepta.oxygen.api;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BaseX resource.
 *
 * @author Christian Gruen, BaseX GmbH 2015, BSD License
 */
public final class BaseXQueryException extends IOException {
    /**
     * Error message pattern.
     */
    private static final Pattern MESSAGE = Pattern.compile(
            "^.* (.*), (\\d+)/(\\d+):\r?\n\\[(.*?)] (.*)$", Pattern.DOTALL);

    /**
     * File.
     */
    private final String file;
    /**
     * Line.
     */
    private final int line;
    /**
     * Column.
     */
    private final int column;
    /**
     * Error code.
     */
    private final String code;
    /**
     * Info string.
     */
    private final String info;

    /**
     * Returns an exception. Tries to convert the given exception to a query exception,
     * or returns the original exception.
     *
     * @param ex exception
     * @return new exception
     */
    public static IOException get(final IOException ex) {
        final String message = ex.getMessage();
        final Matcher matcher = MESSAGE.matcher(message);
        return matcher.find() ? new BaseXQueryException(message, matcher) : ex;
    }

    /**
     * Returns an exception. Tries to convert the given exception to a query exception,
     * or returns an I/O exception.
     *
     * @param message message
     * @return new exception
     */
    public static IOException get(final String message) {
        final Matcher matcher = MESSAGE.matcher(message);
        return matcher.find() ? new BaseXQueryException(message, matcher) : new IOException(message);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param matcher regular expression matcher
     */
    private BaseXQueryException(final String message, final Matcher matcher) {
        super(message);
        file = matcher.group(1);
        line = Integer.parseInt(matcher.group(2));
        column = Integer.parseInt(matcher.group(3));
        code = matcher.group(4);
        info = matcher.group(5);
    }

    /**
     * Returns the line.
     *
     * @return line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column.
     *
     * @return column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the file.
     *
     * @return file
     */
    public String getFile() {
        return file;
    }

    /**
     * Returns the error code.
     *
     * @return error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Return error info.
     *
     * @return the error info
     */
    public String getInfo() {
        return info;
    }
}