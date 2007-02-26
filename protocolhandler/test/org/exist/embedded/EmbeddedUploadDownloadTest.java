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
import org.exist.EXistException;
import org.exist.protocolhandler.embedded.EmbeddedDownload;
import org.exist.protocolhandler.embedded.EmbeddedInputStream;
import org.exist.protocolhandler.embedded.EmbeddedUpload;
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
    
    private static Logger LOG = Logger.getLogger(URLsTest.class);
    
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
    
    public void testEmbeddedUploadToDB() {
        System.out.println("testEmbeddedUploadToDB");
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
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    public void testEmbeddedDownloadFromDB() {
        System.out.println("testEmbeddedDownloadFromDB");
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
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    public void testEmbeddedDownloadFromDB_NotExistingDocument() {
        System.out.println("testEmbeddedDownloadFromDB_NotExistingDocument");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();

            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/testEmbeddedDownloadFromDB_NotExistingDocument.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches("Resource .* not found.")){
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
    public void testEmbeddedDownloadFromDB_NotExistingCollection() {
        System.out.println("testEmbeddedDownloadFromDB_NotExistingCollection");
        BrokerPool pool = null;
        DBBroker broker = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            pool = startDB();

            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/foobar/testEmbeddedDownloadFromDB_NotExistingCollection.xml");
            
            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);
            
            os.flush();
            os.close();
            
            assertTrue( os.size()>0 );
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches("Resource .* not found.")){
                fail(ex.getMessage());
                LOG.error(ex);
            }
        } finally {
            pool.release(broker);
        }
    }
    
//    public void testGetStoredDocumentUsingEmbeddedInputStream() {
//        System.out.println("testGetStoredDocumentUsingEmbeddedInputStream");
//        BrokerPool pool = null;
//        DBBroker broker = null;
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        
//        try {
//            pool = startDB();
//            
//            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build.xml");
//            
//            getDocument(xmldbURL,os);
//            
//            os.flush();
//            os.close();
//            
//            assertTrue( os.size()>0 );
//            
//        } catch (Exception ex) {
//            fail(ex.getMessage());
//            LOG.error(ex);
//            
//        } finally {
//            pool.release(broker);
//        }
//    }
    
//    
//    public void testGetStoredDocumentUsingEmbeddedInputStream_notexisting() {
//        System.out.println("testGetStoredDocumentUsingEmbeddedInputStream");
//        BrokerPool pool = null;
//        DBBroker broker = null;
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        
//        try {
//            pool = startDB();
//            broker = pool.get(SecurityManager.SYSTEM_USER);
//            
//            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build_foobar.xml");
//            
//            getDocument(xmldbURL,os);
//            
//            os.flush();
//            os.close();
//            
//            assertTrue( os.size()>0 );
//            
//        } catch (Exception ex) {
//            fail(ex.getMessage());
//            LOG.error(ex);
//            
//        } finally {
//            pool.release(broker);
//        }
//    }
//    
//    public void bugtestWriteDocumentUsingEmbeddedOutputStream() {
//        //TODO : testWriteDocumentUsingEmbeddedOutputStream
//    }
    
    public void testCleanUp(){
        try {
             //BrokerPool.stopAll(true);
            BrokerPool.stop();
        } catch (EXistException ex) {
            ex.printStackTrace();
        }
    }
    
//    // Copy document from URL-inputstream to outputstream
//    private void getDocument(XmldbURL xmldbUrl, OutputStream os) throws IOException{
//        
//        EmbeddedInputStream is = new EmbeddedInputStream(xmldbUrl);
//        
//        // Transfer bytes from in to out
//        byte[] buf = new byte[4096];
//        int len;
//        while ((len = is.read(buf)) > 0) {
//            os.write(buf, 0, len);
//        }
//        os.close();
//        is.close();
//    }
    
//    // Copy document from inputstream to URL-outputstream
//    private void putDocument(XmldbURL xmldbUrl, InputStream is) throws IOException{
//
//        EmbeddedOutputStream os = new EmbeddedOutputStream(xmldbUrl);
//
//        // Transfer bytes from in to out
//        byte[] buf = new byte[4096];
//        int len;
//        while ((len = is.read(buf)) > 0) {
//            os.write(buf, 0, len);
//        }
//
//        os.close();
//    }
    
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
