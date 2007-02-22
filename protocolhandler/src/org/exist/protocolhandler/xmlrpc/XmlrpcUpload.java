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

package org.exist.protocolhandler.xmlrpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.apache.log4j.Logger;

import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.exist.io.BlockingInputStream;
import org.exist.io.ExistIOException;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURL;

/**
 * Write document using XMLRPC to remote database and read the data 
 * from an input stream.
 * 
 * Sends a document to an eXist-db server using XMLRPC. The document can be
 * either XML or non-XML (binary). Chunked means that the document is send 
 * as smaller parts to the server, the servler glues the parts together. There
 * is no limitation on the size of the documents that can be transported.
 *
 * @author Dannes Wessels
 */
public class XmlrpcUpload {
    
    private final static Logger LOG = Logger.getLogger(XmlrpcUpload.class);
    
    /**
     * Write data from a (input)stream to the specified XMLRPC url and leave
     * the input stream open.
     * 
     * @param xmldbURL URL pointing to location on eXist-db server.
     * @param is Document stream
     * @throws Exception When something is wrong.
     */
    private void streamDocument(XmldbURL xmldbURL, InputStream is)
    throws Exception {
        LOG.debug("Begin document upload");
        try {
            // Setup xmlrpc client
            XmlRpc.setEncoding("UTF-8");
            XmlRpcClient xmlrpc = new XmlRpcClient( xmldbURL.getXmlRpcURL() );
            
            if(xmldbURL.hasUserInfo()){
                xmlrpc.setBasicAuthentication(xmldbURL.getUsername(), xmldbURL.getPassword());
            }
            
            String contentType=MimeType.BINARY_TYPE.getName();
            MimeType mime
                    = MimeTable.getInstance().getContentTypeFor(xmldbURL.getDocumentName());
            if (mime != null){
                contentType = mime.getName();
            }
            
            // Initialize xmlrpc parameters
            Vector params = new Vector();
            String handle=null;
            
            // Copy data from inputstream to database
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) {
                params.clear();
                if(handle!=null){
                    params.addElement(handle);
                }
                params.addElement(buf);
                params.addElement(new Integer(len));
                handle = (String)xmlrpc.execute("upload", params);
            }
            
            // All data transported, now parse data on server
            params.clear();
            params.addElement(handle);
            params.addElement( xmldbURL.getCollectionPath() );
            params.addElement(new Boolean(true));
            params.addElement(contentType);
            Boolean result =(Boolean)xmlrpc.execute("parseLocal", params); // TODO which exceptions
            
            // Check XMLRPC result
            if(result.booleanValue()){
                LOG.debug("Document stored.");
            } else {
                LOG.debug("Could not store document.");
                throw new ExistIOException("Could not store document.");
            }
            
        } catch (Exception ex) {
            LOG.error(ex);
            throw ex;
        } finally {
           LOG.debug("Finished document upload");
        }
    }

    /**
     * Write data from an input stream to the specified XMLRPC url.
     * 
     * 
     * @param xmldbURL URL pointing to location on eXist-db server.
     * @param is Document input stream
     * @throws ExistIOException When something is wrong.
     */

    public void stream(XmldbURL xmldbURL, InputStream is) throws IOException {
        try {
           streamDocument(xmldbURL, is); 
        } catch (IOException ioex) {
            throw ioex;
        } catch (Exception ex) {
            throw new ExistIOException(ex.getMessage(), ex); //TODO
        } finally {
            is.close();
        }
    }

    /**
     * Write data from a <code>BlockingInputStream</code> to the specified
     * XMLRPC url.
     * 
     * @param xmldbURL URL pointing to location on eXist-db server.
     * @param bis Document stream
     */
    public void stream(XmldbURL xmldbURL, BlockingInputStream bis) {
        Exception exception = null; 
        try {
            streamDocument(xmldbURL, bis); 
        } catch (Exception ex) {
            exception = ex;
        } finally {
            bis.close(exception); // Pass the exception through the stream.
        }
    }    
}
