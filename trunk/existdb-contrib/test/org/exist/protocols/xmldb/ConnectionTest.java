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

package org.exist.protocols.xmldb;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

/**
 *  jUnit tests for GETting and PUTting data to eXist.
 *
 * @author Dannes Wessels
 */
public class ConnectionTest extends TestCase {
    
    private static boolean firstTime=true;
    
    public ConnectionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            BasicConfigurator.configure();
            System.setProperty( "java.protocol.handler.pkgs", "org.exist.protocols" );
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        // empty yet
    }
    
    /**
     * Test of writing data to eXist server. Data will be reused by
     * subsequent tests.
     */
    public void testPutDocumentToExistingCollection() throws Exception {
        System.out.println("testPutDocumentToExistingCollection");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/build.xml");

            OutputStream os = url.openConnection().getOutputStream();
            FileInputStream is = new FileInputStream("build.xml");
            copyDocument(is, os);
            is.close();
            os.close();
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
                        
        } catch (IOException ex) {
            fail(ex.getMessage());
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            
        }
    }
    
    /**
     * Test reading an existing document from eXist.
     */
    public void testGetExistingDocument() {
        System.out.println("testGetExistingDocument");
        try {
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/shakespeare/plays/macbeth.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            fail(ex.getMessage());
            
        }
    }
    
    /**
     * Test reading an non-existing document from eXist.
     */
    public void testGetNonExistingDocument() {
        System.out.println("testGetNonExistingDocument");
        try {
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/foobar/macbeth.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
            fail("Document should not exist");
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());

        } catch (IOException ex) {
            assertTrue(ex.getMessage().contains("Collection /db/foobar not found!"));
        }
    }

    /**
     * Test reading an existing document from eXist as a non-existing user.
     */
    public void testGetDocumentForNonExistingUser() {
        System.out.println("testGetDocumentForNonExistingUser");
        ByteArrayOutputStream baos =  new ByteArrayOutputStream();
        try {
            URL url = new URL("xmldb:exist://foo:bar@localhost:8080"
                    +"/exist/xmlrpc/db/shakespeare/plays/macbeth.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
            fail("user should not exist");
            
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            
        } catch (IOException ex) {
            assertTrue(ex.getMessage().contains("User foo unknown"));
            
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
