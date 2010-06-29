/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.restlet.XMLDBResource;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

/**
 *
 * @author alex
 */
public class LogoutResource extends ServerResource {

   public LogoutResource() {
      setNegotiated(false);
   }

   public Representation get() {
      boolean isDebugLog = "true".equals(getContext().getParameters().getFirstValue(LogoutResource.class.getName()+".debug"));
      SessionManager sessionManager = (SessionManager)getContext().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
      if (sessionManager==null) {
         sessionManager = (SessionManager)getRequest().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
      }
      String cookieName = getContext().getParameters().getFirstValue(XMLDBResource.COOKIE_NAME);
      String cookiePath = getContext().getParameters().getFirstValue(XMLDBResource.COOKIE_PATH_NAME);
      getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
      if (sessionManager==null) {
         getLogger().warning("No session manage for logout.");
         return null;
      }
      if (cookieName==null) {
         getLogger().warning("No cookie name parameter for logout.");
         return null;
      }
      Cookie cookie = getRequest().getCookies().getFirst(cookieName);
      if (cookie!=null) {
         sessionManager.expireSession(cookie.getValue());
         CookieSetting unset = new CookieSetting(cookieName,"");
         unset.setMaxAge(0);
         unset.setPath(cookiePath==null ? "/" : cookiePath);
         getResponse().getCookieSettings().add(unset);
      }
      if (isDebugLog && cookie!=null) {
         getLogger().info("Expiring session "+cookie.getValue()+" via logout.");
      }
      return null;
   }
}
