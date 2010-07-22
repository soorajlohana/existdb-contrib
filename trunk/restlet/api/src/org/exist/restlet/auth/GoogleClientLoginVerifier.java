/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Realm;
import org.exist.security.User;
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
public class GoogleClientLoginVerifier extends UserVerifier {
   static Reference CLIENT_LOGIN = new Reference("https://www.google.com/accounts/ClientLogin");
   SessionManager confSessionManager;
   Realm realm;
   UserStorage userStorage;
   public GoogleClientLoginVerifier(Context context) {
      super(context);
      realm = (Realm)context.getAttributes().get(XMLDBResource.REALM_NAME);
      if (realm==null) {
         getLogger().severe("No realm for web users.");
      } else {
         getLogger().info(GoogleClientLoginVerifier.class.getName()+" using realm "+realm);
      }
      confSessionManager = (SessionManager)getContext().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
      String [] listValues = getContext().getParameters().getValuesArray(XMLDBResource.USER_LIST_NAME);
      String userRef = getContext().getParameters().getFirstValue(XMLDBResource.USER_HREF_NAME);
      if (listValues!=null && listValues.length>0) {
         userStorage = new ParameterUserStorage(context);
      } else if (userRef!=null) {
         userStorage = new UserStorage(context,new Reference(userRef));
      } else {
         getLogger().severe("No user configuraiton was provided.");
         userStorage = null;
      }
      if (userStorage!=null) {
         getLogger().info("Loading users via "+userStorage.getClass().getName());
         try {
            userStorage.load();
         } catch (Exception ex) {
            getLogger().log(Level.SEVERE,"Cannot load users from storage.",ex);
         }
      }
   }

   public UserStorage getStorage(String key)
   {
      if (userStorage!=null && userStorage.verifyKey(key)) {
         return userStorage;
      }
      return null;
   }

   public boolean isUserAllowedDatabaseAccess(String database,String user) {
      if (userStorage!=null) {
         return userStorage.isUserAllowedDatabaseAccess(database, user);
      }
      return false;
   }

   public boolean authenticate(String identity,String password)
   {
      if (userStorage==null) {
         return false;
      }
      userStorage.check();

      if (userStorage.getRealm(realm).get(identity)==null) {
         return false;
      }
      Request request = new Request(Method.POST,CLIENT_LOGIN);
      Form authForm = new Form();
      authForm.add("accountType", identity.endsWith("gmail.com") ? "GOOGLE" : "HOSTED");
      authForm.add("service", identity.endsWith("gmail.com") ? "mail" : "apps");
      authForm.add("source", "exist-webapp");
      authForm.add("Email", identity);
      authForm.add("Passwd", password);
      request.setEntity(authForm.getWebRepresentation());
      Response response = getContext().getClientDispatcher().handle(request);
      boolean result = response.getStatus().isSuccess();
      if (!result) {
         getLogger().info("Failed login, status="+response.getStatus().getCode()+", "+response.getEntityAsText());
      }
      response.getEntity().release();
      return result;
   }

   public User getUser(String identity) {
      userStorage.check();
      return userStorage.getRealm(realm).get(identity);
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

      User user = getUser(identity);
      if (user==null) {
         getLogger().info("User "+identity+" not found.");
         return Verifier.RESULT_INVALID;
      }

      if (authenticate(identity,new String(secret))) {
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
   }

}
