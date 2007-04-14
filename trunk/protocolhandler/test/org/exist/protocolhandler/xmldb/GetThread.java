/*
 * PutThread.java
 *
 * Created on April 10, 2007, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.protocolhandler.xmldb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author wessels
 */
public class GetThread implements Runnable {
    
    private static Logger LOG = Logger.getLogger(GetThread.class);
     
    URL url;
    
    int size=-1;
    
    Exception exception;
    
    /**
     * Creates a new instance of PutThread
     */
    public GetThread(URL url) {
        this.url=url;
    }
    
    public void run() {
        
        try {
            LOG.info("thread started");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream is = url.openConnection().getInputStream();
            
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            
            is.close();
            os.close();
            
            size=os.size();
            
        } catch (IOException ex) {
            LOG.error(ex);
            ex.printStackTrace();
            exception=ex;
            
        } finally {
            LOG.info("thread stopped");
        }
    }
    
}
