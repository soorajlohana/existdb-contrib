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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.protocols.xmldb.Handler;

/**
 *  Set of tests that could not be put somewhere else
 *
 * @author Dannes Wessels
 */
public class MiscUrlTest extends TestCase {

    private static Logger LOG = Logger.getLogger(MiscUrlTest.class);
     private static boolean firstRun=true;
    
    public MiscUrlTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        if(firstRun){
            PropertyConfigurator.configure("log4j.conf");
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            firstRun=false;
        }
    }

    protected void tearDown() throws Exception {
    }
    
    
    public void testRegularExpressions(){
        System.out.println(this.getName());
        
        String PATTERN = Handler.PATTERN;
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
    
    public void testDemoServerURLs(){
        try {
            XmldbURL urla = new XmldbURL("xmldb:exist://demo.exist-db.org/xmlrpc/db/foo/bar.xml?a=b#cc");
            assertEquals("demo.exist-db.org", urla.getHost());
            assertEquals(-1, urla.getPort());
            assertEquals("bar.xml", urla.getDocumentName());
            assertEquals("/db/foo", urla.getCollection());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
 


}
