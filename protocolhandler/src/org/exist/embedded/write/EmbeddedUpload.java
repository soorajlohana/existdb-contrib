/*
 * EmbeddedUpload.java
 *
 * Created on February 2, 2007, 7:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.embedded.write;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.dom.DocumentImpl;
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
        LOG.debug("Begin document upload");
        
        DocumentImpl resource = null;
        Collection collection = null;
        BrokerPool pool =null;
        DBBroker broker =null;
        
        boolean collectionLocked = true;
        TransactionManager transact = pool.getTransactionManager();
        Txn txn = transact.beginTransaction();
        
        try {
            
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
                InputSource inputsource = new InputSource(is);
                IndexInfo info = collection.validateXMLResource(txn, broker, documentUri, inputsource);
                doc = info.getDocument();
                doc.getMetadata().setMimeType(contentType);
                collection.release();
                collectionLocked = false;
                collection.store(txn, broker, info, inputsource, false);
                LOG.debug("done");
            } else {
                LOG.debug("storing Binary resource");
                // to check -1
                doc = collection.addBinaryResource(txn, broker, documentUri, is, contentType, -1);
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
