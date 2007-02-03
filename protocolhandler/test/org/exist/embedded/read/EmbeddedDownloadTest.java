/*
 * EmbeddedDownloadTest.java
 * JUnit based test
 *
 * Created on February 2, 2007, 8:42 PM
 */

package org.exist.embedded.read;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
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
 * @author wessels
 */
public class EmbeddedDownloadTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedDownloadTest.class);
    
    private static boolean firstTime=true;
    
    public EmbeddedDownloadTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            BasicConfigurator.configure();
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
    
    /**
     * Test of stream method, of class org.exist.embedded.read.EmbeddedDownload.
     */
    public void testGetDocumentEmbedded() {
        System.out.println("testGetDocumentEmbedded");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            broker = pool.get(SecurityManager.SYSTEM_USER);
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/users.xml");
            OutputStream os = new FileOutputStream("out.xml");
            EmbeddedDownload instance = new EmbeddedDownload();
            
            instance.stream(xmldbURL, os);
            os.flush();
            os.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    public void testGetDocumentEmbeddedInputStream() {
        System.out.println("testGetDocumentEmbeddedInputStream");
        BrokerPool pool = null;
        DBBroker broker = null;
        
        try {
            pool = startDB();
            broker = pool.get(SecurityManager.SYSTEM_USER);
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/users.xml");
            OutputStream os = new FileOutputStream("out2.xml");
            
            getDocument(xmldbURL,os);
            

            os.flush();
            os.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        } finally {
            pool.release(broker);
        }
    }
    
    // Copy document from URL to outputstream
    private void getDocument(XmldbURL xmldbUrl, OutputStream os) throws IOException{
        
        EmbeddedInputStream is = new EmbeddedInputStream(xmldbUrl);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        
        is.close(); 
    }
    
}
