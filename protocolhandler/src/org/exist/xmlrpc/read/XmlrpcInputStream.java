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

package org.exist.xmlrpc.read;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;
import org.exist.io.BlockingInputStream;

/**
 * Read document from remote database (using xmlrpc) as a input stream.
 *
 * @author Dannes Wessels
 */
public class XmlrpcInputStream  extends InputStream {
    
    private final static Logger logger = Logger.getLogger(XmlrpcInputStream.class);
    private BlockingInputStream bis;
    private OutputStream bos;
    private XmlrpcDownloadThread rt;
    
    /**
     *  Constructor of XmlrpcInputStream. 
     * 
     * @param xmldbURL Location of document in database.
     * @throws MalformedURLException Thrown for illegalillegal URLs.
     */
    public XmlrpcInputStream(XmldbURL xmldbURL) {
        
        logger.debug("Initializing ResourceInputStream");
        
        bis = new BlockingInputStream();
        bos = bis.getOutputStream();
        
        rt = new XmlrpcDownloadThread( xmldbURL , bos); 
        
        rt.start();
        
        logger.debug("Initializing ResourceInputStream done");
        
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bis.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bis.read(b, 0, b.length);
    }

//    public void mark(int readlimit) {
//
//        bos.mark(readlimit);
//    }

    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    public void reset() throws IOException {
        super.reset();
    }

    public int read() throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bis.read();
    }

//    public boolean markSupported() {
//
//        boolean retValue;
//        
//        retValue = bos.markSupported();
//        return retValue;
//    }

    public void close() throws IOException {

        bis.close();
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }

    }
    
    /** NOTE (COFF) : This is an OutputStream method!? */
    public void flush() throws IOException {
        bos.flush();
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
    }
       

    public int available() throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bis.available();
    }
    
}
