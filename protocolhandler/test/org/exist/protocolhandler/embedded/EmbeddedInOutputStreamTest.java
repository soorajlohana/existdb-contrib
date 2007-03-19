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
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 *  Testsuite for performing tests on EmbeddedInputstream / EmbeddedOutputStream
 *
 * @author Dannes Wessels
 */
public class EmbeddedInOutputStreamTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedInOutputStreamTest.class);
    
    private static boolean firstTime=true;
    
    private static BrokerPool pool;
    
    protected void setUp() throws Exception {
        if(firstTime){
            PropertyConfigurator.configure("log4j.conf");
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            firstTime=false;
        }
        pool = startDB();
    }
    
    protected void tearDown() throws Exception {
        //
        BrokerPool.stopAll(false);
    }
    
    public EmbeddedInOutputStreamTest(String testName) {
        super(testName);
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
    
    private void sendDocument(XmldbURL uri, InputStream is) throws IOException{
        
        // Setup
        EmbeddedOutputStream eos = new EmbeddedOutputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            eos.write(buf, 0, len);
        }
        
        // Shutdown
        eos.flush();
        eos.close();
    }
    
    private void getDocument(XmldbURL uri, OutputStream os) throws IOException{
        
        // Setup
        EmbeddedInputStream eis = new EmbeddedInputStream(uri);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = eis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        
        // Shutdown
        os.flush();
        os.close();
        eis.close(); // required; checks wether all is OK
        
    }
    
    // ===================================
    
    
//testToDB
    public void testToDB() {
        System.out.println("testToDB");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/build_embedded_testToDB.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
//testFromDB
    public void testtestFromDB() {
        System.out.println("testFromDB");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/build_embedded_testToDB.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
    
//testToDB_NotExistingCollection
    public void testToDB_NotExistingCollection() {
        System.out.println("testToDB_NotExistingCollection");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/foobar/build_embedded_testToDB_NotExistingCollection.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            //fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
//testFromDB_NotExistingCollection
    public void testFromDB_NotExistingCollection() {
        System.out.println("testFromDB_NotExistingCollection");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/foobar/build_embedded_testToDB_NotExistingCollection.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Resource .* not found.")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
//testToDB_NotExistingUser
    public void testToDB_NotExistingUser() {
        System.out.println("testToDB_NotExistingUser");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://foo:bar@/db/build_embedded_testToDB_testToDB_NotExistingUser.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            
            fail("Exception expected, not existing user.");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            //fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
//testFromDB_NotExistingUser
    public void testFromDB_NotExistingUser() {
        System.out.println("testFromDB_NotExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://foo:bar@/db/build_embedded_testToDB_testToDB_NotExistingUser.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Unauthorized user foo")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
//testToDB_NotAuthorized
    public void testToDB_NotAuthorized() {
        System.out.println("testToDB_NotAuthorized");
        try{
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/system/users.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            
            fail("Exception expected, not authorized user.");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            //fail(ex.getMessage());
            LOG.error(ex);
        }
    }
    
//testFromDB_NotAuthorized
    public void testFromDB_NotAuthorized() {
        System.out.println("testFromDB_NotAuthorized");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/system/users.xml";
        
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            
            fail("Exception expected, not existing collection.");
            
        } catch (Exception ex) {
            if(!ex.getCause().getMessage().matches(".*Permission denied to read collection '/db/system'")){
                ex.printStackTrace();
                fail(ex.getMessage());
                LOG.error(ex);
            }
        }
    }
    
}
