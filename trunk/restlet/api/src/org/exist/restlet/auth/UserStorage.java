/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.io.IOException;
import java.lang.String;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Realm;
import org.exist.security.User;
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

   Map<Realm,Map<String,User>> database;
   String key;

   Map<String,Set<String>> databaseUsers;
   long lastModified;
   
   public UserStorage(Context context,Reference location) {
      this.context = context;
      this.location = location;
      this.database = new HashMap<Realm,Map<String,User>>();
      this.databaseUsers = new HashMap<String,Set<String>>();
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
      Realm realm = UserVerifier.getRealm(name.trim());
      Map<String,User> users = getRealm(realm);
      NodeList userElements = realmE.getElementsByTagNameNS(NS, "user");
      for (int i=0; i<userElements.getLength(); i++) {
         User user = loadUser(realm,(Element)userElements.item(i));
         if (user!=null) {
            users.put(user.getName(),user);
         }
      }
   }

   public boolean verifyKey(String value) {
      return value.equals(key);
   }

   public boolean isUserAllowedDatabaseAccess(String database,String user) {
      Set<String> users = databaseUsers.get(database);
      return users==null ? false : users.contains(user);
   }

   protected User loadUser(Realm realm,Element userE) {
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
                  if (groupName.length()>0) {
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
      User user = new WebUser(realm,uid,name,groupsArray);
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

   public Map<String,User> getRealm(Realm realm) {
      Map<String,User> users = database.get(realm);
      if (users==null) {
         users = new HashMap<String,User>();
         database.put(realm,users);
      }
      return users;
   }

}
