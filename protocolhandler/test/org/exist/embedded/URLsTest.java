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

package org.exist.embedded;

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
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 *
 * @author Dannes Wessels
 */
public class URLsTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(URLsTest.class);
    
    private static BrokerPool pool;
    
    private static boolean firstTime=true;
    
    public URLsTest(String testName) {
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
    
    private boolean sendToURL(String URL, String file) throws Exception {
        
        boolean retVal=false;
        
        try {
            pool = startDB();
            URL url = new URL(URL);
            InputStream is = new BufferedInputStream( new FileInputStream(file) );
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is,os);
            is.close();
            os.flush();
            os.close();
            
            retVal=true; // no problems!
            
        } catch (Exception ex) {
            throw ex;
        }
        
        return retVal;
    }
    
    private boolean getFromURL(String URL, OutputStream os) throws Exception {
        
        boolean retVal=false;
        
        try {
            pool = startDB();
            URL url = new URL(URL);
            InputStream is = url.openConnection().getInputStream();
            copyDocument(is,os);
            
            os.close();
            is.close();
            retVal=true; // no problems!
            
        } catch (Exception ex) {
            throw ex;
            
        }
        
        return retVal;
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
    
    public void testURLToDB() {
        System.out.println("testURLToDB");
        
        try {
            boolean retVal = sendToURL(
                    "xmldb:exist:///db/build_testURLToDB.xml",
                    "build.xml" );
            
            assertTrue(retVal);
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }

    public void testURLFromDB() {
        System.out.println("testURLFromDB");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist:///db/build_testURLToDB.xml", os);
            os.flush();
            os.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }

    public void testURLToDB_notExistingCollection() {
        System.out.println("testURLToDB_notExistingCollection");
        try {
            boolean retVal = sendToURL("xmldb:exist:///db/foo/bar.xml",
                    "build.xml");
//            assertFalse(retVal);
            fail("Execption expected");

        } catch (Exception ex) {
            fail("Need to change this text"+ex.getMessage());
            LOG.error(ex);
        }
    }

    public void testURLFromDB_notExistingCollection() {
        System.out.println("testURLFromDB_notExistingCollection");
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist:///db/foo.bar", os);
            os.flush();
            os.close();
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches("Resource .* not found.")){
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }

    public void testURLToDB_NotExistingUser() {
        System.out.println("testURLToDB_NotExistingUser");
        try {
            boolean retVal = sendToURL("xmldb:exist:///db/testURLToDB_NotExistingUser.xml",
                    "build.xml");
            
            fail("Execption expected");
            assertFalse(retVal);

        } catch (Exception ex) {
            fail("Need to change this text"+ex.getMessage());
            LOG.error(ex);
        }
    }

    public void testURLFromDB_NotExistingUser() {
        System.out.println("testURLFromDB_NotExistingUser");
        
        try {
            OutputStream os = new ByteArrayOutputStream();
            getFromURL("xmldb:exist://foo:bar@/db/testURLFromDB_NotExistingUser.xml", os);
            
            os.flush();
            os.close();
            
            fail("Exception expected");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().contains("Unauthorized user")){
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
}
