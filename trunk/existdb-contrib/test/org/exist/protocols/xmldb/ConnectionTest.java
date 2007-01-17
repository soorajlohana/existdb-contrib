/*
 * ConnectionTest.java
 * JUnit based test
 *
 * Created on December 22, 2006, 9:47 PM
 */

package org.exist.protocols.xmldb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author wessels
 */
public class ConnectionTest extends TestCase {
    
    private static boolean firstTime=true;
    
    public ConnectionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            BasicConfigurator.configure();
            System.setProperty( "java.protocol.handler.pkgs", "org.exist.protocols" );
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of connect method, of class org.exist.protocols.xmldb.Connection.
     */
    public void testGet1() {
        System.out.println("testGet1");
        try {
            // TODO add your test code.
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/macbeth.xml");
            InputStream is = url.openStream();
            
            copyDocument(is, System.out);
            
            is.close();
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        }
    }
    
    public void testGet2() {
        System.out.println("testGet2");
        try {
            // TODO add your test code.
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/foobar/macbeth.xml");
            InputStream is = url.openStream();
            
            copyDocument(is, System.out);

            is.close();
            fail("Document should not exist");
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            //fail(ex.toString());
            // Expected TODO more acurate check
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of getInputStream method, of class org.exist.protocols.xmldb.Connection.
     */
    public void testPut() throws Exception {
        System.out.println("testPut");
        // TODO add your test code.
        try {
            // TODO add your test code.
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/build.xml");
            OutputStream os = url.openConnection().getOutputStream();
            
            FileInputStream is = new FileInputStream("build.xml");
            copyDocument(is, os);
            
            is.close();
            os.close();
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        }
    }
    
    private void copyDocument(InputStream is, OutputStream os) throws IOException{
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        
        // Shutdown
        os.flush();
        
    }
    
    
    
}
