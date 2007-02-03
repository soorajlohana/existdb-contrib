/*
 * EmbeddedDownload.java
 *
 * Created on February 2, 2007, 5:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.embedded.read;



import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.security.SecurityManager;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.serializers.Serializer;
import org.exist.xmldb.XmldbURI;
import org.exist.xmldb.XmldbURL;
import org.xml.sax.SAXException;

/**
 *
 * @author wessels
 */
public class EmbeddedDownload {
    
    private final static Logger LOG = Logger.getLogger(EmbeddedDownload.class);
    
    public void stream(XmldbURL xmldbURL, OutputStream os) throws IOException {
        LOG.debug("Begin document download");
        
        DocumentImpl resource = null;
        Collection collection = null;
        BrokerPool pool =null;
        DBBroker broker =null;
        try {
            XmldbURI path = XmldbURI.create(xmldbURL.getPath());
            pool = BrokerPool.getInstance();
            broker = pool.get(SecurityManager.SYSTEM_USER);
            resource = broker.getXMLResource(path, Lock.READ_LOCK);
            
            if(resource == null) {
                // Directory
                collection = broker.openCollection(path, Lock.READ_LOCK);
                if(collection == null){
                    // not found
                    throw new IOException("Resource "+xmldbURL.getPath()+" not found.");
                    
                } else {
                    //collection
                    throw new IOException("Resource "+xmldbURL.getPath()+" is a collection.");
                }
            } else {
                if(resource.getResourceType() == DocumentImpl.XML_FILE) {
                    Serializer serializer = broker.getSerializer();
                    serializer.reset();
                    try {
                        // TODO set properties?
                        //serializer.setProperties(WebDAV.OUTPUT_PROPERTIES);
                        Writer w = new OutputStreamWriter(os,"UTF-8");
                        serializer.serialize(resource,w);
                        w.flush();
                        w.close();
                        
                    } catch (SAXException e) {
                        LOG.error(e);
                    }
                    
                } else {
                    broker.readBinaryResource((BinaryDocument) resource, os);
                    os.flush();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage());
        } finally {
            if(resource != null)
                resource.getUpdateLock().release(Lock.READ_LOCK);
            
            if(collection != null)
                collection.release();
            
            pool.release(broker);
        }
    }    
}
