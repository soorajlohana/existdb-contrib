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
import org.exist.restlet.auth.SessionManager;
import org.exist.restlet.auth.UserManager;
import org.exist.restlet.auth.UserVerifier;
import org.exist.storage.BrokerPool;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
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
            getContext().getAttributes().put(XMLDBResource.REALM_NAME,UserVerifier.getRealm(realmName));
            getLogger().info(dbName+" is using realm "+realmName);
         } else if (getContext().getAttributes().get(XMLDBResource.REALM_NAME)==null) {
            getLogger().info("No user realm for database.");
         }
         manager = BrokerPool.getInstance(dbName).getSecurityManager();
         getContext().getAttributes().put(XMLDBResource.DB_SECURITY_MANAGER, manager);
         super.start();
      }
   }

   @Override
   public Restlet createInboundRoot() {
      final boolean isDebugLog = "true".equals(getContext().getParameters().getFirstValue(XMLDBApplication.class.getName()+".debug"));

      ChallengeAuthenticator userGuard = new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"DB Users");

      UserManager userManager = (UserManager)getContext().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
      if (userManager==null) {
         String userManagerClassName = getContext().getParameters().getFirstValue(XMLDBResource.USER_MANAGER_CLASS_NAME);
         if (userManagerClassName!=null) {
            getLogger().info("Instantiating user manager "+userManagerClassName);
            try {
               Class<? extends UserManager> userManagerClass = (Class<? extends UserManager>)this.getClass().getClassLoader().loadClass(userManagerClassName);
               try {
                  userManager = userManagerClass.getConstructor(Context.class).newInstance(getContext());
               } catch (NoSuchMethodException ex) {
               }
               if (userManager==null) {
                  try {
                     userManager = userManagerClass.getConstructor().newInstance();
                  } catch (NoSuchMethodException ex) {
                     getLogger().severe("There is no constructor available for verifier class "+userManagerClassName);
                  }
               }
            } catch(Exception ex) {
               getLogger().log(Level.SEVERE,"Error loading verifier class: "+userManagerClassName,ex);
            }
         }
         if (userManager==null) {
            getLogger().info("Defaulting to request level verification.");
            Verifier verifier = new Verifier() {
               public int verify(Request request, Response response) {
                  UserManager requestManager = (UserManager)request.getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
                  if (requestManager==null) {
                     return Verifier.RESULT_INVALID;
                  }
                  return requestManager.verify(request, response);
               }
            };
            userGuard.setVerifier(verifier);
         }
      }
      if (userManager!=null) {
         userGuard.setVerifier(userManager);
         getContext().getAttributes().put(XMLDBResource.USER_MANAGER_NAME,userManager);
      }

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
      return userGuard;
   }
   
}
