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

package org.exist.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.exist.xmldb.eXistURLStreamHandlerFactory;

/**
 * jUnit test for the eXist URLStreamHandlerFactory (xmldb).
 *
 * @author Dannes Wessels
 */
public class eXistURLStreamHandlerFactoryTest extends TestCase {
    
    private static String XMLDB_URL_1=
            "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc"
            +"/db/shakespeare/plays/macbeth.xml";
    
    private static boolean firstTime=true;
    
    public eXistURLStreamHandlerFactoryTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws Exception {
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
            BasicConfigurator.configure();
            firstTime=false;
        }
    }
    
    /**
     * Test of eXistURLStreamHandlerFactory.
     */
    public void testXMLDBURLStreamHandler() {
        System.out.println("testXMLDBURLStreamHandler");
        
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            URL url = new URL(XMLDB_URL_1);
            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();
            
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
