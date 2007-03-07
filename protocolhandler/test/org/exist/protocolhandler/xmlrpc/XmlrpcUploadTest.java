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
public class XmlrpcUploadTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcUploadTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcUploadTest(String testName) {
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
    public void testToDB()  {
        
        System.out.println("testToDB");
         
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
     * Test upload of file to non existing collection
     */
    public void testToDB_NotExistingCollection() {
        System.out.println("testToDB_NotExistingCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to non existing collection must fail.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("Collection /db/foobar not found")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
    
    
    /**
     * Test upload of file as non existing user
     */
    public void testToDB_NotExistingUser() {
        System.out.println("testToDB_NotExistingUser");
        
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload as non existing user must fail.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("User foo unknown")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
    
    /**
     * Test upload of file to a forbidden collection
     */
    public void testToDB_NotAuthorized() {
        System.out.println("testToDB_NotAuthorized");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/build.xml";
        File src = new File("build.xml");
        
        XmlrpcUpload xuc = new XmlrpcUpload();
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream(src));
            
            fail("Upload to collection /db/system/ must fail.");

        } catch (Exception ex) {
            if(!ex.getMessage().contains("User 'guest' not allowed to write to collection '/db/system'")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        } 
    }
}
