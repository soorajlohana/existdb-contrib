/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */

package org.exist.protocolhandler.xmldb;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *  jUnit tests for the XmldbURL class.
 *
 * @author Dannes Wessels
 */
public class XmldbURLTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedXmldbURLTest.class);
    
    private static boolean firstRun=true;
    
    private static String XMLDB_URL_0=
            "xmldb:exist://username:password@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_1=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_2=
            "xmldb:exist://guest@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_3=
            "xmldb:exist://@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_4=
            "xmldb:exist://:@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_5=
            "xmldb:exist://localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    // Check some situation
    private static String XMLDB_URL_11=
            "xmldb:exist://localhost:8080/exist/xmlrpc/db/";
    
    private static String XMLDB_URL_12=
            "xmldb:exist://localhost:8080/exist/xmlrpc/db";
    
    private static String XMLDB_URL_13=
            "xmldb:exist://localhost:8080/exist/xmlrpc/";
    
    private static String XMLDB_URL_14=
            "xmldb:exist://localhost:8080/exist/xmlrpc";
    
    private static String XMLDB_URL_15=
            "xmldb:exist://localhost:8080/exist";
    
    // Check some more
    private static String XMLDB_URL_21=
            "xmldb:exist://localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_22=
            "xmldb:exist:///exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    
    public XmldbURLTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstRun){
            PropertyConfigurator.configure("log4j.conf");
            System.setProperty( "java.protocol.handler.pkgs", "org.exist.protocolhandler.protocols" );
            firstRun=false;
        }
    }
    
    protected void tearDown() throws Exception {
        //
    }
    
    public void testRegularExpressions(){
        System.out.println(this.getName());
        
        String PATTERN = org.exist.protocolhandler.protocols.xmldb.Handler.PATTERN;
        String txt1="xmldb:justatext://fobar:8080/exist/xmlrpc/db/foo/bar.txt";
        assertTrue(txt1.matches(PATTERN));
        
        String txt2="xmldb:://fobar:8080/exist/xmlrpc/db/foo/bar.txt";
        assertFalse(txt2.matches(PATTERN));
        
        String txt3="xmldb:abd%^&*efg://fobar:8080/exist/xmlrpc/db/foo/bar.txt";
        assertFalse(txt3.matches(PATTERN));
        
        String txt4="xmldb: ://fobar:8080/exist/xmlrpc/db/foo/bar.txt";
        assertFalse(txt4.matches(PATTERN));
        
        // =================
        String splits[] = txt1.split(":",3);
        assertEquals(3, splits.length);
        
        String instance = splits[1];
        assertEquals("justatext", instance);
        
        int seperator = txt1.indexOf("//");
        assertEquals(16, seperator);
    }
    
    public void testURLclass(){
        System.out.println(this.getName());
        
        try {
            URL urla = new URL("xmldb:justatext://fobar1:8080/exist/xmlrpc/db/foo/bar.txt#foobar");
            assertEquals("fobar1", urla.getHost());
            
            URL urlb = new URL("xmldb:exist://fobar2:8080/exist/xmlrpc/db/foo/bar.txt#foobar");
            assertEquals("fobar2", urlb.getHost());
            
            URL urlc = new URL("xmldb:://fobar3:8080/exist/xmlrpc/db/foo/bar.txt#foobar");
            assertEquals("fobar3", urlc.getHost());
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
        
    }
    
    /**
     * Test of getUserInfo method, of class org.exist.protocolhandler.protocols.util.XmldbURL.
     */
    public void testGetUserInfo() {
        System.out.println(this.getName());
        
        try {
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_0);
            assertEquals("username:password", xmldbUrl.getUserInfo());
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test of getUsername method, of class org.exist.protocolhandler.protocols.util.XmldbURL.
     */
    public void testGetUsername() {
        System.out.println(this.getName());
        try{
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_0);
            assertEquals("username", xmldbUrl.getUsername());
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test of getPassword method, of class org.exist.protocolhandler.protocols.util.XmldbURL.
     */
    public void testGetPassword() {
        System.out.println(this.getName());
        try{
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_0);
            assertEquals("password", xmldbUrl.getPassword());
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test of getCollection method, of class org.exist.protocolhandler.protocols.util.XmldbURL.
     */
    public void testGetCollection() {
        System.out.println(this.getName());
        try{
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_0);
            assertEquals("/db/shakespeare/plays", xmldbUrl.getCollection());
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test of getDocumentName method, of class org.exist.protocolhandler.protocols.util.XmldbURL.
     */
    public void testGetDocumentName() {
        System.out.println(this.getName());
        try{
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_0);
            assertEquals("macbeth.xml", xmldbUrl.getDocumentName());
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    // Some tests are now performed double
    public void testMoreOnOneXmldbURL() {
        System.out.println(this.getName());
        try {
            XmldbURL url = new XmldbURL(XMLDB_URL_1);
            assertEquals("xmldb", url.getProtocol());
            assertEquals("guest:guest", url.getUserInfo());
            assertEquals("localhost", url.getHost());
            assertEquals(8080, url.getPort());
            assertEquals("/exist/xmlrpc/db/shakespeare/plays/macbeth.xml", url.getPath());
            assertNull(url.getQuery());
            assertEquals("/db/shakespeare/plays/macbeth.xml", url.getCollectionPath());
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    
    
    public void testXmldbURI_getUserInfo() {
        System.out.println(this.getName());
        try {
            String userinfo=null;
            
            XmldbURL url = new XmldbURL(XMLDB_URL_1);
            userinfo=url.getUserInfo();
            assertEquals("guest:guest", userinfo  );
            assertTrue( !"foo:bar".equals( userinfo ) );
            
            url = new XmldbURL(XMLDB_URL_1);
            userinfo=url.getUserInfo();
            assertEquals("guest", url.getUsername() );
            assertEquals("guest", url.getPassword() );
            
            
            url = new XmldbURL(XMLDB_URL_2);
            userinfo=url.getUserInfo();
            assertEquals("guest", userinfo);
            assertEquals("guest", url.getUsername() );
            assertNull(url.getPassword() );
            
            url = new XmldbURL(XMLDB_URL_3);
            userinfo=url.getUserInfo();
            assertEquals("", userinfo);
            assertNull(url.getUsername() );
            assertNull(url.getPassword() );
            
            url = new XmldbURL(XMLDB_URL_4);
            userinfo=url.getUserInfo();
            assertEquals(":", userinfo);
            assertNull(url.getUsername() );
            assertNull(url.getPassword() );
            
            url = new XmldbURL(XMLDB_URL_5);
            userinfo=url.getUserInfo();
            assertNull(userinfo);
            assertNull(url.getUsername() );
            assertNull(url.getPassword() );
            
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testXmldbURI_HostName() {
        System.out.println(this.getName());
        try {
            XmldbURL url = new XmldbURL(XMLDB_URL_21);
            assertEquals("localhost", url.getHost()); ///
            
            url = new XmldbURL(XMLDB_URL_22);
            assertNull(url.getHost());
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testXmldbURI_InstanceName() {
        System.out.println(this.getName());
        try {
            XmldbURL url = new XmldbURL(XMLDB_URL_1);
            assertEquals("exist", url.getInstanceName());
            
//            url = new XmldbURL(XMLDB_URL_21);
//           assertEquals("foobar", url.getInstanceName());
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
}
