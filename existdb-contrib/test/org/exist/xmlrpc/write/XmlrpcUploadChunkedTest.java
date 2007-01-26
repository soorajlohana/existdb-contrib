/*
 * XmlrpcUploadChunkedTest.java
 * JUnit based test
 *
 * Created on January 26, 2007, 8:55 PM
 */

package org.exist.xmlrpc.write;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.xmlrpc.write.XmlrpcUploadChunked;

/**
 *  jUnit tests for XmlrpcUploadChunked class
 * .
 * @author Dannes Wessels
 */
public class XmlrpcUploadChunkedTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcUploadChunkedTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcUploadChunkedTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            BasicConfigurator.configure();
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        // -/-
    }
    
    /**
     * Test upload of file
     */
    public void testUploadFile1()  {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUploadChunked xuc = new XmlrpcUploadChunked();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test upload of file to non existing collection
     */
    public void testUploadFile2() {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUploadChunked xuc = new XmlrpcUploadChunked();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to non existing collection must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            
            if(!ex.getMessage().contains("Collection /db/foobar not found")){
                fail(ex.getMessage());
            }
            
        }
    }
    
    
    /**
     * Test upload of file as non existing user
     */
    public void testUploadFile3() {
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUploadChunked xuc = new XmlrpcUploadChunked();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload as non existing user must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("User foo unknown")){
                fail(ex.getMessage());
            }
        }
    }
    
    /**
     * Test upload of file to a forbidden collection
     */
    public void testUploadFile4() {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUploadChunked xuc = new XmlrpcUploadChunked();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to collection /db/system/ must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("User 'guest' not allowed to write to collection '/db/system'")){
                fail(ex.getMessage());
            }
        }
    }
}
