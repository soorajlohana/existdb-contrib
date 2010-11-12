/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.restlet.XMLDBResource;
import org.exist.security.Account;
import org.exist.security.AuthenticationException;
import org.exist.security.Subject;
import org.exist.security.User;
import org.exist.security.internal.RealmImpl;
import org.exist.security.realm.Realm;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class DBUserVerifier implements UserManager {
   SessionManager confSessionManager;
   org.exist.security.SecurityManager manager;
   Context context;
   public DBUserVerifier(Context context) {
      this.manager = (org.exist.security.SecurityManager)context.getAttributes().get(XMLDBResource.DB_SECURITY_MANAGER);
      this.context = context;
   }
   public DBUserVerifier(Context context,org.exist.security.SecurityManager manager) {
      this.manager = manager;
      this.context = context;
      confSessionManager = (SessionManager)context.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
   }

   public Realm getRealm() {
      return this.manager.getRealm(RealmImpl.ID);
   }

   public boolean isUserAllowedDatabaseAccess(String dbname,String username) {
      // All users are from the database, just check that they exist
      return manager.getAccount(manager.getSystemSubject(), username)!=null;
   }
   public Subject authenticate(String username, String password)
   {
      try {
         return manager.authenticate(username, password);
      } catch (AuthenticationException ex) {
         return null;
      }
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
      try {
         Subject subject = manager.authenticate(identity, new String(secret));

         SessionManager sessionManager = (SessionManager)request.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
         if (sessionManager==null) {
            sessionManager = confSessionManager;
         }
         if (sessionManager!=null) {
            String sessionId = sessionManager.newSession(subject);
            request.getAttributes().put(XMLDBResource.SESSION_NAME,sessionId);
         }
         request.getAttributes().put(XMLDBResource.USER_NAME,subject);
         request.getAttributes().put(XMLDBResource.NEW_USER_NAME,Boolean.TRUE);
         return Verifier.RESULT_VALID;

      } catch (AuthenticationException ex) {
         return Verifier.RESULT_INVALID;
      }
   }
}
