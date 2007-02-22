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
import org.exist.security.User;
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
 *   Read a document from a (input)stream and write it into database.
 *
 * @author Dannes Wessels
 */
public class EmbeddedUpload {
    
    private final static Logger LOG = Logger.getLogger(EmbeddedUpload.class);
    
    private User authenticate(XmldbURL xmldbURL, BrokerPool pool){
        
        if(!xmldbURL.hasUserInfo()){
            return null;
        }
        
        SecurityManager secman = pool.getSecurityManager();
        User user = secman.getUser(xmldbURL.getUsername());
        if(user == null) {
            return null;
        }
        if (!user.validate(xmldbURL.getPassword())) {
            return null;
        }
        
        return user;
    }
    
    /**
     *  Read document from stream and write data to database.
     *
     * @param xmldbURL Location in database.
     * @param is  Stream containing document.
     * @throws IOException Thrown when something is wrong.
     */
    public void stream(XmldbURL xmldbURL, InputStream is) throws IOException {
        // DWES: no existIOException?
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
    
    /**
     *  Read document and write data to database.
     * @param xmldbURL Location in database.
     * @param tmp Document that is inserted.
     * @throws org.exist.localcopied.ExistIOException Thrown when something is wrong.
     */
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
            
            User user=authenticate(xmldbURL, pool);
            if(user==null){
                broker = pool.get(pool.getSecurityManager().getUser(SecurityManager.GUEST_USER));
            } else {
                broker = pool.get(user);
            }
            
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
                collection.release(Lock.READ_LOCK);
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
            throw new IOException(e.getMessage(), e);
            
        } finally {
            LOG.debug("Done.");
            if(collectionLocked && collection != null)
                collection.release(Lock.READ_LOCK);
            pool.release(broker);
            
        }
        
    }
    
}
