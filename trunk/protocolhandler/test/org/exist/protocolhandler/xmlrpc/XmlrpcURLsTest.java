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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;

/**
 * @author Dannes Wessels
 */
public class XmlrpcURLsTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcURLsTest.class);
    private static boolean firstTime=true;
    
    public XmlrpcURLsTest(String testName) {
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
        // empty
    }
    
    private void sendToURL(String URL, String file) throws Exception {
        URL url = new URL(URL);
        InputStream is = new BufferedInputStream( new FileInputStream(file) );
        OutputStream os = url.openConnection().getOutputStream();
        copyDocument(is,os);
        is.close();
        os.close();
    }
    
    private void getFromURL(String URL, OutputStream os) throws Exception {
        URL url = new URL(URL);
        InputStream is = url.openConnection().getInputStream();
        copyDocument(is,os);
        is.close();
        os.close();
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
    
    // =====================================
    // TODO xmldb:exist://localhost:8080/db/build_testURLToDB.xml ?
    public void testURLToDB() {
        System.out.println("testURLToDB");
        try {
            sendToURL(
                    "xmldb:exist://localhost:8080/exist/xmlrpc/db/build_testURLToDB.xml",
                    "build.xml" );

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testURLFromDB() {
        System.out.println("testURLFromDB");
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/build_testURLToDB.xml", os);

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    public void testURLToDB_notExistingCollection() {
        System.out.println("testURLToDB_notExistingCollection");
        try {
            sendToURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/foo/bar.xml",
                    "build.xml");
            fail("Not existing collection: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foo not found.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLFromDB_notExistingCollection() {
        System.out.println("testURLFromDB_notExistingCollection");
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://localhost:8080/exist/xmlrpc/db/foo/bar.xml", os);

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Collection /db/foo not found.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLToDB_NotExistingUser() {
        System.out.println("testURLToDB_NotExistingUser");
        try {
            sendToURL("xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/testURLToDB_NotExistingUser.xml",
                    "build.xml");
            fail("Not existing user: Exception expected");

        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User foo unknown.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testURLFromDB_NotExistingUser() {
        System.out.println("testURLFromDB_NotExistingUser");
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/testURLFromDB_NotExistingUser.xml", os);

            assertTrue(os.size()==0);
            
            fail("Not existing user: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*User .* unknown.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
}
