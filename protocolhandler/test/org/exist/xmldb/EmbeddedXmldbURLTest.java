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
 *  $Id$
 */

package org.exist.xmldb;

import java.net.MalformedURLException;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *  jUnit tests for the XmldbURL class.
 *
 * @author Dannes Wessels
 */
public class EmbeddedXmldbURLTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedXmldbURLTest.class);
    
    private static boolean firstRun=true;
    
    private static String XMLDB_URL_1=
            "xmldb:exist:///db/shakespeare/plays/macbeth.xml";
    
    private static String XMLDB_URL_2=
            "xmldb:exist:///db/shakespeare/plays/";
    
    private static String XMLDB_URL_3=
            "xmldb:exist:///db/foobar.txt";
    
    private static String XMLDB_URL_4=
            "xmldb:exist:///db";
    
    public EmbeddedXmldbURLTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstRun){
            PropertyConfigurator.configure("log4j.conf");
            System.setProperty( "java.protocol.handler.pkgs", "org.exist.protocols" );
            firstRun=false;
        }
    }
    
    protected void tearDown() throws Exception {
        //
    }
    
    public void testHostname() {
        System.out.println("testHostname");
        try {
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_1);
            assertNull(xmldbUrl.getHost());
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    public void testURL1() {
        System.out.println("testURL1");
        try {
            XmldbURL xmldbUrl=new XmldbURL(XMLDB_URL_1);
            assertNull(xmldbUrl.getHost());
            assertEquals("/db/shakespeare/plays", xmldbUrl.getCollection());
            assertEquals("macbeth.xml", xmldbUrl.getDocumentName());
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
        }
    }
    
    
}