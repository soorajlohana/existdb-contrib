/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.Set;
import org.exist.config.Configuration;
import org.exist.security.AXSchemaType;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.Subject;
import org.exist.security.realm.Realm;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author alex
 */
public class SubjectWrapper implements Subject {
   Account account;
   boolean authenticated;
   public SubjectWrapper(Account account,boolean authenticated) {
      this.account = account;
      this.authenticated = authenticated;
   }

   public boolean authenticate(Object o) {
      return account.getPassword().equals(o.toString());
   }

   public boolean isAuthenticated() {
      return authenticated;
   }

   public String getSessionId() {
      return null;
   }

   public Group addGroup(String string) throws PermissionDeniedException {
      return account.addGroup(string);
   }

   public Group addGroup(Group group) throws PermissionDeniedException {
      return account.addGroup(group);
   }

   public void remGroup(String group) throws PermissionDeniedException {
      account.remGroup(group);
   }

   public String[] getGroups() {
      return account.getGroups();
   }

   public boolean hasDbaRole() {
      return account.hasDbaRole();
   }

   public String getPrimaryGroup() {
      return account.getPrimaryGroup();
   }

   public Group getDefaultGroup() {
      return account.getDefaultGroup();
   }

   public boolean hasGroup(String group) {
      return account.hasGroup(group);
   }

   public void setPassword(String password) {
      account.setPassword(password);
   }

   public void setHome(XmldbURI xuri) {
      account.setHome(xuri);
   }

   public XmldbURI getHome() {
      return account.getHome();
   }

   public Realm getRealm() {
      return account.getRealm();
   }

   public String getPassword() {
      return account.getPassword();
   }

   public String getDigestPassword() {
      return account.getDigestPassword();
   }

   public void setGroups(String[] groups) {
      account.setGroups(groups);
   }

   public Set<AXSchemaType> getMetadataKeys() {
      return account.getMetadataKeys();
   }

   public String getMetadataValue(AXSchemaType key) {
      return account.getMetadataValue(key);
   }

   public void setMetadataValue(AXSchemaType key, String value) {
      account.setMetadataValue(key, value);
   }

   public String getUsername() {
      return account.getUsername();
   }

   public boolean isAccountNonExpired() {
      return account.isAccountNonExpired();
   }

   public boolean isAccountNonLocked() {
      return account.isAccountNonLocked();
   }

   public boolean isCredentialsNonExpired() {
      return account.isCredentialsNonExpired();
   }

   public boolean isEnabled() {
      return account.isEnabled();
   }

   public int getId() {
      return account.getId();
   }

   public String getRealmId() {
      return account.getRealmId();
   }

   public void save() throws PermissionDeniedException {
      account.save();
   }

   public String getName() {
      return account.getName();
   }

   public boolean isConfigured() {
      return account.isConfigured();
   }

   public Configuration getConfiguration() {
      return account.getConfiguration();
   }

}
