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

package org.exist.xmlrpc.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmlrpc.XmlrpcDownload;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 *  jUnit tests for XmlrpcDownload class.
 * 
 * 
 * @author Dannes Wessels
 */
public class XmlrpcDownloadChunkedTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcDownloadChunkedTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcDownloadChunkedTest(String testName) {
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
    }
    
    /**
     * Test download of file.
     */
    public void testDownloadExistingFile() {
        
        System.out.println("testDownloadExistingFile");
            
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test download of file from not existing collection.
     */
    public void testDownloadFileFromNotExistingCollection() {
        
        System.out.println("testDownloadFileFromNotExistingCollection");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("Collection /db/foobar not found!")){
                fail(ex.getMessage());
            };
        }

    }
    
    /**
     * Test download of file as non existing user.
     */
    public void testDownloadAsNotExistingUser() {
        
        System.out.println("testDownloadAsNotExistingUser");
        
        String url = "xmldb:exist://foo:bar@localhost:8080"
                +"/exist/xmlrpc/db/build.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("User foo unknown")){
                fail(ex.getMessage());
            }
        }
    }
    
    /**
     * Test download of file to a forbidden collection
     */
    public void testDownloadForbiddenFile() {
        
        System.out.println("testDownloadForbiddenFile");
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/system/users.xml";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlrpcDownload xdc = new XmlrpcDownload();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xdc.stream(xmldbURL, baos);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            if(!ex.getMessage().contains("Insufficient privileges to read resource")){
                fail(ex.getMessage());
            }
        }
    }
    
}
