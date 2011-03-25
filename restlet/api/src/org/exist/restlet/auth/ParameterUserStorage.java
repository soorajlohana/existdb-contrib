/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Account;
import org.exist.security.realm.Realm;
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
      String realmName = context.getParameters().getFirstValue(XMLDBResource.REALM_NAME);
      if (realmName==null) {
         getLogger().severe("No realm has been speciied for web users.");
         return;
      } else {
         getLogger().info("Using realm "+realmName+" for users.");
      }
      WebRealm realm = new WebRealm(realmName);
      database.put(realm.getId(), realm);

      int groupId = 0;
      
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
               for (String group : groups) {
                  if (realm.groups.get(group)==null) {
                     realm.groups.put(group,new WebGroup(realm,groupId++,group));
                  }
               }
               Account user = new WebUser(realm,uid,username.substring(0,colon),groups.toArray(new String[0]));
               realm.users.put(username, user);
               for (String dbName : databases) {
                  grantUserAccess(dbName,username);
               }
            }
         }
      }
      loadSystemUsers(realm);
   }

   public void check() {
   }
}
