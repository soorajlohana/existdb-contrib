package org.expath.ftclient.FTP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class InputStream2Writer {
		
	public static Writer convert(InputStream is) throws Exception {
	    if (is != null) {
	        Writer writer = new StringWriter();
	        char[] buffer = new char[1024];
	    try {
	        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        int n;
	        while ((n = reader.read(buffer)) != -1) {
	            writer.write(buffer, 0, n);
	        }
	    } finally {
	        is.close();
	    }
	    return writer;
	    } else {        
	        return null;
	    }		
	}
	
}