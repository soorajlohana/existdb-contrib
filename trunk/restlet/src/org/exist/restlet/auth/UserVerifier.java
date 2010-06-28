/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.exist.security.Realm;
import org.exist.security.User;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class UserVerifier implements UserManager {

   static final Map<String,Realm> realms = new HashMap<String,Realm>();

   public static Realm getRealm(final String name) {
      synchronized (realms) {
         Realm realm = realms.get(name);
         if (realm!=null) {
            return realm;
         }
         realm = new Realm() {
            public String toString() {
               return name;
            }
            public boolean equals(Object obj) {
               return name.equals(obj.toString());
            }
         };
         realms.put(name, realm);
         return realm;
      }
   }

   Context context;
   protected UserVerifier(Context context) {
      this.context = context;
   }

   public User getUser(String user)
   {
      throw new java.lang.IllegalStateException("Cannot get user from this object.");
   }

   public Context getContext() {
      return context;
   }
   public Logger getLogger() {
      return context.getLogger();
   }
   public int verify(Request request, Response response) {
      return Verifier.RESULT_INVALID;
   }
}
