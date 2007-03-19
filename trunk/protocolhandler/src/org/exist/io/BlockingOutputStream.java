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

package org.exist.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream adapter for a BlockingInputStream.
 *
 * @author Chris Offerman
 */
public class BlockingOutputStream extends OutputStream {
    
    private BlockingInputStream bis;
    
    /** Create a new BlockingOutputStream adapter.
     *
     *@param stream  The BlockingInputStream to adapt.
     */
    public BlockingOutputStream(BlockingInputStream stream) {
        bis = stream;
    }
    
    public void write(int b) throws IOException {
        bis.writeOutputStream(b);
    }
    
    public void write(byte b[], int off, int len) throws IOException {
        bis.writeOutputStream(b, off, len);
    }
    
    public void close() throws IOException {
        bis.closeOutputStream();
    }
    
    public void close(Exception ex) throws IOException {
        bis.closeOutputStream(ex);
    }
    
    public void flush() throws IOException {
        bis.flushOutputStream();
    }
}
