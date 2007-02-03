/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 The eXist Project
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
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURL;

import org.exist.xmldb.XmldbURLStreamHandlerFactory;

/**
 *  Example code for demonstrating XMLRPC methods upload
 * and parseLocal. Please run 'admin-examples setup' first, this will
 * download the required mondial.xml document.
 *
 * @author dizzzz
 */
public class XmlrpcUploadChunked {
    
    private final static Logger LOG = Logger.getLogger(XmlrpcUploadChunked.class);
    
    public void stream(XmldbURL xmldbURL, InputStream is) throws IOException {
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
            is.close();
            
            // All data transported, parse data on server
            params.clear();
            params.addElement(handle);
            params.addElement( xmldbURL.getCollectionPath() );
            params.addElement(new Boolean(true));
            params.addElement(contentType);
            Boolean result =(Boolean)xmlrpc.execute("parseLocal", params); // TODO which exceptions
            
            // Check result
            if(result.booleanValue()){
                LOG.debug("Document stored.");
                
            } else {
                LOG.debug("Could not store document.");
                throw new IOException("Could not store document.");
            }
            
        } catch (MalformedURLException ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage());
            
        } catch (XmlRpcException ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage());
            
        } finally {
           LOG.debug("Finished document upload");
        }
    }
    
    public static void main(String[] args) {
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/foobar/build.xml";
        
        // Setup
        URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
        BasicConfigurator.configure();
        XmlrpcUploadChunked xuc = new XmlrpcUploadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            xuc.stream(xmldbURL, new FileInputStream("build.xml"));
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception"+url, ex);
            
        } catch (IOException ex) {
            LOG.error("Caught exception", ex);
            
        }
        
    }
}
