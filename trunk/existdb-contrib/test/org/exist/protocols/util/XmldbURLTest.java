/*
 * XmldbURLTest.java
 * JUnit based test
 *
 * Created on January 21, 2007, 6:37 PM
 */

package org.exist.protocols.util;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author wessels
 */
public class XmldbURLTest extends TestCase {
    
    private static String XMLDB_URL_1=
            "xmldb:exist://username:password@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static XmldbURL xmldbUrl;
    
    public XmldbURLTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(xmldbUrl==null){
            BasicConfigurator.configure();
            System.setProperty( "java.protocol.handler.pkgs", "org.exist.protocols" );
            xmldbUrl=new XmldbURL(XMLDB_URL_1);
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getUserInfo method, of class org.exist.protocols.util.XmldbURL.
     */
    public void testGetUserInfo() {
        assertEquals("username:password", xmldbUrl.getUserInfo());
    }
    
    /**
     * Test of getUsername method, of class org.exist.protocols.util.XmldbURL.
     */
    public void testGetUsername() {
        assertEquals("username", xmldbUrl.getUsername());
    }
    
    /**
     * Test of getPassword method, of class org.exist.protocols.util.XmldbURL.
     */
    public void testGetPassword() {
        assertEquals("password", xmldbUrl.getPassword());
    }
    
    /**
     * Test of getCollection method, of class org.exist.protocols.util.XmldbURL.
     */
    public void testGetCollection() {
        assertEquals("/db/shakespeare/plays", xmldbUrl.getCollection());
    }
    
    /**
     * Test of getDocumentName method, of class org.exist.protocols.util.XmldbURL.
     */
    public void testGetDocumentName() {
        assertEquals("macbeth.xml", xmldbUrl.getDocumentName());
    }
    
}
