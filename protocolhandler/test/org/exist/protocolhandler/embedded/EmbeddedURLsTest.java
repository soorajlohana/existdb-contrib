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

package org.exist.protocolhandler.embedded;

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
import org.exist.storage.BrokerPool;
import org.exist.util.Configuration;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 *
 * @author Dannes Wessels
 */
public class EmbeddedURLsTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedURLsTest.class);
    
    private static BrokerPool pool;
    
    private static boolean firstTime=true;
    
    public EmbeddedURLsTest(String testName) {
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
        BrokerPool.stopAll(false);
    }
    
    protected BrokerPool startDB() {
        try {
            Configuration config = new Configuration();
            BrokerPool.configure(1, 5, config);
            
            // initialize driver
            Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);
            
            return BrokerPool.getInstance();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return null;
    }
    
    private void sendToURL(String URL, String file) throws Exception {
        
        pool = startDB();
        URL url = new URL(URL);
        InputStream is = new BufferedInputStream( new FileInputStream(file) );
        OutputStream os = url.openConnection().getOutputStream();
        copyDocument(is,os);
        is.close();
        os.flush();
        os.close();
        
    }
    
    private void getFromURL(String URL, OutputStream os) throws Exception {
        
        pool = startDB();
        URL url = new URL(URL);
        InputStream is = url.openConnection().getInputStream();
        copyDocument(is,os);
        
        is.close();
        os.flush();
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
    
    // ======================================================================
    
    public void testToDB() {
        System.out.println("testToDB");
        
        try {
            sendToURL(
                    "xmldb:exist:///db/build_testToDB.xml",
                    "build.xml" );
            
        } catch (Exception ex) {
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    public void testFromDB() {
        System.out.println("testFromDB");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist:///db/build_testToDB.xml", os);
            os.flush();
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    public void testToDB_NotExistingCollection() {
        System.out.println("testToDB_NotExistingCollection");
        try {
            sendToURL("xmldb:exist:///db/foo/bar.xml",
                    "build.xml");
            fail("Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Resource /db/foo is not a collection.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testFromDB_NotExistingCollection() {
        System.out.println("testFromDB_NotExistingCollection");
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist:///db/foo.bar", os);
            os.flush();
            os.close();
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches("Resource .* not found.")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
    public void testToDB_NotExistingUser() {
        System.out.println("testToDB_NotExistingUser");
        try {
            sendToURL("xmldb:exist://foo:bar@/db/testToDB_NotExistingUser.xml",
                    "build.xml");
            
            fail("Not existing user: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Unauthorized user.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    public void testFromDB_NotExistingUser() {
        System.out.println("testFromDB_NotExistingUser");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://foo:bar@/db/testFromDB_NotExistingUser.xml", os);
            
            os.flush();
            os.close();
            
            fail("Not existing user: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Unauthorized user.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    
    public void testToDB_NotAuthorized() {
        System.out.println("testToDB_NotAuthorized");
        try {
            sendToURL("xmldb:exist://guest:guest@/db/system/testToDB_NotAuthorized.xml",
                    "build.xml");
            
            fail("Not authorized: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*not allowed to write to collection.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
    public void testFromDB_NotAuthorized() {
        System.out.println("testFromDB_NotAuthorized");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://guest:guest@/db/system/testToDB_NotAuthorized.xml", os);
            
            os.flush();
            os.close();
            
            fail("Not authorized: Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Permission denied to read collection.*")){
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
    
}
