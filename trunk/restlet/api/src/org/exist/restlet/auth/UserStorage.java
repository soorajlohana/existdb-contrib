/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exist.restlet.XMLDBResource;
import org.exist.security.SecurityManager;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.Subject;
import org.exist.security.realm.Realm;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alex
 */
public class UserStorage {

   public static String NS = "http://www.exist-db.org/V/user-list/2010/1/0";
   Context context;
   Reference location;

   Map<String,Realm> database;
   Map<String,Subject> systems;
   String key;

   Map<String,Set<String>> databaseUsers;
   long lastModified;
   
   public UserStorage(Context context,Reference location) {
      this.context = context;
      this.location = location;
      this.database = new HashMap<String,Realm>();
      this.databaseUsers = new HashMap<String,Set<String>>();
      this.systems = new HashMap<String,Subject>();
      this.key = context.getParameters().getFirstValue(XMLDBResource.USER_MANAGER_KEY_NAME);
   }

   public Logger getLogger() {
      return context.getLogger();
   }

   public void load() 
      throws IOException,ParserConfigurationException,SAXException
   {
      if (location==null) {
         return;
      }
      Client client = context.getClientDispatcher();
      Response response = client.handle(new Request(Method.GET,location));
      if (!response.getStatus().isSuccess()) {
         throw new IOException("Cannot get users from location "+location+" due to status "+response.getStatus().getCode());
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource source = new InputSource(response.getEntity().getReader());
      Document doc = builder.parse(source);

      String value = doc.getDocumentElement().getAttribute("key");
      if (value!=null) {
         value = value.trim();
         if (value.length()>0) {
            this.key = value;
         }
      }

      NodeList realms = doc.getDocumentElement().getElementsByTagNameNS(NS, "realm");
      for (int i=0; i<realms.getLength(); i++) {
         Element realm = (Element)realms.item(i);
         loadRealm(realm);
      }

   }

   protected void loadRealm(Element realmE) {
      String name = realmE.getAttribute("name");
      if (name==null) {
         getLogger().severe("Skipping realm element without a name attribute.");
         return;
      }
      getLogger().info("Loading users for realm "+name);
      WebRealm realm = new WebRealm(name,null,null);
      database.put(realm.getId(), realm);
      NodeList groupElements = realmE.getElementsByTagNameNS(NS, "group");
      realm.groups.put(SecurityManager.DBA_GROUP, new WebGroup(name,0,SecurityManager.DBA_GROUP));
      for (int i=0; i<groupElements.getLength(); i++) {
         Element groupE = (Element)groupElements.item(i);
         String groupName = groupE.getAttribute("name");
         if (name==null) {
            getLogger().severe("Ignoring group in realm "+name+" missing the 'name' attribute.");
            continue;
         }
         groupName = groupName.trim();
         String value = groupE.getAttribute("id");
         if (value==null) {
            getLogger().severe("Ignoring group "+groupName+" in realm "+name+" missing the 'id' attribute.");
            continue;
         }
         int id = -1;
         try {
            id = Integer.parseInt(value);
         } catch (NumberFormatException ex) {
            getLogger().severe("Ignoring group "+groupName+" in realm "+name+" with bad uid value "+value+".");
            continue;
         }
         Group group = new WebGroup(name,id,groupName);
         realm.groups.put(group.getName(),group);
      }
      NodeList userElements = realmE.getElementsByTagNameNS(NS, "user");
      for (int i=0; i<userElements.getLength(); i++) {
         Account user = loadUser(realm,(Element)userElements.item(i));
         if (user!=null) {
            realm.users.put(user.getName(),user);
         }
      }
      loadSystemUsers(realm);
   }

   protected void loadSystemUsers(WebRealm realm) {
      String [] dbaGroups = { SecurityManager.DBA_GROUP };
      WebUser dba = new WebUser(realm,0,"system-dba",dbaGroups);
      realm.users.put(dba.getName(),dba);
      String [] guestGroups = { SecurityManager.GUEST_GROUP };
      WebUser guest = new WebUser(realm,0,"system-guest",guestGroups);
      realm.users.put(guest.getName(),guest);
      systems.put(realm.getId()+"-system", new SubjectWrapper(dba,false));
      systems.put(realm.getId()+"-guest", new SubjectWrapper(guest,false));
   }

   public boolean verifyKey(String value) {
      return value.equals(key);
   }

   public boolean isUserAllowedDatabaseAccess(String database,String user) {
      Set<String> users = databaseUsers.get(database);
      return users==null ? false : users.contains(user);
   }

   protected Account loadUser(WebRealm realm,Element userE) {
      String name = userE.getAttribute("name");
      if (name==null) {
         getLogger().severe("Ignoring user that is missing a 'name' attribute for realm "+realm+".");
         return null;
      }
      name = name.trim();
      String value = userE.getAttribute("id");
      if (value==null) {
         getLogger().severe("Ignoring user "+name+" in realm "+realm+" that is missing the 'id' attribute.");
         return null;
      }
      int uid = -1;
      try {
         uid = Integer.parseInt(value);
      } catch (NumberFormatException ex) {
         getLogger().severe("Ignoring user "+name+" in realm "+realm+" with bad uid value "+value+".");
         return null;
      }
      getLogger().info("User: "+name+" -> "+uid);
      Set<String> groups = new HashSet<String>();
      Node child = userE.getFirstChild();
      while (child!=null) {
         if (child.getNodeType()==Node.ELEMENT_NODE) {
            Element childE = (Element)child;
            if (NS.equals(child.getNamespaceURI()) && child.getLocalName().equals("group")) {
               String groupName = childE.getAttribute("name");
               if (groupName!=null) {
                  groupName = groupName.trim();
                  if (groupName.length()>0 && realm.groups.get(groupName)!=null) {
                     groups.add(groupName);
                  }
               }
            } else if (NS.equals(child.getNamespaceURI()) && child.getLocalName().equals("grant")) {
               String dbName = childE.getAttribute("database");
               if (dbName!=null) {
                  dbName = dbName.trim();
                  if (dbName.length()>0) {
                     grantUserAccess(dbName,name);
                  }
               }
            }
         }
         child = child.getNextSibling();
      }
      String [] groupsArray = groups.toArray(new String[0]);
      Account user = new WebUser(realm,uid,name,groupsArray);
      return user;
   }
   
   public void check() {
   }

   public void grantUserAccess(String database,String user) {
      Set<String> users = null;
      synchronized (databaseUsers) {
         users = databaseUsers.get(database);
         if (users==null) {
            users = new HashSet<String>();
            databaseUsers.put(database,users);
         }
      }
      synchronized (users) {
         users.add(user);
      }
   }

   public void revokeUserAccess(String database,String user) {
      Set<String> users = databaseUsers.get(database);
      synchronized (users) {
         users.remove(user);
      }
   }

   public Subject getSystemSubject(Realm realm) {
      return systems.get(realm.getId()+"-system");
   }

   public Subject getGuestSubject(Realm realm) {
      return systems.get(realm.getId()+"-guest");
   }

   public Realm getRealm(String name) {
      return database.get(name);
   }

}
