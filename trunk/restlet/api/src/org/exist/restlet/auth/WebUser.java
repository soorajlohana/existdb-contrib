/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.exist.config.Configuration;
import org.exist.security.AXSchemaType;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.SecurityManager;
import org.exist.security.realm.Realm;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author alex
 */
public class WebUser implements Account {

   int id;
   String name;
   String [] groups;
   String nonPassword;
   boolean hasDbaRole;
   Map<AXSchemaType,String> attributes;
   XmldbURI home;
   Realm realm;
   public WebUser(Realm realm,int id,String name)
   {
      this(realm,id,name,null);
   }
   public WebUser(Realm realm,int id,String name,String [] groups) {
      this.realm = realm;
      this.id = id;
      this.name = name;
      this.groups = groups;
      if (this.groups==null) {
         this.groups = new String[1];
         this.groups[0] = SecurityManager.GUEST_GROUP;
      } else {
         for (int g=0; g<this.groups.length; g++) {
            if (this.groups[g].equals(SecurityManager.DBA_GROUP)) {
               hasDbaRole = true;
            }
         }
      }
      this.attributes = new HashMap<AXSchemaType,String>();
      this.nonPassword = UUID.randomUUID().toString();
      this.home = null;
   }

   public String getName() {
      return name;
   }

   public String getUsername() {
      return name;
   }

   public int getId() {
      return id;
   }

   public XmldbURI getHome() { 
      return home;
   }

   public boolean isEnabled() { return true; }
   public boolean isCredentialsNonExpired() { return true; }
   public boolean isAccountNonLocked() { return true; }
   public boolean isAccountNonExpired() { return true; }
   
	public void setHome(XmldbURI homeCollection) {
      throw new SecurityException("Cannot set the home of a web user.");
   }

   public Group getDefaultGroup() {
      return realm.getGroup(new SubjectWrapper(this,false), getPrimaryGroup());
   }

   public String getDigestPassword() { return nonPassword; }
   public String getPassword() { return nonPassword; }
   public void setPassword(String password) {throw new SecurityException("Cannot set the password of a web user."); }
   public void setUID(int id) { throw new SecurityException("Cannot set the UID of a web user."); }
   public Realm getRealm() { return realm; }
   public String getRealmId() { return realm.getId(); }
   public boolean isAuthenticated() { return false; }
	public boolean authenticate(Object credentials) { return false; }

	public final void remGroup(String group) {
      throw new SecurityException("Cannot remove a group of a web user.");
	}
	public final boolean hasGroup(String group) {
		if (groups == null)
			return false;
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].equals(group))
				return true;
		}
		return false;
	}
	public final String getPrimaryGroup() {
		if (groups == null || groups.length == 0)
			return null;
		return groups[0];
	}
   public Group addGroup(String group) {
      throw new SecurityException("Cannot add a group for a web user.");
   }
   public Group addGroup(Group group) {
      throw new SecurityException("Cannot add a group for a web user.");
   }
	public String[] getGroups() {
      return groups;
   }

	public final void setGroups(String[] groups) {
      throw new SecurityException("Cannot change the groups of a web user.");
	}
   
	public boolean hasDbaRole() {
      return hasDbaRole;
   }
   
	public void setMetadataValue(AXSchemaType key, String value) {
      throw new SecurityException("Cannot set attributes of a web user.");
	}

	public String getMetadataValue(AXSchemaType key) {
		return attributes.get(key);
	}

   public Set<AXSchemaType> getMetadataKeys() {
      return attributes.keySet();
   }

   public void save()
      throws PermissionDeniedException
   {
      throw new PermissionDeniedException("Cannot save a web user.");
   }

   public boolean isConfigured() {
      return false;
   }
   
	public Configuration getConfiguration() {
      throw new SecurityException("Cannot return the configuration of a web user.");
	}

}
