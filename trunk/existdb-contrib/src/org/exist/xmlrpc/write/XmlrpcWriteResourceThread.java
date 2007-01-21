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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.exist.protocols.Credentials;
import org.exist.protocols.Shared;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.XmldbURI;

/**
 *  Writes data from an inputstream to a specified URL
 * @author Dannes Wessels
 */
public class XmlrpcWriteResourceThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(XmlrpcWriteResourceThread.class);
    private XmldbURI docXmldbURI;
    private URL docURL;
    private InputStream inputStream;
    private Exception exception;
    private Credentials creds;
    
    
   // NEW
    public XmlrpcWriteResourceThread(URL url, InputStream is) {
        this.docURL=url;
        this.inputStream=is;
        creds =Shared.extractUserInfo(docURL.toString());
    }
    
    public XmlrpcWriteResourceThread(XmldbURI uri, InputStream is) {
        this.docXmldbURI=uri;
        this.inputStream=is;
        creds =Shared.extractUserInfo(uri.toString());
    }
    
    /**
     * Start Thread.
     */
    public void run() {
        logger.debug("Thread started." );
        streamResource( inputStream );
        logger.debug("Thread stopped." );
    }
    
    public boolean isExceptionThrown(){
        return (exception!=null);
    }
    
    public Exception getThrownException(){
        return exception;
    }
    
    private void streamResource( InputStream is ){
        
        // TODO
        // -cast URL to xmldb
        // -distill authority + context from xmldbUri
        // -distill collectionpath
        String xmlrpcURL = "http://" + docXmldbURI.getAuthority() + docXmldbURI.getContext();
        String collectionPath = docXmldbURI.getCollectionPath();
        
        // get mimetype
        String contentType=MimeType.BINARY_TYPE.getName();
        MimeType mime = MimeTable.getInstance().getContentTypeFor(docXmldbURI.toString());
        if (mime != null){
            contentType = mime.getName();
        }
        
        try {
            // Setup xmlrpc client
            XmlRpc.setEncoding("UTF-8");
            XmlRpcClient xmlrpc = new XmlRpcClient(xmlrpcURL);
            
            if(creds.username!=null){
                xmlrpc.setBasicAuthentication(creds.username, creds.password);
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
            
            // All data transported, parse data on server
            params.clear();
            params.addElement(handle);
            params.addElement(collectionPath);
            params.addElement(new Boolean(true));
            params.addElement(contentType);
            Boolean result =(Boolean)xmlrpc.execute("parseLocal", params); // exceptions
            
            // Check result
            if(result.booleanValue()){
                logger.debug("document stored.");
            } else {
                logger.debug("could not store document.");
                exception = new IOException("Could not store document");
            }
            
        } catch (MalformedURLException ex) {
            exception=ex;
            logger.error(ex);
            
        } catch (IOException ex) {
            exception=ex;
            logger.error(ex);
            
        } catch (XmlRpcException ex) {
            exception=ex;
            ex.printStackTrace();
            logger.error(ex);
            
        } catch (Exception ex) { // Catch all
            exception=ex;
            ex.printStackTrace();
            logger.error(ex);
            
        } finally {
            try {
                // nothing
                is.close();
            } catch (IOException ex) {
                exception=ex;
                ex.printStackTrace();
                logger.error(ex);
            }
        }
        
    }
    
}
