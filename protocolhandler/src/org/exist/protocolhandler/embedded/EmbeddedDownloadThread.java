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
import org.exist.io.BlockingOutputStream;
import org.exist.xmldb.XmldbURL;

/**
 *  Wrap EmbeddedDownload class into a thread for EmbeddedInputStream.
 *
 * @author Dannes Wessels
 */
public class EmbeddedDownloadThread extends Thread {
    
    private final static Logger logger = Logger.getLogger(EmbeddedDownloadThread.class);
    
    private XmldbURL xmldbURL;
    private BlockingOutputStream outputStream;
    private IOException exception;
    
    
    /**
     *  Constructor of EmbeddedDownloadThread.
     * 
     * @param xmldbURL Document location in database.
     * @param os Stream to which the document is written.
     */
    public EmbeddedDownloadThread(XmldbURL url, BlockingOutputStream os) {
        xmldbURL = url;
        outputStream = os;
    }
    
    /**
     * Write resource to the output stream.
     */
    public void run() {
        logger.debug("Thread started." );
        try {
            EmbeddedDownload ed = new EmbeddedDownload();
            ed.stream(xmldbURL, outputStream);
            
        } catch (IOException ex) {
            logger.error(ex);
            exception = ex;
            
        } finally {
            try { // NEEDED!
                outputStream.close(exception);
            } catch (IOException ex) {
                logger.debug(ex);
            }
            logger.debug("Thread stopped." );
        }
    }
    
    /** COFF: @@@ can be removed!!
     *  Check if an exception is thrown during processing.
     *
     * @return TRUE when exception is thown in thread
     */
//**    public boolean isExceptionThrown(){
//**        return (exception!=null);
//**    }
    
    /** COFF: @@@ can be removed!!
     *  Get thrown processing exception.
     *
     * @return Exception that is thrown during processing, NULL if not available.
     */
//**    public Exception getThrownException(){
//**        return exception;
//**    }
    
}
