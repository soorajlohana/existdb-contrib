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
import org.exist.protocols.eXistURLStreamHandlerFactory;

/**
 *
 * @author Dannes Wessels
 */
public class UrlTest extends TestCase {
    
    private static String XMLDB_URL_1=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    public UrlTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
    public void testXmldbURI_getUserInfo1() {
        System.out.println("testXmldbURI_getUserInfo1");
        
        URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
        try {
            
            URL test = new URL(XMLDB_URL_1);
            String result = test.toString();
            
            Assert.assertEquals(XMLDB_URL_1, result);
            
            String modified = "http"+result.substring(11);
            
            URL modifiedURL = new URL(modified);
            
            Assert.assertEquals("guest:guest", modifiedURL.getUserInfo() );
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void testXmldbURI_getUserInfo2() {
        System.out.println("testXmldbURI_getUserInfo2");
        
        //URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
        try {
            
            URL test = new URL(XMLDB_URL_1);
            String result = test.toString();
            
            Assert.assertEquals(XMLDB_URL_1, result);
            
            String modified = "http"+result.substring(11);
            
            URL modifiedURL = new URL(modified);
            
            assertTrue(!"foo:bar".equals( modifiedURL.getUserInfo() ) );
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
