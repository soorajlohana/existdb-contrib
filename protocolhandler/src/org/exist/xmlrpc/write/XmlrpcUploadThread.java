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

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;

/**
 *  Writes data from an inputstream to a specified URL.
 *
 * @author Dannes Wessels
 */
public class XmlrpcUploadThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(XmlrpcUploadThread.class);
    private XmldbURL xmldbURL;
    private InputStream inputStream;
    private Exception exception;
    
    
    public XmlrpcUploadThread(XmldbURL url, InputStream is) {
        this.xmldbURL=url;
        this.inputStream=is;
    }
    
    /**
     * Start Thread.
     */
    public void run() {
        logger.debug("Thread started." );
        try {
            XmlrpcUpload xuc = new XmlrpcUpload();
            xuc.stream(xmldbURL, inputStream);
        } catch (Exception ex) {
            logger.error(ex);
            exception=ex;
        } finally {
            try { // not needed?
                inputStream.close();
            } catch (IOException ex) {
                logger.debug(ex);
                exception=ex;
            }
            logger.debug("Thread stopped." );
        }
        
    }
    
    public boolean isExceptionThrown(){
        return (exception!=null);
    }
    
    public Exception getThrownException(){
        return exception;
    }
    
}
