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

package org.exist.xmlrpc.read;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import org.exist.xmldb.XmldbURLStreamHandlerFactory;
import org.exist.xmldb.XmldbURL;

/**
 *  Example code for demonstrating XMLRPC methods getDocumentData
 * and getNextChunk. Please run 'admin-examples setup' first, this will
 * download the required mondial.xml document.
 *
 * @author dizzzz
 */
public class XmlrpcDownloadChunked {
    
    private final static Logger LOG = Logger.getLogger(XmlrpcDownloadChunked.class);
    
    public void stream(XmldbURL xmldbURL, OutputStream os) throws IOException {
        LOG.debug("Begin document download");
        try {
            // Setup client client
            XmlRpc.setEncoding("UTF-8");
            XmlRpcClient client = new XmlRpcClient( xmldbURL.getXmlRpcURL() );
            
            if(xmldbURL.hasUserInfo()){
                client.setBasicAuthentication(xmldbURL.getUsername(), xmldbURL.getPassword());
            }
            
            // Setup xml serializer
            Hashtable options = new Hashtable();
            options.put("indent", "no");
            options.put("encoding", "UTF-8");
            
            // Setup client parameters
            Vector params = new Vector();
            params.addElement( xmldbURL.getCollectionPath() );
            params.addElement( options );
            
            // Shoot first method write data
            Hashtable ht = (Hashtable) client.execute("getDocumentData", params);
            int offset = ((Integer)ht.get("offset")).intValue();
            byte[]data= (byte[]) ht.get("data");
            String handle = (String) ht.get("handle");
            os.write(data);
            
            // When there is more data to download
            while(offset!=0){
                // Clean and re-setup client parameters
                params.clear();
                params.addElement(handle);
                params.addElement(new Integer(offset));
                
                // Get and write next chunk
                ht = (Hashtable) client.execute("getNextChunk", params);
                data= (byte[]) ht.get("data");
                offset = ((Integer)ht.get("offset")).intValue();
                os.write(data);
            }
            
            // Finish transport
            os.close();
            
        } catch (XmlRpcException ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage());
            
        } catch (MalformedURLException ex){
            LOG.error(ex);
            throw new IOException(ex.getMessage());
            
        } finally {
            LOG.debug("Finished document download"); 
        }
    }
    
    public static void main(String[] args) {
        
        String url = "xmldb:exist://guest:guest@localhost:8080"
                +"/exist/xmlrpc/db/mondial/mondial.xml";
        
        // Setup
        URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
        BasicConfigurator.configure();
        XmlrpcDownloadChunked rc = new XmlrpcDownloadChunked();
        
        try {
            XmldbURL xmldbURL = new XmldbURL(url);
            rc.stream(xmldbURL, System.out);
            
        } catch (MalformedURLException ex) {
            LOG.error("Caught exception", ex);
            
        } catch (IOException ex) {
            LOG.error("Caught exception", ex);
        }
        
    }
    
    
}
