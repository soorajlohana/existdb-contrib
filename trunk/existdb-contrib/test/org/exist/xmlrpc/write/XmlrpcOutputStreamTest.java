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

package org.exist.xmlrpc.write;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.xmldb.XmldbURL;

/**
 *  jUnit tests for XmlrpcOutputStream class.
 *
 * @author Dannes Wessels.
 */
public class XmlrpcOutputStreamTest extends TestCase {
    
    private static boolean firstTime=true;
    
    public XmlrpcOutputStreamTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            BasicConfigurator.configure();
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        //
    }
    
    public void testSendXmlDoc1() {
        System.out.println("testSendXmlDoc1");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@dignity:8080"
                    +"/exist/xmlrpc/db/build.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void testSendXmlDoc2() {
        System.out.println("testSendXmlDoc2");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@dignity:8080"
                    +"/exist/xmlrpc/db/notexisting/build.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
            fail("Expected exception");
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
            
        } catch (Exception ex) {
            System.out.println("Expected exception:");
            ex.printStackTrace();
        }
    }
    
    public void testSendBinaryDoc1() {
        System.out.println("testSendBinaryDoc1");
        try{
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@dignity:8080"
                    +"/exist/xmlrpc/db/manifest.mf";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void testSendBinaryDoc2() {
        System.out.println("testSendBinaryDoc2");
        try{
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@dignity:8080"
                    +"/exist/xmlrpc/db/notexisting/manifest.mf";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument( xmldbUri, fis);
            fis.close();
            
            fail("Expected exception");
        } catch (MalformedURLException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
            
        } catch (Exception ex) {
            System.out.println("Expected exception:");
            ex.printStackTrace();
        }
    }
    
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
        xos.flush();
        xos.close();
        
    }
    
}
