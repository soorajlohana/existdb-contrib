/*
 * StringDataSource.java
 *
 * Created on April 10, 2007, 10:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
