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
 * $Id$
 */

package org.exist.protocolhandler.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.XmldbURL;
import org.exist.protocolhandler.eXistURLStreamHandlerFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  jUnit tests for XmlrpcUpload class
 * .
 * 
 * @author Dannes Wessels
 */
public class XmlrpcUploadDownloadTest{
    
    private static Logger LOG = Logger.getLogger(XmlrpcUploadDownloadTest.class); 

    @BeforeClass
    public static void start() throws Exception {

        URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
        PropertyConfigurator.configure("log4j.conf");

    }

    /**
     * Test upload of file
     */
    @Test
    public void toDB()  {
        
        System.out.println("toDB");
         
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));

        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test download of file.
     */
    @Test
    public void fromDB() {
        
        System.out.println("fromDB");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
            assertTrue(baos.size()>0);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }

    
    /**
     * Test upload of file to non existing collection
     */
    @Test
    public void toDB_NotExistingCollection() {
        System.out.println("toDB_NotExistingCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to non existing collection must fail.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foobar not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
    
    
    /**
     * Test download of file from not existing collection.
     */
    @Test
    public void fromDB_NotExistingCollection() {
        
        System.out.println("fromDB_NotExistingCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
            fail("Collection does not exist: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getMessage().matches(".*Collection /db/foobar not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            };
        }
        
    }
    
    
    /**
     * Test upload of file as non existing user
     */
    @Test
    public void toDB_NotExistingUser() {
        System.out.println("toDB_NotExistingUser");
        
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload as non existing user must fail.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
    
    /**
     * Test download of file as non existing user.
     */
    @Test
    public void fromDB_NotExistingUser() {
        
        System.out.println("fromDB_NotExistingUser");
        
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
            fail("User does not exist: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    /**
     * Test upload of file to a forbidden collection
     */
    @Test
    public void toDB_NotAuthorized() {
        System.out.println("toDB_NotAuthorized");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to collection /db/system/ must fail.");

        } catch (Exception ex) {
            if(!ex.getMessage().matches(".*User 'guest' not allowed to write to collection '/db/system'.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
    
 
    /**
     * Test download of file to a forbidden collection
     */
    @Test
    public void fromDB_NotAuthorized() {
        
        System.out.println("fromDB_NotAuthorized");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/users.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
            fail("User not authorized: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getMessage().matches(".*Insufficient privileges to read resource.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    /*
     * Additional tests on binary resources
     */
    
    /**
     * Test upload of file
     */
    @Test
    public void toDB_BinaryDoc()  {
        
        System.out.println("toDB_BinaryDoc");
         
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/manifest.txt";
        File src = new File("manifest.mf");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));

        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test download of file.
     */
    @Test
    public void fromDB_BinaryDoc() {
        
        System.out.println("fromDB_BinaryDoc");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/manifest.txt";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
            assertTrue("Filesize must be greater than 0", baos.size()>0);
            assertEquals(85, baos.size());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
}
