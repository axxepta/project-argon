package de.axxepta.oxygen.api;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static org.junit.Assert.*;

public class ArgonRestConnectionTest {

    static RestConnection conn;

    @BeforeClass
    public static void setUp() throws Exception {

        conn = new RestConnection("localhost:8984/rest", 8984, "admin", "admin");
    }

    @Test
    public void createDatabase() throws Exception {

        conn.create("test-db","no", "no", "yes", "yes", "no");

    }

    /*
    @Test
    public void deleteDatabase() throws Exception {

        conn.delete(BaseXSource.DATABASE, "test-db");
    }
    */

    @AfterClass
    public static void TearDown() throws Exception{

        //conn.delete(BaseXSource.DATABASE, "test-db");


    }

}
