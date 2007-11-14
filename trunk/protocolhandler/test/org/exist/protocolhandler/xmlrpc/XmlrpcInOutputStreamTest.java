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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.eXistURLStreamHandlerFactory;
import org.exist.protocolhandler.xmldb.XmldbURL;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  jUnit tests for XmlrpcOutputStream class.
 *
 * @author Dannes Wessels.
 */
public class XmlrpcInOutputStreamTest {
    
    private static Logger LOG = Logger.getLogger(XmlrpcInOutputStreamTest.class);
    
    private String TESTCASENAME= getClass().getName();
    
    
    @BeforeClass
    public static void start() throws Exception {

        URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
        PropertyConfigurator.configure("log4j.conf");

    }
    
    
    // ***************************************
    
    private void sendDocument(XmldbURL uri, InputStream is) throws IOException{
        
        // Setup
        XmlrpcOutputStream xos = new XmlrpcOutputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            xos.write(buf, 0, len);
        }
        
        // Shutdown
        xos.close();
        
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
    
    // ***********************
    
    @Test
    public void testCreateCollection(){
        try {
            URL url = new URL("http://localhost:8080/exist/rest/db?_query="
                    +"xmldb:create-collection(%22/db/%22,%22"+TESTCASENAME+"%22)");
            url.openStream();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    //ToDB
    @Test
    public void toDB() {
        System.out.println("toDB");
        try{
            FileInputStream fis = new FileInputStream("conf.xml");
            String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                    +TESTCASENAME+"/conf_toDB.xml";
            XmldbURL xmldbUri = new XmldbURL(url);
            sendDocument( xmldbUri, fis);
            fis.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    //FromDB
    @Test
    public void fromDB() {
        System.out.println("fromDB");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                +TESTCASENAME+"/conf_toDB.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(url);
            getDocument(xmldbUri, baos);
            assertTrue(baos.size()>0);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    //ToDB_NotExistingCollection
    @Test
    public void toDB_NotExistingCollection() {
        System.out.println("toDB_NotExistingCollection");
        try{
            FileInputStream fis = new FileInputStream("conf.xml");
            String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                    +TESTCASENAME+"/foobar/conf_toDB.xml";
            XmldbURL xmldbUri = new XmldbURL(url);
            sendDocument( xmldbUri, fis);
            fis.close();
            
            fail("not existing collection: Expected exception");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/.* not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    //FromDB_NotExistingCollection
    @Test
    public void fromDB_NotExistingCollection() {
        System.out.println("fromDB_NotExistingCollection");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                +TESTCASENAME+"/foobar/conf_toDB.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(url);
            getDocument(xmldbUri, baos);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/.* not found.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    //ToDB_NotExistingUser
    @Test
    public void toDB_NotExistingUser() {
        System.out.println("toDB_NotExistingUser");
        try{
            FileInputStream fis = new FileInputStream("conf.xml");
            String url = "xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/"
                    +TESTCASENAME+"/conf_toDB.xml";
            XmldbURL xmldbUri = new XmldbURL(url);
            sendDocument( xmldbUri, fis);
            fis.close();
            
            fail("not existing user: Expected exception");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    //FromDB_NotExistingUser
    @Test
    public void fromDB_NotExistingUser() {
        System.out.println("fromDB_NotExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url = "xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/"
                +TESTCASENAME+"/conf_toDB.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(url);
            getDocument(xmldbUri, baos);
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    //ToDB_NotAuthorized
    @Test
    public void toDB_NotAuthorized() {
        System.out.println("toDB_NotAuthorized");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String url = "xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/system/users.xml";
            XmldbURL xmldbUri = new XmldbURL(url);
            sendDocument( xmldbUri, fis);
            fis.close();
            
            fail("User not authorized: Expected exception");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Document exists and update is not allowed for the collection.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    //FromDB_NotAuthorized
    @Test
    public void fromDB_NotAuthorized() {
        System.out.println("fromDB_NotAuthorized");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url = "xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/system/users.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(url);
            getDocument(xmldbUri, baos);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Insufficient privileges to read resource")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    /*
     * Extra binary tests
     */
    
    //ToDB
    @Test
    public void toDB_binaryDoc() {
        System.out.println("toDB_binaryDoc");
        try{
            FileInputStream fis = new FileInputStream("manifest.mf");
            String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                    +TESTCASENAME+"/manifest.txt";
            XmldbURL xmldbUri = new XmldbURL(url);
            sendDocument( xmldbUri, fis);
            fis.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    //FromDB
    @Test
    public void fromDB_binaryDoc() {
        System.out.println("fromDB_binaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url = "xmldb:exist://localhost:8080/exist/xmlrpc/db/"
                +TESTCASENAME+"/manifest.txt";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(url);
            getDocument(xmldbUri, baos);
            
            assertTrue("Filesize must be greater than 0", baos.size()>0);
            assertEquals(85, baos.size());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
}
