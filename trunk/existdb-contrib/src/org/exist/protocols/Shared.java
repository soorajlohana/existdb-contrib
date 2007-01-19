/*
 * Shared.java
 *
 * Created on January 19, 2007, 3:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.protocols;

import java.net.MalformedURLException;
import java.net.URL;
import org.exist.protocols.Credentials;

/**
 *
 * @author dwessels
 */
public class Shared {
    
    public static Credentials extractUserInfo(String url){
        
        Credentials creds = new Credentials();
        
        String rewrittenUrl = (url.startsWith("xmldb:exist:"))
            ? "http"+url.toString().substring(11)
            : url;
        
        String userInfo=null;
        try {
            userInfo = new URL(rewrittenUrl).getUserInfo();
        } catch (MalformedURLException ex) {
            //logger.error(ex.getMessage());
            ex.printStackTrace();
        }
        
        if(userInfo==null){
            creds.username=null;
            creds.password=null;
        } else {
            int separator = userInfo.indexOf(':');
            if(separator==-1){
                creds.username=userInfo;
                creds.password=null;
            } else {
                creds.username=userInfo.substring(0,separator);
                creds.password=userInfo.substring(separator+1);
            }
        }
        
        if(creds.username!=null && creds.username.equals("")){
            creds.username=null;
        }
        
        if(creds.password!=null && creds.password.equals("")){
            creds.password=null;
        }
        
        return creds;
    }
    
}
