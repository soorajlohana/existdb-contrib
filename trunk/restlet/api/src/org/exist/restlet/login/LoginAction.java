/*
 * LoginForm.java
 *
 * Created on September 7, 2007, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.login;

import org.exist.restlet.XMLDBResource;
import org.exist.restlet.auth.SessionManager;
import org.exist.restlet.auth.UserManager;
import org.exist.security.Subject;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

/**
 *
 * @author alex
 */
public class LoginAction extends ServerResource
{

   /** Creates a new instance of LoginForm */
   public LoginAction()
   {
      setNegotiated(false);
   }

   protected void doInit() {
   }
   
   protected Representation post(Representation rep) {
      UserManager userManager = (UserManager)getRequest().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
      if (userManager==null) {
         userManager = (UserManager)getContext().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
      }

      if (userManager==null) {
         getLogger().severe("The UserManager instance is missing.");
         getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
         return null;
      }
      
      Form form = new Form(rep);
      String nonceS = form.getFirstValue("nonce");
      if (nonceS==null) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }
      int nonce = Integer.parseInt(nonceS);
      LoginApplication.NonceCache nonceCache = (LoginApplication.NonceCache)getContext().getAttributes().get(LoginApplication.NONCES);
      if (!nonceCache.consume(nonce)) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }

      String username = form.getFirstValue("username");
      String password = form.getFirstValue("password");
      Subject user = userManager.authenticate(username, password);
      if (user!=null) {
         SessionManager sessionManager = (SessionManager)getRequest().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
         if (sessionManager==null) {
            sessionManager = (SessionManager)getContext().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
         }
         if (sessionManager!=null) {
            String sessionId = sessionManager.newSession(user);
            getRequest().getAttributes().put(XMLDBResource.SESSION_NAME,sessionId);
         }
         getRequest().getAttributes().put(XMLDBResource.USER_NAME,user);
         getRequest().getAttributes().put(XMLDBResource.NEW_USER_NAME,Boolean.TRUE);
         String redirect = form.getFirstValue("redirect");
         if (redirect!=null && redirect.length()!=0) {
            getResponse().redirectSeeOther(redirect);
         }
         getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
      } else {
         getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }
      return null;
   }
   
}
