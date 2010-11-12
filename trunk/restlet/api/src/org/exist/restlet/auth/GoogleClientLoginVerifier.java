/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Account;
import org.exist.security.Subject;
import org.exist.security.realm.Realm;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class GoogleClientLoginVerifier implements UserManager {
   static Reference CLIENT_LOGIN = new Reference("https://www.google.com/accounts/ClientLogin");
   SessionManager confSessionManager;
   String realmName;
   Realm realm;
   UserStorage userStorage;
   Subject system;
   Context context;
   public GoogleClientLoginVerifier(Context context) {
      this.context = context;
      realmName = context.getAttributes().get(XMLDBResource.REALM_NAME).toString();
      if (realmName==null) {
         getLogger().severe("No realm for web users.");
      } else {
         getLogger().info(GoogleClientLoginVerifier.class.getName()+" using realm "+realmName);
      }
      confSessionManager = (SessionManager)context.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
      String [] listValues = context.getParameters().getValuesArray(XMLDBResource.USER_LIST_NAME);
      String userRef = context.getParameters().getFirstValue(XMLDBResource.USER_HREF_NAME);
      if (listValues!=null && listValues.length>0) {
         userStorage = new ParameterUserStorage(context);
      } else if (userRef!=null) {
         userStorage = new UserStorage(context,new Reference(userRef));
      } else {
         getLogger().severe("No user configuraiton was provided.");
         userStorage = null;
      }
      if (userStorage!=null) {
         realm = userStorage.getRealm(realmName);
         system = userStorage.getSystemSubject(realm);
         getLogger().info("Loading users via "+userStorage.getClass().getName());
         try {
            userStorage.load();
         } catch (Exception ex) {
            getLogger().log(Level.SEVERE,"Cannot load users from storage.",ex);
         }
      }
   }

   public Logger getLogger() {
      return context.getLogger();
   }

   public boolean isUserAllowedDatabaseAccess(String database,String user) {
      if (userStorage!=null) {
         return userStorage.isUserAllowedDatabaseAccess(database, user);
      }
      return false;
   }

   public Subject authenticate(String identity,String password)
   {
      if (userStorage==null || realm==null) {
         return null;
      }
      userStorage.check();

      Account account = realm.getAccount(system, identity);
      if (account==null) {
         return null;
      }
      Request request = new Request(Method.POST,CLIENT_LOGIN);
      Form authForm = new Form();
      authForm.add("accountType", identity.endsWith("gmail.com") ? "GOOGLE" : "HOSTED");
      authForm.add("service", identity.endsWith("gmail.com") ? "mail" : "apps");
      authForm.add("source", "exist-webapp");
      authForm.add("Email", identity);
      authForm.add("Passwd", password);
      request.setEntity(authForm.getWebRepresentation());
      Response response = context.getClientDispatcher().handle(request);
      boolean result = response.getStatus().isSuccess();
      if (!result) {
         getLogger().info("Failed login, status="+response.getStatus().getCode()+", "+response.getEntityAsText());
      }
      response.getEntity().release();
      return result ? new SubjectWrapper(account,true) : null;
   }

   public int verify(Request request, Response response) {
      if (request.getAttributes().get(XMLDBResource.USER_NAME)!=null) {
         return Verifier.RESULT_VALID;
      }

      if (userStorage==null) {
         return Verifier.RESULT_INVALID;
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

      Account account = realm.getAccount(system, identity);
      if (account==null) {
         getLogger().info("User "+identity+" not found.");
         return Verifier.RESULT_INVALID;
      }

      Subject authSubject = authenticate(identity,new String(secret));
      if (authSubject!=null) {
         SessionManager sessionManager = (SessionManager)request.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
         if (sessionManager==null) {
            sessionManager = confSessionManager;
         }
         if (sessionManager!=null) {
            String sessionId = sessionManager.newSession(authSubject);
            request.getAttributes().put(XMLDBResource.SESSION_NAME,sessionId);
         }
         request.getAttributes().put(XMLDBResource.USER_NAME,authSubject);
         request.getAttributes().put(XMLDBResource.NEW_USER_NAME,Boolean.TRUE);
         return Verifier.RESULT_VALID;
      } else {
         getLogger().info("Password check failed on "+identity);
         return Verifier.RESULT_INVALID;
      }
   }

}
