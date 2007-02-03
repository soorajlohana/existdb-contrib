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
 *  $Id$
 */

package org.exist.xmldb;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 *  Equivalent to the XmldbURI class
 *
 * @author Dannes Wessels
 */
public class XmldbURL {
    
    private static int USERNAME=1;
    private static int PASSWORD=2;
    
    private URL myUrl;
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(XmldbURI xmldbURI) throws MalformedURLException {
        this(xmldbURI.toURL());
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(URL url) throws MalformedURLException  {
        // check protocol
        myUrl = url;
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(URI uri) throws MalformedURLException  {
        myUrl=uri.toURL();
    }
    
    /** Creates a new instance of XmldbURL */
    public XmldbURL(String txt) throws MalformedURLException {
        myUrl = new URL( txt );
    }
    
    public String getUserInfo() {
       return myUrl.getUserInfo();
    }
    
    public String getUsername(){
        return extractCredentials(USERNAME);
    }
    
    public String getPassword(){
        return extractCredentials(PASSWORD);
    }
    
    public URL getURL(){
        return myUrl;
    }
    
    public String getAuthority() {
        return myUrl.getAuthority();
    }
    
    public String getContext() {
        return "/exist/xmlrpc"; // TODO repair
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
    
    private String extractCredentials(int part) {
        
        String userInfo = myUrl.getUserInfo();
        String username = null;
        String password = null;
        
        if(userInfo==null){
            username=null;
            password=null;
        } else {
            int separator = userInfo.indexOf(':');
            if(separator==-1){
                username=userInfo;
                password=null;
            } else {
                username=userInfo.substring(0,separator);
                password=userInfo.substring(separator+1);
            }
        }
        
        // Fix credentials. If not found (empty string) fill NULL
        if(username!=null && username.equals("")){
            username=null;
        }
        
        // Fix credentials. If not found (empty string) fill NULL
        if(password!=null && password.equals("")){
            password=null;
        }
        
        if(part==USERNAME){
            return username;
        } else if(part==PASSWORD){
            return password;
        }
        return null;
    }
    
    public String getProtocol(){
        return myUrl.getProtocol();
    }
    
    public String getHost(){
        String hostname=myUrl.getHost();
        if(hostname.equals("")){
            return null;
        } else {
            return hostname;
        }
    }    
    
    public int getPort(){
        return myUrl.getPort();
    }    

    public String getPath(){
        return myUrl.getPath();
    }    

    public String getQuery(){
        return myUrl.getQuery();
    }    
    
    public String getCollectionPath(){
        return myUrl.getPath().substring(13);
    }
    
    public String getXmlRpcURL(){
        return "http://" + myUrl.getAuthority() + getContext();
    }
    
    public boolean hasUserInfo(){
        return (getUserInfo()!=null && getUsername()!=null);
    }

    public String getInstanceName() {
        return "exist";  // No other choice
    }
    
    public String toString(){
        return myUrl.toString();
    }
    
    public boolean isEmbedded(){
        return (getHost()==null);
    }
}
