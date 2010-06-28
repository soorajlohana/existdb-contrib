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
   org.exist.security.SecurityManager manager;
   public DBUserVerifier(Context context,org.exist.security.SecurityManager manager) {
      super(context);
      this.manager = manager;
   }

   public User getUser(String identity) {
      return manager.getUser(identity);
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
      User user = manager.getUser(identity);
      if (user!=null) {
         boolean valid = user.authenticate(new String(secret));
         if (valid) {
            request.getAttributes().put(XMLDBResource.USER_NAME,user);
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
