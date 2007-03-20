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

package org.exist.protocolhandler.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.protocolhandler.xmldb.XmldbURL;

/**
 *  jUnit tests for XmlrpcInputStream class.
 *
 * @author Dannes Wessels
 */
public class XmlrpcInputStreamTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcInputStreamTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcInputStreamTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            PropertyConfigurator.configure("log4j.conf");
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        // Empty
    }

    /**
     * Test retrieve document from db.
     */
    public void testFromDB_XmlDoc() {
        System.out.println("testFromDB_XmlDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/macbeth.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            assertTrue(baos.size()>0);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test try retrieve non existing document from db.
     */
    public void testFromDB_NotExistingDoc_XmlDoc() {
        System.out.println("testFromDB_NotExistingDoc_XmlDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/foobar.xml";
        try {
            XmldbURL xmldbUri=xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*document not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    /**
     * Test retrieve binary document from db.
     */
    public void testFromDB_BinaryDoc() throws Exception {
        System.out.println("testFromDB_BinaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/shakes.css";
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            
            // TODO sometimes baos is empty
            assertTrue(baos.size()>0);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test retrieve non existing binary document from db.
     */
    public void testFromDB_NotExistingDoc_BinaryDoc() throws Exception {
        System.out.println("testFromDB_NotExistingDoc_BinaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/foo.css";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("document not found")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
//FromDB_NotExistingCollection
    public void testFromDB_NotExistingCollection() {
        System.out.println("FromDB_NotExistingCollection");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/foo/bar.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foo not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
//FromDB_NotExistingUser
    public void testFromDB_NotExistingUser() {
        System.out.println("testFromDB_NotExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/foo/bar.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
//FromDB_NotAuthorized
    public void testFromDB_NotAuthorized() {
        System.out.println("testFromDB_NotAuthorized");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/system/users.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("exception should be thrown");

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Insufficient privileges to read resource.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    // Copy document from URL to outputstream
    private void getDocument(XmldbURL uri, OutputStream os) throws IOException{
        
        // Setup
        InputStream xis = new XmlrpcInputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = xis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }

        // Shutdown
        xis.close(); // required; checks wether all is OK
    }
}
