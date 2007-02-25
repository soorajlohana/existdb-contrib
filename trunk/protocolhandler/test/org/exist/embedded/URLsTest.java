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
import org.exist.storage.DBBroker;
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
        BrokerPool.stop();
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
    
    // Actual tests
    public void testURLToDB() {
        System.out.println("testURLToDB");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            URL url = new URL("xmldb:exist:///db/build_testURLToDB.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is,os);
            is.close();
            os.close();
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    public void testURLFromDB() {
        System.out.println("testURLFromDB");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            URL url = new URL("xmldb:exist:///db/build_testURLToDB.xml");
            InputStream is = url.openConnection().getInputStream();
            OutputStream os = new ByteArrayOutputStream();
            copyDocument(is,os);
            is.close();
            os.close();
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
            
        } finally {
            pool.release(broker);
        }
    }
    
    // must fail
    public void testURLToDB_notExistingCollection() {
        System.out.println("testURLToDB_notExistingCollection");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            URL url = new URL("xmldb:exist:///db/foobar/testURLToDB_notExistingCollection.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            OutputStream os = url.openConnection().getOutputStream();
            copyDocument(is,os);
            is.close();
            os.close();
            
            fail("Execption expected");
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    // must fail
    public void testURLFromDB_NotExistingUser() {
        System.out.println("testURLFromDB_NotExistingUser");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            URL url = new URL("xmldb:exist://foo:bar@/db/build_testURLToDB.xml");
            InputStream is = url.openConnection().getInputStream();
            OutputStream os = new ByteArrayOutputStream();
            copyDocument(is,os);
            is.close();
            os.close();
            
            fail("Exception expected");
            
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
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
