/*
 * XmlrpcOutputStreamTest.java
 * JUnit based test
 *
 * Created on December 17, 2006, 9:29 PM
 */

package org.exist.xmlrpc.write;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.exist.xmldb.XmldbURI;
import org.exist.xmlrpc.write.XmlrpcOutputStream;

/**
 *
 * @author wessels
 */
public class XmlrpcOutputStreamTest extends TestCase {
    
    private static boolean firstTime=true;
    
    public XmlrpcOutputStreamTest(String testName) {
        super(testName);
        
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            BasicConfigurator.configure();
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        //
    }
    
    public void testSendXmlDoc1() {
        System.out.println("testSendXmlDoc1");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/build.xml";
            XmldbURI xmldbUri = XmldbURI.create(uri);
            sendDocument(xmldbUri, fis);
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void testSendXmlDoc2() {
        System.out.println("testSendXmlDoc2");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/notexisting/build.xml";
            XmldbURI xmldbUri = XmldbURI.create(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
            fail("Expected exception");
        } catch (Exception ex) {
            System.out.println("Expected exception:");
            ex.printStackTrace();
        }
    }
    
    public void testSendBinaryDoc1() {
        System.out.println("testSendBinaryDoc1");
        try{
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/manifest.mf";
            XmldbURI xmldbUri = XmldbURI.create(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void testSendBinaryDoc2() {
        System.out.println("testSendBinaryDoc2");
        try{
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/notexisting/manifest.mf";
            XmldbURI xmldbUri = XmldbURI.create(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
            
            fail("Expected exception");
        } catch (Exception ex) {
            System.out.println("Expected exception:");
            ex.printStackTrace();
        }
    }
    
    private void sendDocument(XmldbURI uri, InputStream is) throws IOException{
        
        // Setup
        XmlrpcOutputStream xos = new XmlrpcOutputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            xos.write(buf, 0, len);
        }
        
        // Shutdown
        xos.flush();
        xos.close();
//        xos.flush();// required; checks wether all is OK
//        xos.close();// required; checks wether all is OK
//        xos.close();
     
        
    }
    
}
