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
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author wessels
 */
public class XmlrpcWriteResourceThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(XmlrpcWriteResourceThread.class);
    private XmldbURI docUri;
    private InputStream inputStream;
    private String userInfo="guest:guest";
    private Exception exception;
    
    
    public XmlrpcWriteResourceThread(XmldbURI docUri, InputStream is) {
        this.docUri=docUri;
        this.inputStream=is;
    }
    
    /**
     * Start Thread.
     */
    public void run() {
        logger.debug("Start thread." );
        streamResource( inputStream );
    }
    
    public boolean isExceptionThrown(){
        return (exception!=null);
    }
    
    public Exception getThrownException(){
        return exception;
    }
    
    private void streamResource( InputStream is ){
        
        String url = "http://"+userInfo+"@" + docUri.getAuthority() + docUri.getContext();
        String path = docUri.getCollectionPath();
        
        try {
            // Setup xmlrpc client
            XmlRpc.setEncoding("UTF-8");
            XmlRpcClient xmlrpc = new XmlRpcClient(url);
            xmlrpc.setBasicAuthentication("guest", "guest"); // TODO
            
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
            params.addElement(path);
            params.addElement(new Boolean(true));
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
