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
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;
import org.exist.localcopied.BlockingInputStream;

/**
 *
 * @author wessels
 */
public class XmlrpcOutputStream  extends OutputStream {
    
    
    private final static Logger logger = Logger.getLogger(XmlrpcOutputStream.class);
    private BlockingInputStream bis;
    private OutputStream bos;
    private XmlrpcWriteResourceThread rt; 
    
    public XmlrpcOutputStream(XmldbURL uri) {
        
        logger.debug("Initializing XmlrpcOutputStream");
        
        bis = new BlockingInputStream();
        bos = bis.getOutputStream();
        
        rt = new XmlrpcWriteResourceThread(uri, bis);
        
        rt.start();
        
        logger.debug("Initializing XmlrpcOutputStream done");
        
    }

    public void write(int b) throws IOException {
        if(rt.isExceptionThrown())
        {
            logger.error(rt.getThrownException());
            throw new IOException(rt.getThrownException());
        }
                
        bos.write(b);
    }

    public void write(byte[] b) throws IOException {
        if(rt.isExceptionThrown())
        {
            logger.error(rt.getThrownException());
            throw new IOException(rt.getThrownException());
        }

        bos.write(b,0,b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if(rt.isExceptionThrown())
        {
            logger.error(rt.getThrownException());
            throw new IOException(rt.getThrownException());
        }

        bos.write(b,off,len);
    }

    public void close() throws IOException {
       
//        joinThread();
        
//        bos.close(); // to extend?
//        bis.close();  // NOTE (COFF): Probably wrong to close both sides here!
        
        if(rt.isExceptionThrown())
        {
            logger.error(rt.getThrownException());
            throw new IOException(rt.getThrownException());
        }
        bos.close();
    }

    public void flush() throws IOException {
        

        if(rt.isExceptionThrown())
        {
            logger.error(rt.getThrownException());
            throw new IOException(rt.getThrownException());
        }
        bos.flush();
    }
    
    /**
     * Wait for the thread to finish.
     * Interrupt it when it doesn't finish in time.
     */
    private void joinThread() {
        final int TIME_OUT = 1000; // One second.
        try {
            rt.join(TIME_OUT);
            if (rt.isAlive()) rt.interrupt();
        } catch (InterruptedException e) {
            // Ignore.
        }
    }
    

    
}
