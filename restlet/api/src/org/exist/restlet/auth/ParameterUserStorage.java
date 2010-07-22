/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Realm;
import org.exist.security.User;
import org.restlet.Context;

/**
 *
 * @author alex
 */
public class ParameterUserStorage extends UserStorage {
   public ParameterUserStorage(Context context) {
      super(context,null);
   }

   public void load() {
      getLogger().info("Loading users from parameters...");
      Realm realm = (Realm)context.getAttributes().get(XMLDBResource.REALM_NAME);
      if (realm==null) {
         getLogger().severe("No realm has been speciied for web users.");
         return;
      } else {
         getLogger().info("Using realm "+realm+" for users.");
      }
      Map<String,User> users = getRealm(realm);
      
      String [] listValues = context.getParameters().getValuesArray(XMLDBResource.USER_LIST_NAME);
      if (listValues!=null && listValues.length>0) {
         for (int l=0; l<listValues.length; l++) {
            String [] userSpecs = listValues[l].split(",");
            for (int i=0; i<userSpecs.length; i++) {
               String spec = userSpecs[i].trim();
               int eq = spec.indexOf('=');
               Set<String> groups = new HashSet<String>();
               Set<String> databases = new HashSet<String>();
               String username = spec;
               if (eq>0) {
                  username = spec.substring(0,eq).trim();
                  String [] parts = spec.substring(eq+1).trim().split(";");
                  for (int p=0; p<parts.length; p++) {
                     parts[p] = parts[p].trim();
                     if (parts[p].startsWith("group:")) {
                        groups.add(parts[p].substring(6).trim());
                     } else if (parts[p].startsWith("database:")) {
                        databases.add(parts[p].substring(9).trim());
                     }
                  }
               }
               int colon = username.indexOf(':');
               int uid = i;
               if (colon>0) {
                  uid = Integer.parseInt(username.substring(colon+1));
                  username = username.substring(0,colon).trim();
               }
               getLogger().info("User: "+uid+" -> "+username);
               User user = new WebUser(realm,uid,username.substring(0,colon),groups.toArray(new String[0]));
               users.put(username, user);
               for (String dbName : databases) {
                  grantUserAccess(dbName,username);
               }
            }
         }
      }
   }

   public void check() {
   }
}
