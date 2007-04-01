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
 *  $Id$
 */

package org.exist.protocolhandler.xmldb;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.exist.xmldb.XmldbURI;

/**
 *  A utility class for xmldb URLs. Since, java.net.URL is final this class
 * acts as a wrapper, convenience methods have been added.<BR>
 * <BR>
 * Example:<BR>
 * <I>xmldb:exist://username:password@hostname:8080/exist/xmlrpc/db/collection/document.xml</I><BR>
 * <BR>
 * Note: A collection URL ends with a "/":<BR>
 * <I>xmldb:exist://hostname:8080/exist/xmlrpc/db/collection/</I>
 *
 * @see java.net.URI
 * @see java.net.URL
 * @see org.exist.xmldb.XmldbURI
 *
 * @author Dannes Wessels
 */
public class XmldbURL {
    
    private static final int USERNAME=1;
    private static final int PASSWORD=2;
    
    private URL myUrl;
    
    /**
     * Creates a new instance of XmldbURL using an XmldbURI object.
     *
     * @param xmldbURI Resource location.
     * @throws java.net.MalformedURLException
     */
    public XmldbURL(XmldbURI xmldbURI) throws MalformedURLException {
        this(xmldbURI.toURL());
    }
    
    /**
     * Creates a new instance of XmldbURL using an URL object.
     * @param url Resource location.
     * @throws java.net.MalformedURLException
     */
    public XmldbURL(URL url) throws MalformedURLException  {
        // check protocol
        if(url.getProtocol().equals("xmldb")){
            myUrl = url;
        } else {
            throw new MalformedURLException("URL is not an \"xmldb:\" URL: "+url.toString() );
        }
    }
    
    /**
     * Creates a new instance of XmldbURL using an URI object.
     *
     * @param uri Resource location.
     * @throws java.net.MalformedURLException
     */
    public XmldbURL(URI uri) throws MalformedURLException  {
        this(uri.toURL());
    }
    
    /**
     * Creates a new instance of XmldbURL using an String.
     * @param txt Resource location.
     * @throws java.net.MalformedURLException
     */
    public XmldbURL(String txt) throws MalformedURLException {
        this(new URL(txt));
    }
    
    /**
     * xmldb:exist://<B>username:password</B>@hostname:8080/exist/xmlrpc/db/collection/document.xml
     * @see java.net.URL#getUserInfo
     *
     * @return username:password
     */
    public String getUserInfo() {
        return myUrl.getUserInfo();
    }
    
    /**
     * xmldb:exist://<B>username</B>:password@hostname:8080/exist/xmlrpc/db/collection/document.xml
     * @return username
     */
    public String getUsername(){
        return extractCredentials(USERNAME);
    }
    
    /**
     * xmldb:exist://username:<B>password</B>@hostname:8080/exist/xmlrpc/db/collection/document.xml
     * @return password
     */
    public String getPassword(){
        return extractCredentials(PASSWORD);
    }
    
    /**
     * @return URL representation of location.
     */
    public URL getURL(){
        return myUrl;
    }
    
    /**
     * xmldb:exist://<B>username:password@hostname:8080/exist/xmlrpc/db/collection/document.xml</B>?query#fragment
     * @see java.net.URL#getAuthority
     * @return authority
     */
    public String getAuthority() {
        return myUrl.getAuthority();
    }
    
    /**
     * Return context
     */
    public String getContext() {
        return "/exist/xmlrpc"; // TODO repair
    }
    
    // /exist/xmlrpc/db/shakespeare/plays/macbeth.xml
    // /exist/xmlrpc/db/shakespeare/plays/
    // /db/shakespeare/plays/macbeth.xml
    // /db/shakespeare/plays/
    
    
    /**
     * xmldb:exist://username:password@hostname:8080/exist/xmlrpc<B>/db/collection</B>/document.xml
     * @return collection
     */
    public String getCollection(){

        String serverPath=myUrl.getPath();
        String collectionName=null;
        
        // TODO seperate check /exist/xmlrpc or /.*/xmlrpc/, check this first
        // then /db check can be removed
        
        if(serverPath.startsWith("/db")){ // Embedd URLs
            if(serverPath.endsWith("/")){
                collectionName=serverPath.substring(0,serverPath.length()-1);
            } else {
                int lastSep=serverPath.lastIndexOf('/'); // TODO extra checks
                if(lastSep==0){
                   collectionName=null;
                } else {
                   collectionName=serverPath.substring(0, lastSep);
                }
            }
        } else {
            // URLa : xmldb:exist:...../exist/xmlrpc/db/....
            // URLb : xmldb:exist:...../xmlrpc/db/....
            int dbLocation=serverPath.indexOf("/db");
            if(serverPath.endsWith("/")){
                collectionName=serverPath.substring(dbLocation);
            } else {
                int lastSep=serverPath.lastIndexOf('/'); // TODO extra checks
                collectionName=serverPath.substring(dbLocation, lastSep);
            }
        }
        
        return collectionName;
    }
    
    /**
     * xmldb:exist://username:password@hostname:8080/exist/xmlrpc/db/collection/<B>document.xml</B>
     * @return collection
     */
    public String getDocumentName(){
        String serverPath=myUrl.getPath();
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
    
    // Get username or password
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
    
    /**
     * <B>xmldb</B>:exist://username:password@hostname:8080/exist/xmlrpc/db/collection/document.xml
     * @see java.net.URL#getProtocol
     * @return protocol
     */
    public String getProtocol(){
        return myUrl.getProtocol();
    }
    
    /**
     * xmldb:exist://username:password@<B>hostname</B>:8080/exist/xmlrpc/db/collection/document.xml
     * @see java.net.URL#getProtocol
     * @return protocol
     */
    public String getHost(){
        String hostname=myUrl.getHost();
        if(hostname.equals("")){
            return null;
        } else {
            return hostname;
        }
    }
    
    /**
     * xmldb:exist://username:password@hostname:<B>8080</B>/exist/xmlrpc/db/collection/document.xml
     * @see java.net.URL#getPort
     * @return port
     */
    public int getPort(){
        return myUrl.getPort();
    }
    
    /**
     * xmldb:exist://username:password@hostname:8080:<B>/exist/xmlrpc/db/collection/document.xml</B>
     * @see java.net.URL#getPath
     * @return port
     */
    public String getPath(){
        return myUrl.getPath();
    }
    
    /**
     * xmldb:exist://username:password@hostname:8080/exist/xmlrpc/db/collection/document.xml?<B>query</B>#fragment
     * @see java.net.URL#getQuery
     * @return query
     */
    public String getQuery(){
        return myUrl.getQuery();
    }
    
    /**
     * xmldb:exist://username:password@hostname:8080:/exist/xmlrpc<B>/db/collection/document.xml</B>
     * @return collectionpath
     */
    public String getCollectionPath(){
        return myUrl.getPath().substring(13);
    }
    
    /**
     * Get http:// URL from xmldb:exist:// URL
     * xmldb:exist://username:password@hostname:8080:/exist/xmlrpc/db/collection/document.xml
     * @return http://username:password@hostname:8080:/exist/xmlrpc/db/collection/document.xml
     */
    public String getXmlRpcURL(){
        return "http://" + myUrl.getAuthority() + getContext();
    }
    
    /**
     * Does the URL have at least a username?
     * @return TRUE when URL contains username
     */
    public boolean hasUserInfo(){
        return (getUserInfo()!=null && getUsername()!=null);
    }
    
    /**
     * Get eXist instance name.
     *
     * @return eXist-db instance name, at this moment fixed to exist
     */
    public String getInstanceName() {
        return "exist";  // No other choice
    }
    
    /**
     * Get textual representation of URL.
     *
     * @see java.net.URL#toString
     * @return Text representation of URL.
     */
    public String toString(){
        return myUrl.toString();
    }
    
    /**
     * Get information wether URL is an embedded URL.
     *
     * @return TRUE when URL refers to resource in embedded eXist-db.
     */
    public boolean isEmbedded(){
        return (getHost()==null);
    }
}
