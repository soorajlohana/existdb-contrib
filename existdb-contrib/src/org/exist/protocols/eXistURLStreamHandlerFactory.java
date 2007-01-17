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

package org.exist.protocols;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.apache.log4j.Logger;
import org.exist.protocols.xmldb.Handler;

/**
 *
 * @author wessels
 */
public class eXistURLStreamHandlerFactory implements URLStreamHandlerFactory {
    
    private final static Logger LOG = Logger.getLogger(eXistURLStreamHandlerFactory.class);
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        
        System.out.println("createURLStreamHandler="+protocol);
        
        if("xmldb".equals(protocol)){
            return new Handler();
        } 
        
        return null;
            
    }
    
}
