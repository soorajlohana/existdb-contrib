/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashMap;
import java.util.Map;
import org.exist.restlet.XMLDBResource;
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
   long lastModified;
   Map<String,User> users;
   public GoogleClientLoginVerifier(Context context) {
      super(context);
      lastModified = -1;
      this.users = new HashMap<String,User>();
   }

   protected void checkUserMap()
   {
      String [] listValues = getContext().getParameters().getValuesArray(XMLDBResource.USER_LIST_NAME);
      if (listValues!=null && listValues.length>0 && lastModified<0) {
         for (int l=0; l<listValues.length; l++) {
            String [] userSpecs = listValues[l].split(",");
            for (int i=0; i<userSpecs.length; i++) {
               String spec = userSpecs[i].trim();
               int eq = spec.indexOf('=');
               String [] groups = null;
               String username = spec;
               if (eq>0) {
                  username = spec.substring(0,eq).trim();
                  groups = spec.substring(eq+1).trim().split(",");
                  for (int g=0; g<groups.length; g++) {
                     groups[g] = groups[g].trim();
                  }
               }
               int colon = username.indexOf(':');
               int uid = i;
               if (colon>0) {
                  uid = Integer.parseInt(username.substring(colon+1));
                  username = username.substring(0,colon).trim();
               }
               getLogger().info("User: "+uid+" -> "+username);
               users.put(username, new WebUser(uid,username.substring(0,colon),groups));
            }
         }
         lastModified = System.currentTimeMillis();
      }
   }

   protected boolean authenticate(String identity,String password)
   {
      Request request = new Request(Method.POST,CLIENT_LOGIN);
      Form authForm = new Form();
      authForm.add("accountType", "HOSTED");
      authForm.add("service", identity.endsWith("gmail.com") ? "mail" : "apps");
      authForm.add("source", "exist-webapp");
      authForm.add("Email", identity);
      authForm.add("Passwd", password);
      request.setEntity(authForm.getWebRepresentation());
      Response response = getContext().getClientDispatcher().handle(request);
      boolean result = response.getStatus().isSuccess();
      response.getEntity().release();
      return result;
   }

   public int verify(Request request, Response response) {
      ChallengeResponse authInfo = request.getChallengeResponse();
      if (authInfo==null) {
         return Verifier.RESULT_MISSING;
      }
      String identity = authInfo.getIdentifier();
      char [] secret = authInfo.getSecret();
      if (identity==null || secret==null) {
         return Verifier.RESULT_INVALID;
      }

      checkUserMap();

      User user = users.get(identity);
      if (user==null) {
         getLogger().info("User "+identity+" not found.");
         return Verifier.RESULT_INVALID;
      }

      if (authenticate(identity,new String(secret))) {
         request.getAttributes().put(XMLDBResource.USER_NAME,user);
         return Verifier.RESULT_VALID;
      } else {
         getLogger().info("Password check failed on "+identity);
         return Verifier.RESULT_INVALID;
      }
   }

}
