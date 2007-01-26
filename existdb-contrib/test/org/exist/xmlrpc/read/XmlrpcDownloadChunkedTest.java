/*
 * XmlrpcDownloadChunkedTest.java
 * JUnit based test
 *
 * Created on January 26, 2007, 9:12 PM
 */

package org.exist.xmlrpc.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.xmlrpc.read.XmlrpcDownloadChunked;

/**
 *
 * @author wessels
 */
public class XmlrpcDownloadChunkedTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcDownloadChunkedTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcDownloadChunkedTest(String testName) {
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
    }
    
    /**
     * Test download of file.
     */
    public void testDownloadFile1() {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownloadChunked xdc = new XmlrpcDownloadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test download of file from not existing collection.
     */
    public void testDownloadFile2() {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownloadChunked xdc = new XmlrpcDownloadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("Collection /db/foobar not found!")){
                fail(ex.getMessage());
            };
        }

    }
    
    /**
     * Test download of file as non existing user.
     */
    public void testDownloadFile3() {
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownloadChunked xdc = new XmlrpcDownloadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("User foo unknown")){
                fail(ex.getMessage());
            }
        }
    }
    
    /**
     * Test download of file to a forbidden collection
     */
    public void testDownloadFile4() {
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/users.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownloadChunked xdc = new XmlrpcDownloadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("Insufficient privileges to read resource")){
                fail(ex.getMessage());
            }
        }
    }
    
}
