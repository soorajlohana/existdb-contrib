package org.expath.ftclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.util.Base64;

public class InputStream2Base64String {
	
	public static String convert(InputStream is) throws Exception {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = is.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
            }
        } catch (IOException ex) {
        	throw new IOException("Could not completely read the input stream.");
        }
        byte[] bytes = bos.toByteArray();
        
        return new String(Base64.encodeBase64(bytes));
	}

}
