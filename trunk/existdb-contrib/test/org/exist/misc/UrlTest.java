/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 The eXist Project
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
 * $Id$
 */

package org.exist.misc;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.*;
import org.exist.protocols.Credentials;
import org.exist.protocols.Shared;
import org.exist.protocols.eXistURLStreamHandlerFactory;

/**
 *
 * @author Dannes Wessels
 */
public class UrlTest extends TestCase {
    
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
    
    private String getUserInfo(String xmldbUri) throws MalformedURLException{
        URL modifiedURL = new URL( "http"+xmldbUri.substring(11) );
        return modifiedURL.getUserInfo();
    }
    
    public UrlTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
    public void testXmldbURI_getUserInfo() {
        System.out.println("testXmldbURI_getUserInfo");
        
        URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
        try {
            // check wether round trip is ok
            Assert.assertEquals(XMLDB_URL_1, new URL(XMLDB_URL_1).toString() );
            
            // Double test :-) makes no sense
            Credentials creds=null;
            String userinfo = getUserInfo(XMLDB_URL_1);
            assertEquals("guest:guest", userinfo );
            assertTrue( !"foo:bar".equals(userinfo) );
            
            creds=Shared.extractUserInfo(XMLDB_URL_1);
            assertEquals("guest", creds.username );
            assertEquals("guest", creds.password );
            
            creds=null;
            userinfo = getUserInfo(XMLDB_URL_2);
            creds=Shared.extractUserInfo(XMLDB_URL_2);
            assertEquals("guest", userinfo);
            assertEquals("guest", creds.username );
            assertNull(null, creds.password );
            
            creds=null;
            userinfo = getUserInfo(XMLDB_URL_3);
            creds=Shared.extractUserInfo(XMLDB_URL_3);
            assertEquals("", userinfo);
            assertNull(null, creds.username );
            assertNull(null, creds.password );
            
            creds=null;
            userinfo = getUserInfo(XMLDB_URL_4);
            creds=Shared.extractUserInfo(XMLDB_URL_4);
            assertEquals(":", userinfo);
            assertNull(null, creds.username );
            assertNull(null, creds.password );
            
            creds=null;
            userinfo = getUserInfo(XMLDB_URL_5);
            creds=Shared.extractUserInfo(XMLDB_URL_5);
            assertNull(userinfo);
            assertNull(null, creds.username );
            assertNull(null, creds.password );
            
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
