/*
 * EmbeddedUpload.java
 *
 * Created on February 2, 2007, 7:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.embedded.write;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.dom.DocumentImpl;
import org.exist.security.SecurityManager;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURI;
import org.exist.xmldb.XmldbURL;
import org.xml.sax.InputSource;

/**
 *
 * @author Dannes Wessels
 */
public class EmbeddedUpload {
    
    private final static Logger LOG = Logger.getLogger(EmbeddedUpload.class);
    
    public void stream(XmldbURL xmldbURL, InputStream is) throws IOException {
        File tmp =null;
        try{
            tmp = File.createTempFile("EMBEDDED", "tmp");
            FileOutputStream fos = new FileOutputStream(tmp);
            
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            is.close();
            fos.close();
            
            stream(xmldbURL, tmp);
        } finally {
            if(tmp!=null){
                tmp.delete();
            }
        }
    }
    
    public void stream(XmldbURL xmldbURL, File tmp) throws IOException {
        LOG.debug("Begin document upload");
        
        DocumentImpl resource = null;
        Collection collection = null;
        BrokerPool pool =null;
        DBBroker broker =null;
        TransactionManager transact = null;
        Txn txn = null;
        
        boolean collectionLocked = true;
        
        
        try {
            pool = BrokerPool.getInstance();
            broker = pool.get(SecurityManager.SYSTEM_USER);
            
            transact = pool.getTransactionManager();
            txn = transact.beginTransaction();
            
            XmldbURI collectionUri = XmldbURI.create(xmldbURL.getCollection());
            XmldbURI documentUri = XmldbURI.create(xmldbURL.getDocumentName());
            
            collection = broker.openCollection(collectionUri, Lock.READ_LOCK);
            
            if(collection == null) {
                transact.abort(txn);
                throw new IOException("Resource "+collectionUri.toString()+" is not a collection.");
            }
            
            if(collection.hasChildCollection(documentUri)) {
                transact.abort(txn);
                throw new IOException("Resource "+documentUri.toString()+" is a collection.");
            }
            
            MimeType mime = MimeTable.getInstance().getContentTypeFor(documentUri);
            String contentType=null;
            if (mime != null){
                contentType = mime.getName();
            } else {
                mime = MimeType.BINARY_TYPE;
            }
            LOG.debug(mime.toString());
            
            DocumentImpl doc = null;
            if(mime.isXMLType()) {
                LOG.debug("storing XML resource");
                
                InputSource inputsource = new InputSource(tmp.toURI().toASCIIString());
                IndexInfo info = collection.validateXMLResource(txn, broker, documentUri, inputsource);
                doc = info.getDocument();
                doc.getMetadata().setMimeType(contentType);
                collection.release();
                collectionLocked = false;
                collection.store(txn, broker, info, inputsource, false);
                LOG.debug("done");
            } else {
                LOG.debug("storing Binary resource");
                InputStream is = new FileInputStream(tmp);
                doc = collection.addBinaryResource(txn, broker, documentUri, is, contentType, (int) tmp.length());
                is.close();
                LOG.debug("done");
            }
            
            LOG.debug("commit");
            transact.commit(txn);
            
        } catch (Exception e) {
            transact.abort(txn);
            LOG.debug(e);
            throw new IOException(e.getMessage());
            
        } finally {
            LOG.debug("Done.");
            if(collectionLocked && collection != null)
                collection.release();
            pool.release(broker);
            
        }
        
    }
    
}
