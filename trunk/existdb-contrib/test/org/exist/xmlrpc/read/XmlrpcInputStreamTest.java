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

package org.exist.xmlrpc.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.exist.xmldb.XmldbURI;
import org.exist.xmlrpc.read.XmlrpcInputStream;

/**
 *
 * @author wessels
 */
public class XmlrpcInputStreamTest extends TestCase {
    
    private static boolean firstTime=true;
    
    public XmlrpcInputStreamTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            BasicConfigurator.configure();
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test retrieve document from db.
     */
    public void testGetXmlDoc1() {
        System.out.println("testGetXmlDoc1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/macbeth.xml";
        XmldbURI xmldbUri = XmldbURI.create(uri);
        try {
            getDocument(xmldbUri, baos);
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Test try retrieve non existing document from db.
     */
    public void testGetXmlDoc2() {
        System.out.println("testGetXmlDoc2");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/foobar.xml";
        XmldbURI xmldbUri = XmldbURI.create(uri);
        try {
            getDocument(xmldbUri, baos);
            baos.close();
            fail("exception should be thrown");
            
        } catch (Exception ex) {
            System.out.println("Expected exception:");
            ex.printStackTrace();
        }
    }
    
    /**
     * Test retrieve binary document from db.
     */
    public void testGetBinaryDoc1() throws Exception {
        System.out.println("testGetBinaryDoc1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/shakes.css";
        
        XmldbURI xmldbUri = XmldbURI.create(uri);
        try {
            getDocument(xmldbUri, baos);
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Test retrieve non existing binary document from db.
     */
    public void testGetBinaryDoc2() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/foo.css";
        XmldbURI xmldbUri = XmldbURI.create(uri);
        try {
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");
        } catch (Exception ex) {
            System.out.println("Excpected exception:");
            ex.printStackTrace();
        }
    }
    
    // Copy document from URL to outputstream
    private void getDocument(XmldbURI uri, OutputStream os) throws IOException{
        
        // Setup
        InputStream xis = new XmlrpcInputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = xis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        
        // Shutdown
        os.flush();
        os.close();
        xis.close(); // required; checks wether all is OK
        
    }
}
