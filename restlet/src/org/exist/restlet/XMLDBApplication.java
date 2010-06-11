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
import org.exist.security.User;
import org.exist.storage.BrokerPool;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
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
         getLogger().info("Starting XMLDBApplication on "+this);
         dbName = getContext().getParameters().getFirstValue(XMLDBResource.DBNAME_NAME);
         if (dbName==null) {
            getLogger().severe("There is no "+XMLDBResource.DBNAME_NAME+" parameter for the "+this.getClass().getName());
            return;
         }
         manager = BrokerPool.getInstance(dbName).getSecurityManager();
         super.start();
      }
   }

   @Override
   public Restlet createInboundRoot() {
      ChallengeAuthenticator userGuard = new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"eXist Users");
      userGuard.setVerifier(new Verifier() {
         public int verify(Request request, Response response) {
            ChallengeResponse authInfo = request.getChallengeResponse();
            if (authInfo==null) {
               return Verifier.RESULT_MISSING;
            }
            String identity = authInfo.getIdentifier();
            char [] secret = authInfo.getSecret();
            if (identity==null || secret==null) {
               return Verifier.RESULT_INVALID;
            }
            User user = manager.getUser(identity);
            if (user!=null) {
               boolean valid = user.authenticate(new String(secret));
               if (valid) {
                  request.getAttributes().put(XMLDBResource.USER_NAME,user);
                  return Verifier.RESULT_VALID;
               } else {
                  getLogger().info("Password check failed on "+identity);
                  return Verifier.RESULT_INVALID;
               }
            } else {
               getLogger().info("User "+identity+" not found.");
               return Verifier.RESULT_INVALID;
            }
         }
         
      });
      
      Router router = new Router(getContext());
      userGuard.setNext(router);
      // the default proxy to the database
      router.attachDefault(new Restlet(getContext()) {
         boolean fineLog = getLogger().isLoggable(Level.FINE);
         public void handle(Request request, Response response) {
            try {
               // The reference to the resource
               Reference ref = request.getResourceRef();
               // The remaining part after the matching prefix
               String path = ref.getRemainingPart();

               if (fineLog) {
                  getLogger().fine("Routing request to "+path+" to database "+dbName);
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
               dbRequest.getAttributes().put("org.restlet.http.headers",request.getAttributes().get("org.restlet.http.headers"));

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
