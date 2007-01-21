/*
 * XmldbURL.java
 *
 * Created on January 21, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.protocols.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.exist.xmldb.XmldbURI;

/**
 *  Equivalent to the XmldbURI class
 *
 * @author Dannes Wessels
 */
public class XmldbURL {
    
    private URL myUrl;
    private Credentials creds;
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(XmldbURI xmldbURI) throws MalformedURLException {
        //myUrl = new URL( xmldbURI.toString() );
        this(xmldbURI.toString());
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(URL url) throws MalformedURLException {
        //myUrl = url;
        this(url.toString());
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(URI uri) throws MalformedURLException {
        //myUrl = new URL( uri.toString() );
        this(uri.toString());
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(String txt) throws MalformedURLException {
        
        String rewrittenUrl = (txt.startsWith("xmldb:exist:"))
            ? "http"+txt.substring(11)
            : txt;
        myUrl = new URL( rewrittenUrl );
    }
    
    public String getUserInfo() {
        if(creds==null){
            try {
                extractCredentials();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return creds.userInfo;
    }
    
    public String getUsername(){
        if(creds==null){
            try {
                extractCredentials();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return creds.username;
    }
    
    public String getPassword(){
        if(creds==null){
            try {
                extractCredentials();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return creds.password;
    }
    
    // /exist/xmlrpc/db/shakespeare/plays/macbeth.xml
    // /exist/xmlrpc/db/shakespeare/plays/
    public String getCollection(){
        String serverPath=myUrl.getFile();
        String collectionName=null;
        
        if(!serverPath.startsWith("/exist/xmlrpc/")){
            collectionName=null;
        } else {
            if(serverPath.endsWith("/")){
                collectionName=serverPath.substring(13);
            } else {
                int lastSep=serverPath.lastIndexOf('/'); // TODO extra checks
                collectionName=serverPath.substring(13, lastSep);
            }
        }
            
        return collectionName;
    }
    
    public String getDocumentName(){
        String serverPath=myUrl.getFile();
        String documentName=null;
        if(serverPath.endsWith("/")){
            documentName=null;
        } else {
            int lastSep=serverPath.lastIndexOf('/');
            if(lastSep==-1){
                documentName=serverPath; // TODO discuss
            } else {
                documentName=serverPath.substring(lastSep+1);
            }
        }
        return documentName;
    }
    
    private void extractCredentials() throws MalformedURLException {
        creds = new Credentials();
        
        String url = myUrl.toString();
   
        // Small trick: rewrite xmldb:exist:// url to http:// url
        // with this we actually CAN distill the UserInfo
        String rewrittenUrl = (url.startsWith("xmldb:exist:"))
            ? "http"+url.toString().substring(11)
            : url;
        
        creds.userInfo= new URL(rewrittenUrl).getUserInfo();
        if(creds.userInfo==null){
            creds.username=null;
            creds.password=null;
        } else {
            int separator = creds.userInfo.indexOf(':');
            if(separator==-1){
                creds.username=creds.userInfo;
                creds.password=null;
            } else {
                creds.username=creds.userInfo.substring(0,separator);
                creds.password=creds.userInfo.substring(separator+1);
            }
        }
        
        // Fix credentials. If not found (empty string) fill NULL
        if(creds.username!=null && creds.username.equals("")){
            creds.username=null;
        }
        
        // Fix credentials. If not found (empty string) fill NULL
        if(creds.password!=null && creds.password.equals("")){
            creds.password=null;
        }
    }
    
    class Credentials {
        public String userInfo;
        public String username;
        public String password;
    }
}
