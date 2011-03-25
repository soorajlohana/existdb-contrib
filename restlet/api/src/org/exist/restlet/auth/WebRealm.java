/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.config.ConfigurationException;
import org.exist.security.Account;
import org.exist.security.AuthenticationException;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.Subject;
import org.exist.security.realm.Realm;
import org.exist.storage.DBBroker;

/**
 *
 * @author alex
 */
public class WebRealm implements Realm {

   Map<String,Account> users;
   Map<String,Group> groups;
   Map<String,Group> roles;
   String id;
   public WebRealm(String id) {
      this(id,null,null);
   }
   public WebRealm(String id,Set<Group> webGroups,Set<Group> webRoles) {
      this.id = id;
      this.users = new HashMap<String,Account>();
      this.groups = new HashMap<String,Group>();
      if (webGroups!=null) {
         for (Group group : webGroups) {
            groups.put(group.getName(), group);
         }
      }
      this.roles = new HashMap<String,Group>();
      if (webRoles!=null) {
         for (Group group : webRoles) {
            roles.put(group.getName(), group);
         }
      }
   }

   public org.exist.security.SecurityManager getSecurityManager() {
      return null;
   }

   public List<String> findUsernamesWhereNameStarts(Subject invokingUser, String startsWith) {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public List<String> findUsernamesWhereUsernameStarts(Subject invokingUser, String startsWith) {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public String getId() {
      return id;
   }

   public Collection<Account> getAccounts() {
      return Collections.unmodifiableCollection(users.values());
   }

   public Collection<Group> getGroups() {
      return Collections.unmodifiableCollection(groups.values());
   }

   public Collection<Group> getRoles() {
      return Collections.unmodifiableCollection(roles.values());
   }

   public void startUp(DBBroker dbb) throws EXistException {
   }

   public Database getDatabase() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Group getExternalGroup(Subject sbjct, String string) {
      return null;
   }

   public Subject authenticate(String name, Object o) throws AuthenticationException {
      Account account = users.get(name);
      if (account!=null) {
         if (account.getPassword().equals(o.toString())) {
            return new SubjectWrapper(account,true);
         }
      }
      throw new AuthenticationException(AuthenticationException.WRONG_PASSWORD,"Authentication failed.");
   }

   public Account addAccount(Account acnt) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public Account getAccount(Subject sbjct, String name) {
      if (sbjct.hasDbaRole()) {
         return users.get(name);
      }
      return null;
   }

   public boolean hasAccount(Account acnt) {
      return users.get(acnt.getName())!=null;
   }

   public boolean hasAccount(String name) {
      return users.get(name)!=null;
   }

   public boolean updateAccount(Subject sbjct, Account acnt) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public boolean deleteAccount(Subject sbjct, Account acnt) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public Group addGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public Group getGroup(Subject sbjct, String name) {
      return groups.get(name);
   }

   public boolean hasGroup(Group group) {
      return groups.get(group.getName())!=null;
   }

   public boolean hasGroup(String name) {
      return groups.get(name)!=null;
   }

   public boolean updateGroup(Subject subject,Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

   public boolean deleteGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
      throw new SecurityException("Not allowed on a web realm.");
   }

}
