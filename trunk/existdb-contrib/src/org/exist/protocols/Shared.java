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

/**
 *
 * @author dwessels
 */
public class Shared {
    
    public static void extractUserInfo(String url, String username, String password){
        
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
            username="guest";
            password="guest";
        } else {
            int separator = userInfo.indexOf(':');
            if(separator==-1){
                username=userInfo;
                password=null;
            } else {
                username=userInfo.substring(0,separator);
                password=userInfo.substring(separator);
            }
        }
        
    }
    
}
