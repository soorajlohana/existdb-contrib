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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.protocolhandler.xmldb.XmldbURL;

/**
 *  jUnit tests for XmlrpcOutputStream class.
 *
 * @author Dannes Wessels.
 */
public class XmlrpcInOutputStreamTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcInOutputStreamTest.class);
    
    private static boolean firstTime=true;
    
    public XmlrpcInOutputStreamTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            PropertyConfigurator.configure("log4j.conf");
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        //
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
        xos.flush();
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
    
    //ToDB
    //FromDB
    //ToDB_NotExistingCollection
    //FromDB_NotExistingCollection
    //ToDB_NotExistingUser
    //FromDB_NotExistingUser
    //ToDB_NotAuthorized
    //FromDB_NotAuthorized
    
    
    
    //ToDB_NotExistingUser
    public void testToDB_NotExistingUser() {
        System.out.println("testToDB_NotExistingUser");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://foo:bar@localhost:8080"
                    +"/exist/xmlrpc/db/build_testToDB_NotExistingUser.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
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
    
    //ToDB_NotAuthorized
    public void testToDB_NotAuthorized() {
        System.out.println("testToDB_NotAuthorized");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@localhost:8080"
                    +"/exist/xmlrpc/db/system/users.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
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
    
    

    
}
