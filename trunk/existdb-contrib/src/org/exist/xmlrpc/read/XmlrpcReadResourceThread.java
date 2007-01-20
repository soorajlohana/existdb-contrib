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
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.exist.protocols.Credentials;
import org.exist.protocols.Shared;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author wessels
 */
public class XmlrpcReadResourceThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(XmlrpcReadResourceThread.class);
    private XmldbURI docUri;
    private OutputStream outputStream;
    private Exception exception=null;
    private Credentials creds=null;
    
    public XmlrpcReadResourceThread(XmldbURI docUri, OutputStream os) {
        this.docUri=docUri;
        this.outputStream=os;
        
        creds =Shared.extractUserInfo(docUri.toString());
    }
    
    /**
     * Start Thread.
     */
    public void run() {
        logger.debug("Start thread." );
        streamResource( outputStream );
    }
    
    public boolean isExceptionThrown(){
        return (exception!=null);
    }
    
    public Exception getThrownException(){
        return this.exception;
    }
    
    /**
     *   Serialize XML document to Writer object.
     *
     * @param writer Object that receives the serialized data.
     */
    private void streamResource( OutputStream os ) {
        
        String url = "http://" + docUri.getAuthority() + docUri.getContext();
        String path = docUri.getCollectionPath();
        
        try {
            // Setup xmlrpc client
            XmlRpc.setEncoding("UTF-8");
            XmlRpcClient xmlrpc = new XmlRpcClient(url);
            
            if(creds.username!=null){
                xmlrpc.setBasicAuthentication(creds.username, creds.password);
            }
            
            // Setup xml serializer
            Hashtable options = new Hashtable();
            options.put("indent", "no");
            options.put("encoding", "UTF-8");
            
            // Setup xmlrpc parameters
            Vector params = new Vector();
            params.addElement( path );
            params.addElement( options );
            
            // Shoot first method write data
            Hashtable ht = (Hashtable) xmlrpc.execute("getDocumentData", params);
            int offset = ((Integer)ht.get("offset")).intValue();
            byte[]data= (byte[]) ht.get("data");
            String handle = (String) ht.get("handle");
            os.write(data);
            
            // When there is more data to download
            while(offset!=0){
                // Clean and re-setup xmlrpc parameters
                params.clear();
                params.addElement(handle);
                params.addElement(new Integer(offset));
                
                // Get and write next chunk
                ht = (Hashtable) xmlrpc.execute("getNextChunk", params);
                data= (byte[]) ht.get("data");
                offset = ((Integer)ht.get("offset")).intValue();
                os.write(data);
            }
            
            // Finish transport
            os.flush();
//            os.close(); // DWES this should not be here!
            
        } catch (MalformedURLException ex) {
            logger.error(ex);
            exception=ex;
        } catch (XmlRpcException ex) {
            ex.printStackTrace();
            logger.error(ex);
            exception=ex;
        } catch (IOException ex) {
            logger.error(ex);
            exception=ex;
        } catch (Exception ex){
            logger.error(ex);
            exception=ex;
            
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                exception=ex;;
            }
        }
    }
    
}
