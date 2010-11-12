/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.config.Configuration;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;

/**
 *
 * @author alex
 */
public class WebGroup implements Group {

   String realm;
   int id;
   String name;
   public WebGroup(String realm,int id,String name) {
      this.realm = realm;
      this.id = id;
      this.name = name;
   }
   public boolean isMembersManager(Account acnt) {
      return false;
   }

   public int getId() {
      return id;
   }

   public String getRealmId() {
      return realm;
   }

   public void save() throws PermissionDeniedException {
   }

   public String getName() {
      return name;
   }

   public boolean isConfigured() {
      return false;
   }

   public Configuration getConfiguration() {
      return null;
   }

}
