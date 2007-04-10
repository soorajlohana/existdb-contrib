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
 */

package nl.ow.dilemma.exist.module.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 *
 * @author wessels
 */
public class StringDataSource implements DataSource {
    
    private String name=null;
    private String data=null;
    private String contentType=null;
    
    /** Creates a new instance of StringDataSource */
    public StringDataSource(String name, String data, String contentType) {
        this.name=name;
        this.data=data;
        this.contentType=contentType;
    }

    public InputStream getInputStream() throws IOException {
        if(data!=null){
            return new ByteArrayInputStream(data.getBytes());
        }
        throw new IOException("No data");
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }
    
}
