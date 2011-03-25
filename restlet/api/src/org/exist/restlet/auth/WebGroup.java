/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.ArrayList;
import java.util.List;
import org.exist.config.Configuration;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.realm.Realm;

/**
 *
 * @author alex
 */
public class WebGroup implements Group {

   Realm realm;
   int id;
   String name;
   public WebGroup(Realm realm,int id,String name) {
      this.realm = realm;
      this.id = id;
      this.name = name;
   }
   public List<Account> getManagers() {
      return new ArrayList<Account>();
   }
   public void addManagers(List<Account> managers) {
   }
   public void addManager(Account manager) {
   }
   public void removeManager(Account account) {
   }
   public boolean isManager(Account acnt) {
      return false;
   }
   public void assertCanModifyGroup(Account account) throws PermissionDeniedException {
      throw new PermissionDeniedException("Cannot modify group for web groups.");
   }

   public int getId() {
      return id;
   }

   public Realm getRealm() {
      return realm;
   }

   public String getRealmId() {
      return realm.getId();
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
