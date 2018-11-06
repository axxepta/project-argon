package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.BaseXSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static org.junit.Assert.*;

public class CustomProtocolURLUtilsTest {

    static final String ARGON = "argon";

    @BeforeClass
    public static void setUp() {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (ARGON.equals(protocol)) {
                    return new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL url) throws IOException {
                            return new URLConnection(url) {
                                @Override
                                public void connect() throws IOException {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }
                    };
                }
                return null;
            }
        });
    }

    @Test
    public void pathFromURL() throws Exception {
        assertEquals("foo", CustomProtocolURLUtils.pathFromURL(new URL(ARGON + ":foo")));
        assertEquals("foo/bar", CustomProtocolURLUtils.pathFromURL(new URL(ARGON + ":foo/bar")));
        assertEquals("foo/bar", CustomProtocolURLUtils.pathFromURL(new URL(ARGON + ":/foo/bar")));
    }

    @Test
    public void pathFromURLString() throws Exception {
        assertEquals("foo", CustomProtocolURLUtils.pathFromURLString(ARGON + ":foo"));
        assertEquals("foo/bar", CustomProtocolURLUtils.pathFromURLString(ARGON + ":foo/bar"));
        assertEquals("foo/bar", CustomProtocolURLUtils.pathFromURLString(ARGON + ":/foo/bar"));
        assertEquals("", CustomProtocolURLUtils.pathFromURLString("/foo/bar"));
        assertEquals("", CustomProtocolURLUtils.pathFromURLString("foo/bar"));
    }

    @Test
    public void sourceFromURL() throws Exception {
        assertEquals(BaseXSource.DATABASE, CustomProtocolURLUtils.sourceFromURL(new URL(ARGON + ":foo/bar")));
        assertEquals(null, CustomProtocolURLUtils.sourceFromURL(new URL( "http://foo/bar")));
    }

    @Test
    public void sourceFromURLString() throws Exception {
        assertEquals(BaseXSource.DATABASE, CustomProtocolURLUtils.sourceFromURLString(ARGON + ":foo/bar"));
        assertEquals(null, CustomProtocolURLUtils.sourceFromURLString( "http://foo/bar"));
        assertEquals(null, CustomProtocolURLUtils.sourceFromURLString("foo/bar"));
    }

}