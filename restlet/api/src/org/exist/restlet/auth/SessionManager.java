/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.exist.restlet.XMLDBResource;
import org.exist.security.User;
import org.restlet.Context;

/**
 *
 * @author alex
 */
public class SessionManager {
   static public class Session {
      String id;
      User user;
      long granted;
      Session(User user) {
         this.user = user;
         this.granted = System.currentTimeMillis();
         this.id = UUID.randomUUID().toString();
      }
      boolean isValid(long expiry) {
         return (granted+expiry)>System.currentTimeMillis();
      }
   }
   Context context;
   Map<String,Session> sessions;
   long expiry;
   long lastCheck;
   public SessionManager(Context context) {
      this.context = context;
      this.sessions = new HashMap<String,Session>();
      String expiryS = context.getParameters().getFirstValue(XMLDBResource.COOKIE_EXPIRY_NAME);
      // default to 30 minutes;
      this.expiry = expiryS==null ? 30*60*1000 : Integer.parseInt(expiryS)*1000;
      this.lastCheck = -1;
   }

   public Context getContext() {
      return context;
   }

   public Logger getLogger() {
      return context.getLogger();
   }

   public String newSession(User user) {
      Session session = new Session(user);
      synchronized (sessions) {
         sessions.put(session.id,session);
      }
      return session.id;
   }

   public User getUser(String id) {
      if ((lastCheck+expiry)<System.currentTimeMillis()) {
         // check for expirations
         synchronized (sessions) {
            Set<String> expired = new HashSet<String>();
            for (Session session : sessions.values()) {
               if (session.isValid(expiry)) {
                  expired.add(session.id);
               }
            }
            for (String expiredId : expired) {
               sessions.remove(expiredId);
            }
            lastCheck = System.currentTimeMillis();
         }
      }
      Session session = sessions.get(id);
      if (session!=null) {
         if (session.isValid(expiry)) {
            return session.user;
         }
         synchronized (sessions) {
            sessions.remove(session.id);
         }
      }
      return null;
   }

   public boolean expireSession(String id) {
      synchronized (sessions) {
         return sessions.remove(id)!=null;
      }
   }
}
