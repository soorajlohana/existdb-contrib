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
 *  $Id$
 */

package org.exist.protocols.xmldb;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;

/**
 *  A stream protocol handler knows how to make a connection for a particular
 * protocol type. This handler deals with "xmldb:"
 *
 * @author Dannes Wessels
 *
 * @see <A HREF="http://java.sun.com/developer/onlineTraining/protocolhandlers/"
 *                                     >A New Era for Java Protocol Handlers</A>
 */
public class Handler extends URLStreamHandler {
    
    
    private final static Logger LOG = Logger.getLogger(Handler.class);
    
    /**
     * Creates a new instance of Handler
     */
    public Handler() {
        // --
        LOG.debug("Setup \"xmldb:exist:\" handler");
    }
    
    // TODO: check exist:foobar:// as well !
    protected void parseURL(URL url, String spec, int start, int limit) {
        LOG.debug("Parsing URL "+spec+" "+ start+" "+limit);
        
        
//        if(spec.startsWith("xmldb:exist:///")){
//            spec = "xmldb:exist://"
//            
//        } else 
            
            if(spec.startsWith("xmldb:exist://")){
            LOG.debug("Parsing xmldb:exist:// URL.");
            super.parseURL(url, spec, 12, limit);
            
        } else if(spec.startsWith("xmldb://")) {
            LOG.debug("Parsing xmldb:// URL.");
            super.parseURL(url, spec, 6, limit);
            
        } else {
            LOG.error("Expected xmldb URL, found "+spec);
            super.parseURL(url, spec, start, limit);
        }
        
    }
    
    
    protected URLConnection openConnection(URL u) throws IOException {
        return new Connection(u);
    }
    
}
