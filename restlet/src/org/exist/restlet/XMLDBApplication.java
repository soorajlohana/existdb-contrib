/*
 * AtomApplication.java
 *
 * Created on March 28, 2007, 12:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.util.logging.Level;
import org.exist.restlet.admin.XMLDBAdminApplication;
import org.exist.restlet.auth.DBUserVerifier;
import org.exist.restlet.auth.SessionManager;
import org.exist.restlet.auth.UserManager;
import org.exist.security.User;
import org.exist.storage.BrokerPool;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class XMLDBApplication extends Application{

   org.exist.security.SecurityManager manager;
   SessionManager sessionManager;
   String dbName;
   public XMLDBApplication(Context context)
   {
      super(context);
   }
   public void start() 
      throws Exception
   {
      if (isStopped()) {
         getLogger().info("Starting XMLDBApplication:");
         dbName = getContext().getParameters().getFirstValue(XMLDBResource.DBNAME_NAME);
         if (dbName==null) {
            getLogger().severe("There is no "+XMLDBResource.DBNAME_NAME+" parameter for the "+this.getClass().getName());
            return;
         }
         String realmName = getContext().getParameters().getFirstValue(XMLDBResource.REALM_NAME);
         if (realmName!=null) {
            getContext().getAttributes().put(XMLDBResource.REALM_NAME,XMLDBResource.getRealm(realmName));
            getLogger().info(dbName+" is using realm "+realmName);
         } else {
            getLogger().info("No user realm for database.");
         }
         manager = BrokerPool.getInstance(dbName).getSecurityManager();
         super.start();
      }
   }

   @Override
   public Restlet createInboundRoot() {
      final String cookieName = getContext().getParameters().getFirstValue(XMLDBResource.COOKIE_NAME);
      if (cookieName!=null) {
         sessionManager = new SessionManager(getContext());
      }
      final String cookiePath = getContext().getParameters().getFirstValue(XMLDBResource.COOKIE_PATH_NAME);
      final boolean isDebugLog = "true".equals(getContext().getParameters().getFirstValue(XMLDBApplication.class.getName()+".debug"));

      ChallengeAuthenticator userGuard = new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"DB Users") {
         public int authenticated(Request request,Response response) {
            if (cookieName!=null) {
               String sessionId = (String)request.getAttributes().get(XMLDBResource.SESSION_NAME);
               if (sessionId==null) {
                  User user = (User)request.getAttributes().get(XMLDBResource.USER_NAME);
                  sessionId = sessionManager.newSession(user);
                  if (isDebugLog) {
                     getLogger().info("Setting session cookie "+cookieName+"="+sessionId+" for "+user.getName());
                  }
                  CookieSetting cookie = new CookieSetting(cookieName,sessionId);
                  cookie.setPath(cookiePath==null ? "/" : cookiePath);
                  response.getCookieSettings().add(cookie);
               }
            }
            return super.authenticated(request, response);
         }
      };

      Verifier verifier = (Verifier)getContext().getAttributes().get(XMLDBResource.VERIFIER_NAME);
      if (verifier==null) {
         String verifierClassName = getContext().getParameters().getFirstValue(XMLDBResource.VERIFIER_CLASS_NAME);
         if (verifierClassName!=null) {
            getLogger().info("Instantiating user verifier "+verifierClassName);
            try {
               Class<? extends Verifier> verifierClass = (Class<? extends Verifier>)this.getClass().getClassLoader().loadClass(verifierClassName);
               try {
                  verifier = verifierClass.getConstructor(Context.class).newInstance(getContext());
               } catch (NoSuchMethodException ex) {
               }
               if (verifier==null) {
                  try {
                     verifier = verifierClass.getConstructor().newInstance();
                  } catch (NoSuchMethodException ex) {
                     getLogger().severe("There is no constructor available for verifier class "+verifierClassName);
                  }
               }
            } catch(Exception ex) {
               getLogger().log(Level.SEVERE,"Error loading verifier class: "+verifierClassName,ex);
            }
         }
         if (verifier==null) {
            getLogger().info("Defaulting to database user verifier.");
            verifier = new DBUserVerifier(getContext(),manager);
         }
      }
      if (verifier instanceof UserManager) {
         final UserManager userManager = (UserManager)verifier;
         // Wrap the user manager instance to hide any implementation and disallow casting.
         getContext().getAttributes().put(XMLDBResource.USER_MANAGER_NAME,new UserManager() {
            public User getUser(String identity) {
               return userManager.getUser(identity);
            }
         });
      }
      if (sessionManager!=null) {
         final Verifier userVerifier = verifier;
         verifier = new Verifier() {
            public int verify(Request request, Response response) {
               Cookie cookie = request.getCookies().getFirst(cookieName);
               if (cookie!=null) {
                  User user = sessionManager.getUser(cookie.getValue());
                  if (user!=null) {
                     if (isDebugLog) {
                        getLogger().info(cookieName+"="+cookie.getValue()+" is valid, user="+user.getName());
                     }
                     request.getAttributes().put(XMLDBResource.USER_NAME,user);
                     request.getAttributes().put(XMLDBResource.SESSION_NAME,cookie.getValue());
                     return Verifier.RESULT_VALID;
                  }
                  if (isDebugLog) {
                     getLogger().info(cookieName+"="+cookie.getValue()+" is invalid.");
                  }
               }
               return userVerifier.verify(request, response);
            }
         };
      }
      userGuard.setVerifier(verifier);

      Router router = new Router(getContext());
      userGuard.setNext(router);
      // the default proxy to the database
      router.attachDefault(new Restlet(getContext()) {
         public void handle(Request request, Response response) {
            try {
               // The reference to the resource
               Reference ref = request.getResourceRef();
               // The remaining part after the matching prefix
               String path = ref.getRemainingPart();

               if (isDebugLog) {
                  getLogger().info("Routing request to "+path+" to database "+dbName);
               }

               // Create a RIAP scheme reference to the database
               Reference dbRef = LocalReference.createRiapReference(LocalReference.RIAP_COMPONENT, "/exist/"+dbName+"/"+path);
               dbRef.setQuery(ref.getQuery());
               dbRef.setFragment(ref.getFragment());

               // Create a database request with the same method
               Request dbRequest = new Request(request.getMethod(),dbRef);
               // Set the entity both from the request
               dbRequest.setEntity(request.getEntity());
               // Copy the headers from the request
               for (String name : request.getAttributes().keySet()) {
                  dbRequest.getAttributes().put(name,request.getAttributes().get(name));
               }

               // Interact with eXist
               Response dbResponse = getContext().getClientDispatcher().handle(dbRequest);

               // Copy the results to the response
               response.setEntity(dbResponse.getEntity());
               response.setStatus(dbResponse.getStatus());
               Object headers = dbResponse.getAttributes().get("org.restlet.http.headers");
               if (headers!=null) {
                  response.getAttributes().put("org.restlet.http.headers",headers);
               }
            } catch (Exception ex) {
               getLogger().log(Level.SEVERE,"Exception during RIAP proxy to database.",ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }
         }
      });
      router.attach("_/admin/",new XMLDBAdminApplication(getContext(),dbName,manager)).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
      if (sessionManager!=null) {
         router.attach("_/logout",new Restlet() {
            public void handle(Request request,Response response) {
               Cookie cookie = request.getCookies().getFirst(cookieName);
               if (cookie!=null) {
                  sessionManager.expireSession(cookie.getValue());
                  CookieSetting unset = new CookieSetting(cookieName,"");
                  unset.setMaxAge(0);
                  unset.setPath(cookiePath);
                  response.getCookieSettings().add(unset);
               }
               response.setStatus(Status.SUCCESS_NO_CONTENT);
               if (isDebugLog) {
                  getLogger().info("Expiring session "+cookie.getValue()+" via logout.");
               }
            }
         });
      }
      return userGuard;
   }
   
}
