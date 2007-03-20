/*
 * XmlrpcURLsTest.java
 * JUnit based test
 *
 * Created on February 26, 2007, 9:20 PM
 */

package org.exist.protocolhandler.xmlrpc;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 * @author Dannes Wessels
 */
public class XmlrpcURLsTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcURLsTest.class);
    private static boolean firstTime=true;
    
    
    public XmlrpcURLsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            PropertyConfigurator.configure("log4j.conf");
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    private boolean sendToURL(String URL, String file) throws Exception {
        
        boolean retVal=false;
        
        try {
            URL url = new URL(URL);
            InputStream is = new BufferedInputStream( new FileInputStream(file) );
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is,os);
            is.close();
//**            os.flush();
            os.close();
            
            retVal=true; // no problems!
            
        } catch (Exception ex) {
            throw ex;
        }
        
        return retVal;  // COF: will never return false - makes no sense!
    }
    
    private boolean getFromURL(String URL, OutputStream os) throws Exception {
        
        boolean retVal=false;
        
        try {
            URL url = new URL(URL);
            InputStream is = url.openConnection().getInputStream();
            copyDocument(is,os);
            
            is.close();
//**            os.flush();
            os.close();
            
            retVal=true; // no problems!
            
        } catch (Exception ex) {
            throw ex;
            
        }
        
        return retVal;  // COF: will never return false - makes no sense!
    }
    
    // Transfer bytes from inputstream to outputstream
    private void copyDocument(InputStream is, OutputStream os) throws IOException{
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        os.flush();
    }
    
    // =====================================
     // TODO xmldb:exist://localhost:8080/db/build_testURLToDB.xml ?
    public void testURLToDB() {
        System.out.println("testURLToDB");
        
        try {
            boolean retVal = sendToURL(
                    "xmldb:exist://localhost:8080/exist/xmlrpc/db/build_testURLToDB.xml",
                    "build.xml" );
            
            assertTrue(retVal);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testURLFromDB() {
        System.out.println("testURLFromDB");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/build_testURLToDB.xml", os);
//**            os.flush();
//**            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testURLToDB_notExistingCollection() {
        System.out.println("testURLToDB_notExistingCollection");
        try {
            boolean retVal = sendToURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/foo/bar.xml",
                    "build.xml");
            
            fail("Not existing collection: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foo not found.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLFromDB_notExistingCollection() {
        System.out.println("testURLFromDB_notExistingCollection");
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/foo/bar.xml", os);
//**            os.flush();
//**            os.close();
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foo not found.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLToDB_NotExistingUser() {
        System.out.println("testURLToDB_NotExistingUser");
        try {
            sendToURL("xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/testURLToDB_NotExistingUser.xml",
                    "build.xml");
            
            fail("Not existing user: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLFromDB_NotExistingUser() {
        System.out.println("testURLFromDB_NotExistingUser");
        
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/testURLFromDB_NotExistingUser.xml", os);
            
//**            os.flush();
//**            os.close();
            
            assertTrue(os.size()==0);
            
            fail("Not existing user: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User .* unknown.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    
}
