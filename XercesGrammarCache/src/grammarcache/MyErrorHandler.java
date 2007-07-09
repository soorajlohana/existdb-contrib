/*
 * MyErrorHandler.java
 *
 * Created on July 9, 2007, 9:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package grammarcache;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author dwessels
 */
public class MyErrorHandler implements ErrorHandler {
    
    private boolean isValid=true;
    
    /** Creates a new instance of MyErrorHandler */
    public MyErrorHandler() {
    }
    
    public boolean isValid(){
        return isValid;
    }

    public void warning(SAXParseException exception) throws SAXException {
        System.out.println("WARNING:" +exception.getLineNumber() + ","  + exception.getColumnNumber() + "=" +exception.getMessage());
    }

    public void error(SAXParseException exception) throws SAXException {
        System.out.println("ERROR:" +exception.getLineNumber() + ","  + exception.getColumnNumber() + "=" +exception.getMessage());
        isValid=false;
        throw new SAXException(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        System.out.println("FATAL:" +exception.getLineNumber() + ","  + exception.getColumnNumber() + "=" +exception.getMessage());
        isValid=false;
        throw new SAXException(exception);
    }
    
}