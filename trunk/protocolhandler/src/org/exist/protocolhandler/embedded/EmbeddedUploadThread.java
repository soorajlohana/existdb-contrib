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

package org.exist.protocolhandler.embedded;


import java.io.IOException;
import org.apache.log4j.Logger;
import org.exist.protocolhandler.io.BlockingInputStream;
import org.exist.protocolhandler.xmldb.XmldbURL;

/**
 *  Wrap XmlrpcUpload class into a thread for XmlrpcOutputStream.
 *
 * @author Dannes Wessels
 */
public class EmbeddedUploadThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(EmbeddedUploadThread.class);
    private XmldbURL xmldbURL;
    private BlockingInputStream inputStream;
    private IOException exception;
    
    
    public EmbeddedUploadThread(XmldbURL url, BlockingInputStream is) {
        xmldbURL=url;
        inputStream=is;
    }
    
    /**
     * Start Thread.
     */
    public void run() {
        logger.debug("Thread started." );
        try {
            EmbeddedUpload uploader = new EmbeddedUpload();
            uploader.stream(xmldbURL, inputStream);
        } catch (IOException ex) {
            logger.error(ex);
            exception = ex;
        } finally {
            inputStream.close(exception);
            logger.debug("Thread stopped." );
        }
    }
}
