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
 *  $Id$
 */

package org.exist.protocols.xmldb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.exist.embedded.read.EmbeddedInputStream;
import org.exist.xmldb.XmldbURL;
import org.exist.xmlrpc.read.XmlrpcInputStream;
import org.exist.xmlrpc.write.XmlrpcOutputStream;

/**
 *  A URLConnection object manages the translation of a URL object into a
 * resource stream.
 *
 * @see <A HREF="http://java.sun.com/developer/onlineTraining/protocolhandlers/"
 *                                     >A New Era for Java Protocol Handlers</A>
 *
 * @author Dannes Wessels
 */
public class Connection extends URLConnection {
    
    private final static Logger LOG = Logger.getLogger(Connection.class);
    
    /**
     * Constructor
     */
    protected Connection(URL url) {
        super(url);
        LOG.info("Constructor "+url.toString() );
        
        // TODO check is this needed
        setDoInput(true);
        setDoOutput(true);
    }
    
    /**
     * @see java.net.URLConnection#connect
     */
    public void connect() throws IOException {
        LOG.info("connect "+url) ;
    }
    
    /**
     * @see java.net.URLConnection#getInputStream
     */
    public InputStream getInputStream() throws IOException {
        LOG.debug("getInputStream="+url) ;
        
        InputStream xis=null;
        XmldbURL xu = new XmldbURL(url);
        if(xu.isEmbedded()){
            xis = new EmbeddedInputStream( new XmldbURL(url) );
        } else {
//            try {
            xis = new XmlrpcInputStream( new XmldbURL(url) );
            
//            } catch (MalformedURLException ex) {
//                LOG.error(ex);
//                throw new IOException(ex.getMessage());
//            }
        }
        return xis;
    }
    
    
    /**
     * @see java.net.URLConnection#getOutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        LOG.debug("getOutputStream="+url) ;
        
        XmlrpcOutputStream xos=null;
        try {
            xos = new XmlrpcOutputStream( new XmldbURL(url) );
        } catch (MalformedURLException ex) {
            LOG.error(ex);
            throw new IOException(ex.getMessage());
        }
        return xos;
    }
}
