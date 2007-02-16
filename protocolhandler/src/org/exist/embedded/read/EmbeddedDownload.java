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

package org.exist.embedded.read;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.localcopied.IOException;
import org.exist.security.SecurityManager;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.serializers.Serializer;
import org.exist.xmldb.XmldbURI;
import org.exist.xmldb.XmldbURL;

/**
 *   Read document from an embedded database and write the data into an 
 * output stream.
 *
 * @author Dannes Wessels
 */
public class EmbeddedDownload {
    
    private final static Logger LOG = Logger.getLogger(EmbeddedDownload.class);
    
    /**
     *   Write document referred by URL to an (output)stream.
     *
     * @param xmldbURL Document location in database.
     * @param os Stream to which the document is written.
     * @throws IOException
     */
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
                // Test for collection
                collection = broker.openCollection(path, Lock.READ_LOCK);
                if(collection == null){
                    // No collection, no document
                    throw new IOException("Resource "+xmldbURL.getPath()+" not found.");
                    
                } else {
                    // Collection
                    throw new IOException("Resource "+xmldbURL.getPath()+" is a collection.");
                }
                
            } else {
                if(resource.getResourceType() == DocumentImpl.XML_FILE) {
                    Serializer serializer = broker.getSerializer();
                    serializer.reset();
                    
                    // TODO set properties? serializer.setProperties(WebDAV.OUTPUT_PROPERTIES);
                    Writer w = new OutputStreamWriter(os,"UTF-8");
                    serializer.serialize(resource,w);
                    w.flush();
                    w.close();
                    
                } else {
                    broker.readBinaryResource((BinaryDocument) resource, os);
                    os.flush();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage(), ex);
            
        } finally {
            if(resource != null)
                resource.getUpdateLock().release(Lock.READ_LOCK);
            
            if(collection != null)
                collection.release(Lock.READ_LOCK);
            
            pool.release(broker);
            
            LOG.debug("End document download");
        }
    }
}
