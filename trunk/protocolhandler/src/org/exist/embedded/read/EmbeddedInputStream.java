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

package org.exist.embedded.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.exist.xmldb.XmldbURL;
import org.exist.localcopied.BlockingOutputStream;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author wessels
 */
public class EmbeddedInputStream  extends InputStream {
    
    private final static Logger logger = Logger.getLogger(EmbeddedInputStream.class);
    private BlockingOutputStream bos;
    private EmbeddedReadResourceThread rt;
    
    
    public EmbeddedInputStream(XmldbURL uri) throws MalformedURLException {
        
        logger.debug("Initializing ResourceInputStream");
        
        bos = new BlockingOutputStream();
        
        rt = new EmbeddedReadResourceThread( uri , bos); 
        
        rt.start();
        
        logger.debug("Initializing ResourceInputStream done");
        
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bos.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }
        
        return bos.read(b, 0, b.length);
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
        
        return bos.read();
    }

//    public boolean markSupported() {
//
//        boolean retValue;
//        
//        retValue = bos.markSupported();
//        return retValue;
//    }

    public void close() throws IOException {

        bos.close();
        
        if(rt.isExceptionThrown())
        {
            throw new IOException(rt.getThrownException());
        }

    }
    
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
        
        return bos.available();
    }
    
}
