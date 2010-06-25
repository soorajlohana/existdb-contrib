/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.exist.security.Realm;
import org.exist.security.User;
import org.exist.security.SecurityManager;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author alex
 */
public class WebUser implements User {

   int id;
   String name;
   String [] groups;
   String nonPassword;
   boolean hasDbaRole;
   Map<String,Object> attributes;
   XmldbURI home;
   public WebUser(int id,String name)
   {
      this(id,name,null);
   }
   public WebUser(int id,String name,String [] groups) {
      this.id = id;
      this.name = name;
      this.groups = groups;
      if (this.groups==null) {
         this.groups = new String[1];
         this.groups[0] = SecurityManager.GUEST_GROUP;
      }
      this.attributes = new HashMap<String,Object>();
      this.nonPassword = UUID.randomUUID().toString();
      this.home = null;
   }

   public String getName() {
      return name;
   }

   public int getUID() {
      return id;
   }

   public XmldbURI getHome() { 
      return home;
   }
   
	public void setHome(XmldbURI homeCollection) {
      this.home = homeCollection;
   }

   public String getDigestPassword() { return nonPassword; }
   public String getPassword() { return nonPassword; }
   public void setPassword(String password) {throw new SecurityException("Cannot set the password of a web user."); }
   public void setUID(int id) { throw new SecurityException("Cannot set the UID of a web user."); }
   public Realm getRealm() { return null; }
   public boolean isAuthenticated() { return false; }
	public boolean authenticate(Object credentials) { return false; }

	public final void remGroup(String group) {
		if (groups == null) {
			groups = new String[1];
			groups[0] = SecurityManager.GUEST_GROUP;
		} else {
			int len = groups.length;

			String[] rgroup = null;
			if (len > 1)
				rgroup = new String[len - 1];
			else {
				rgroup = new String[1];
				len = 1;
			}

			boolean found = false;
			for (int i = 0; i < len; i++) {
				if (!groups[i].equals(group)) {
					if (found == true)
						rgroup[i - 1] = groups[i];
					else
						rgroup[i] = groups[i];
				} else {
					found = true;
				}
			}
			if (found == true && len == 1)
				rgroup[0] = SecurityManager.GUEST_GROUP;
			groups = rgroup;
		}
		if (SecurityManager.DBA_GROUP.equals(group))
			hasDbaRole = false;
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
   public void addGroup(String group) {
		if (groups == null) {
			groups = new String[1];
			groups[0] = group;
		} else {
			int len = groups.length;
			String[] ngroups = new String[len + 1];
			System.arraycopy(groups, 0, ngroups, 0, len);
			ngroups[len] = group;
			groups = ngroups;
		}
		if (SecurityManager.DBA_GROUP.equals(group)) {
			hasDbaRole = true;
      }
   }
	public String[] getGroups() {
      return groups;
   }

	public final void setGroups(String[] groups) {
		this.groups = groups;
		for (int i = 0; i < groups.length; i++) {
			if (SecurityManager.DBA_GROUP.equals(groups[i])) {
				hasDbaRole = true;
         }
      }
	}
   
	public boolean hasDbaRole() {
      return hasDbaRole;
   }
   
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }
}
