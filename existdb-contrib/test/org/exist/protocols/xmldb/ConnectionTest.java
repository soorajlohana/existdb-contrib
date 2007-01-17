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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author wessels
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
    }
    
    /**
     * Test of reading existent data from eXist server.
     */
    public void testGet1() {
        System.out.println("testGet1");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/macbeth.xml");

            InputStream is = url.openStream();
            copyDocument(is, System.out);
            is.close();
            
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of reading non existent data from eXist server.
     */
    public void testGet2() {
        System.out.println("testGet2");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/foobar/macbeth.xml");

            InputStream is = url.openStream();
            copyDocument(is, System.out);
            is.close();
            
            fail("Document should not exist");
            
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            //fail(ex.toString());
            // Expected TODO more acurate check
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of writing data to eXist server.
     */
    public void testPut() throws Exception {
        System.out.println("testPut");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/build.xml");

            OutputStream os = url.openConnection().getOutputStream();
            FileInputStream is = new FileInputStream("build.xml");
            copyDocument(is, os);
            is.close();
            os.close();
            
        } catch (MalformedURLException ex) {
            fail(ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            fail(ex.toString());
            ex.printStackTrace();
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
