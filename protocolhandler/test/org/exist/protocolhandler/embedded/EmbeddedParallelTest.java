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
 * $Id: EmbeddedURLsTest.java 191 2007-03-30 15:49:34Z dizzzz $
 */

package org.exist.protocolhandler.embedded;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.GetThread;
import org.exist.protocolhandler.xmldb.PutThread;
import org.exist.storage.BrokerPool;
import org.exist.util.Configuration;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

/**
 *
 * @author wessels
 */
public class EmbeddedParallelTest extends TestCase {
    
    public EmbeddedParallelTest(String testName) {
        super(testName);
    }
    
    private static Logger LOG = Logger.getLogger(EmbeddedParallelTest.class);
    
    private static BrokerPool pool;
    
    private static boolean firstTime=true;
    
    
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
        os.close();
        
    }
    
    private void getFromURL(String URL, OutputStream os) throws Exception {
        
        pool = startDB();
        URL url = new URL(URL);
        InputStream is = url.openConnection().getInputStream();
        copyDocument(is,os);
        
        is.close();
        os.close();
        
    }
    
    // Transfer bytes from inputstream to outputstream
    private void copyDocument(InputStream is, OutputStream os) throws IOException{
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }
    
    // ======================================================================
    
    public void testEmbeddedParallelUpload(){
        System.out.println(this.getName());
        File file=new File("samples/shakespeare/r_and_j.xml");
        try {
            pool = startDB();
            for(int i=0; i<10 ; i++){
                LOG.info("Firing URL "+i);
                URL url = new URL("xmldb:exist:///db/r_and_j-"+i+".xml");
                
                PutThread ct = new PutThread(file, url);
                Thread thread = new Thread(ct);
                
                // Start the thread
                thread.start();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    public void testEmbeddedParallelDownload(){
        System.out.println(this.getName());
        try {
            pool = startDB();
            for(int i=0; i<10 ; i++){
                LOG.info("Firing URL "+i);
                URL url = new URL("xmldb:exist:///db/r_and_j-"+i+".xml");
                
                GetThread ct = new GetThread(url);
                Thread thread = new Thread(ct);
                
                // Start the thread
                thread.start();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
}
