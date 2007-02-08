/*
 * EmbeddedUploadTest.java
 * JUnit based test
 *
 * Created on February 2, 2007, 8:49 PM
 */

package org.exist.embedded.write;

import java.io.FileInputStream;
import junit.framework.*;
import java.io.InputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.xmldb.XmldbURL;
import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 *
 * @author wessels
 */
public class EmbeddedUploadTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(EmbeddedUploadTest.class);
    
    private static boolean firstTime=true;
    
    public EmbeddedUploadTest(String testName) {
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
        //
    }
    

    /**
     * Test of stream method, of class org.exist.embedded.write.EmbeddedUpload.
     */
    public void bugtestStream() {
        System.out.println("testStream");
        try {
            
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build.xml");
            InputStream is = new FileInputStream("build.xml");
            EmbeddedUpload instance = new EmbeddedUpload();
            
            instance.stream(xmldbURL, is);
            
            is.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
            LOG.error(ex);
        }
        

    }
    
    public void testEMptytest() {

    }
    
}
