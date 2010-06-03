/*
 * AtomApplication.java
 *
 * Created on March 28, 2007, 12:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

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
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

/**
 *
 * @author alex
 */
public class XMLDBApplication extends Application{

   org.exist.security.SecurityManager manager;
   String dbName;
   /** Creates a new instance of AtomApplication */
   public XMLDBApplication(Context context,String dbName)
      throws Exception
   {
      super(context);
      this.dbName = dbName;
      manager = BrokerPool.getInstance(dbName).getSecurityManager();
   }

   @Override
   public Restlet createRoot() {
      Filter userGuard = new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"eXist Users") {
         public boolean authenticate(Request request,Response response)
         {
            ChallengeResponse authInfo = request.getChallengeResponse();
            if (authInfo==null) {
               return false;
            }
            String identity = authInfo.getIdentifier();
            char [] secret = authInfo.getSecret();
            if (identity==null || secret==null) {
               return false;
            }
            User user = manager.getUser(identity);
            if (user!=null) {
               boolean valid = user.authenticate(new String(secret));
               request.getAttributes().put(XMLDBResource.USER_ATTR,user);
               return valid;
            } else {
               getLogger().info("User "+identity+" not found.");
               return false;
            }
         }
         
      };
      
      Router router = new Router(getContext());
      userGuard.setNext(router);
      // the default proxy to the database
      router.attachDefault(new Restlet() {
         public void handle(Request request, Response response) {
            Reference ref = request.getResourceRef();
            String path = ref.getRemainingPart();
            Reference dbRef = LocalReference.createRiapReference(LocalReference.RIAP_COMPONENT, "/exist/"+dbName+"/"+path);
            dbRef.setQuery(ref.getQuery());
            dbRef.setFragment(ref.getFragment());
            Request dbRequest = new Request(request.getMethod(),dbRef);
            dbRequest.setEntity(request.getEntity());
            dbRequest.getAttributes().put("org.restlet.http.headers",request.getAttributes().get("org.restlet.http.headers"));
            Response dbResponse = getContext().getClientDispatcher().handle(dbRequest);
            response.setEntity(dbResponse.getEntity());
            response.setStatus(dbResponse.getStatus());
            response.getAttributes().put("org.restlet.http.headers",dbResponse.getAttributes().get("org.restlet.http.headers"));
         }
      });
      router.attach("/_/admin/",new XMLDBAdminApplication(getContext(),dbName,manager));
      return userGuard;
   }
   
}
