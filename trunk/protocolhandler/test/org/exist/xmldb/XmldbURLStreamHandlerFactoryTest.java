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

package org.exist.xmldb;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * jUnit test for the eXist XmldbURLStreamHandlerFactory class.
 *
 * @author Dannes Wessels
 */
public class XmldbURLStreamHandlerFactoryTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmldbURLStreamHandlerFactoryTest.class);
    
    private static String XMLDB_URL_1=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/build_streamhandler.xml";
    
    private static String XMLDB_URL_2=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/system/build_streamhandler.xml";
    
    private static String XMLDB_URL_3=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/foobar/build_streamhandler.xml";
    
    private static boolean firstTime=true;
    
    public XmldbURLStreamHandlerFactoryTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws Exception {
        // Nothing to do yet
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            PropertyConfigurator.configure("log4j.conf");
            firstTime=false;
        }
    }
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testWrite).
     */
    public void testWriteToURL() {
        System.out.println("testWriteToURL");
        
        try {
            InputStream is = new FileInputStream("build.xml");
            URL url = new URL(XMLDB_URL_1);
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is, os);
            is.close();
            os.close();
            
        } catch (Exception ex) {
            fail(ex.toString());
            LOG.error(ex);
        }
    }
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testRead).
     */
    public void testReadFromURL() {
        System.out.println("testRead");
        
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            URL url = new URL(XMLDB_URL_1);
            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
        } catch (Exception ex) {
            fail(ex.toString());
            LOG.error(ex);
        }
    }
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testWrite).
     */
    public void testWriteToURL_PermissionDenied() {
        System.out.println("testWriteToURL_PermissionDenied");
        
        try {
            InputStream is = new FileInputStream("build.xml");
            URL url = new URL(XMLDB_URL_2);
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is, os);
            is.close();
            os.close();
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("User 'guest' not allowed to write to collection '/db/system'")){
                fail(ex.getCause().getMessage());
            }
        }
    }
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testRead).
     */
    public void testReadFromURL_PermissionDenied() {
        System.out.println("testReadFromURL_PermissionDenied");
        
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            URL url = new URL(XMLDB_URL_2);
            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("Insufficient privileges to read resource")){
                fail(ex.getCause().getMessage());
            }
        }
    }
    
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testWrite).
     */
    public void testWriteToURL_NotExisting() {
        System.out.println("testWriteToURL_NotExisting");
        
        try {
            InputStream is = new FileInputStream("build.xml");
            URL url = new URL(XMLDB_URL_3);
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is, os);
            is.close();
            os.close();
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("Collection /db/foobar not found")){
                fail(ex.getCause().getMessage());
            }
        }
    }
    
    /**
     * Test of XmldbURLStreamHandlerFactory (testRead).
     */
    public void testReadFromURL__NotExisting() {
        System.out.println("testReadFromURL__NotExistings");
        
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            URL url = new URL(XMLDB_URL_3);
            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("Collection /db/foobar not found")){
                fail(ex.getCause().getMessage());
            }
        }
    }
    
    // Transfer bytes from inputstream to outputstream
    private void copyDocument(InputStream is, OutputStream os) throws IOException{
        
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        os.flush();
    }
}