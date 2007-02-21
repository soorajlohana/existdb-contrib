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

package org.exist.xmlrpc.write;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.io.ExistIOException;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 *  jUnit tests for XmlrpcUpload class
 * .
 * 
 * @author Dannes Wessels
 */
public class XmlrpcUploadChunkedTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcUploadChunkedTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcUploadChunkedTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            PropertyConfigurator.configure("log4j.conf");
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        // -/-
    }
    
    /**
     * Test upload of file
     */
    public void testUploadFile()  {
        
        System.out.println("testUploadFile");
         
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (Exception ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test upload of file to non existing collection
     */
    public void testUploadFileToNotExistingCollection() {
        System.out.println("testUploadFileToNotExistingCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to non existing collection must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (ExistIOException ex) {
            
            if(!ex.getCause().getMessage().contains("Collection /db/foobar not found")){
                fail(ex.getMessage());
            }
        } catch (Exception ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        }
    }
    
    
    /**
     * Test upload of file as non existing user
     */
    public void testUploadFileAsNotExistingUser() {
        System.out.println("testUploadFileAsNotExistingUser");
        
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload as non existing user must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (ExistIOException ex) {
            if(!ex.getCause().getMessage().contains("User foo unknown")){
                fail(ex.getMessage());
            }
        } catch (Exception ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
            
    }
    
    /**
     * Test upload of file to a forbidden collection
     */
    public void testUploadFileToForbiddenCollection() {
        System.out.println("testUploadFileToForbiddenCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to collection /db/system/ must fail.");
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            fail(ex.getMessage());
            
        } catch (ExistIOException ex) {
            if(!ex.getMessage().contains("User 'guest' not allowed to write to collection '/db/system'")){
                fail(ex.getMessage());
            }
        } catch (Exception ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
}
