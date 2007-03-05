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
import org.exist.EXistException;
import org.exist.security.SecurityManager;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.util.Configuration;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 *
 * @author Dannes Wessels
 */
public class EmbeddedUploadDownloadTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedURLsTest.class);
    
    private static boolean firstTime=true;
    
    public EmbeddedUploadDownloadTest(String testName) {
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
//        BrokerPool.stop();
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
            e.printStackTrace();
            fail(e.getMessage());
        }
        return null;
    }
    
    
    public void testToDB() {
        System.out.println("testToDB");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build_testEmbeddedUploadToDB.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    public void testFromDB() {
        System.out.println("testFromDB");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build_testEmbeddedUploadToDB.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
//ToDB_NotExistingCollection
    public void testToDB_NotExistingCollection() {
        System.out.println("testToDB_NotExistingCollection");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/foobar/testToDB_NotExistingCollection.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Resource /db/foobar is not a collection.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//FromDB_NotExistingCollection
    public void testFromDB_NotExistingCollection() {
        System.out.println("testFromDB_NotExistingCollection");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/foobar/testToDB_NotExistingCollection.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Resource .* not found.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//ToDB_NotExistingUser
    public void testToDB_NotExistingUser() {
        System.out.println("testToDB_NotExistingUser");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist://foo:bar@/db/testToDB_NotExistingUser.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            if(!ex.getCause().getMessage().matches(".*Unauthorized .* foo.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//FromDB_NotExistingUser
    public void testFromDB_NotExistingUser() {
        System.out.println("testFromDB_NotExistingUser");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist://foo:bar@/db/foobar/testFromDB_NotExistingUser.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Unauthorized .* foo.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//ToDB_NotAuthorized
    public void testToDB_NotAuthorized() {
        System.out.println("testToDB_NotAuthorized");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/testToDB_NotAuthorized.xml");
            InputStream is = new BufferedInputStream( new FileInputStream("build.xml") );
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();
            
        } catch (Exception ex) {
            
            if(!ex.getCause().getMessage().matches(".*User .* not allowed to write to collection.*")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//FromDB_NotAuthorized
    public void testFromDB_NotAuthorized() {
        System.out.println("testFromDB_NotAuthorized");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/users.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Permission denied to read collection .*")){
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
    
    public void testCleanUp(){
        BrokerPool.stopAll(false);
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
