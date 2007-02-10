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

package org.exist.xmlrpc.write;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.exist.localcopied.ExistIOException;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURL;

import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 *  Write document using XMLRPC to remote database and read the data 
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
     *  Write data from a (input)stream to the specified XMLRPC url.
     * 
     * @param xmldbURL URL pointing to location on eXist-db server.
     * @param is Document stream
     * @throws org.exist.localcopied.ExistIOException When something is wrong.
     */
    public void stream(XmldbURL xmldbURL, InputStream is) throws ExistIOException {
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
            is.close(); // DWES is this needed there
            
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
            throw new ExistIOException(ex.getMessage(), ex); // need to fill message
                        
        } finally {
           LOG.debug("Finished document upload");
        }
    }
    
}
