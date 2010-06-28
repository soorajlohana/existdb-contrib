/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.restlet.XMLDBResource;
import org.exist.security.User;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class DBUserVerifier extends UserVerifier implements UserManager {
   SessionManager confSessionManager;
   org.exist.security.SecurityManager manager;
   public DBUserVerifier(Context context) {
      super(context);
      this.manager = (org.exist.security.SecurityManager)getContext().getAttributes().get(XMLDBResource.DB_SECURITY_MANAGER);
   }
   public DBUserVerifier(Context context,org.exist.security.SecurityManager manager) {
      super(context);
      this.manager = manager;
      confSessionManager = (SessionManager)getContext().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
   }

   public User getUser(String identity) {
      return manager.getUser(identity);
   }

   public int verify(Request request, Response response) {
      if (request.getAttributes().get(XMLDBResource.USER_NAME)!=null) {
         return Verifier.RESULT_VALID;
      }

      ChallengeResponse authInfo = request.getChallengeResponse();
      if (authInfo==null) {
         return Verifier.RESULT_MISSING;
      }
      String identity = authInfo.getIdentifier();
      char [] secret = authInfo.getSecret();
      if (identity==null || secret==null) {
         return Verifier.RESULT_INVALID;
      }
      User user = manager.getUser(identity);
      if (user!=null) {
         boolean valid = user.authenticate(new String(secret));
         if (valid) {
            SessionManager sessionManager = (SessionManager)request.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
            if (sessionManager==null) {
               sessionManager = confSessionManager;
            }
            if (sessionManager!=null) {
               String sessionId = sessionManager.newSession(user);
               request.getAttributes().put(XMLDBResource.SESSION_NAME,sessionId);
            }
            request.getAttributes().put(XMLDBResource.USER_NAME,user);
            request.getAttributes().put(XMLDBResource.NEW_USER_NAME,Boolean.TRUE);
            return Verifier.RESULT_VALID;
         } else {
            getLogger().info("Password check failed on "+identity);
            return Verifier.RESULT_INVALID;
         }
      } else {
         getLogger().info("User "+identity+" not found.");
         return Verifier.RESULT_INVALID;
      }
   }
}
