/*
 * ExistIOException.java
 *
 * Created on February 8, 2007, 5:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.localcopied;

import java.io.IOException;

/**
 *
 * @author wessels
 */
public class ExistIOException extends IOException{
    
    /** Creates a new instance of ExistIOException */
    public ExistIOException(String message, Throwable cause) {
        super(message + " " + cause.fillInStackTrace().toString() );
    }
    
}
