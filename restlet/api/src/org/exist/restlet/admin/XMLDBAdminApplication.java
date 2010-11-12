/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.admin;

import org.exist.restlet.XMLDBResource;
import org.exist.security.AuthenticationException;
import org.exist.security.Subject;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

/**
 *
 * @author alex
 */
public class XMLDBAdminApplication extends Application {

   public final static String SECURITY_MANAGER_ATTR = "org.exist.xmldb.securityManager";
   String dbname;
   org.exist.security.SecurityManager manager;
   /** Creates a new instance of AtomApplication */
   public XMLDBAdminApplication(Context context,String dbname,org.exist.security.SecurityManager manager)
   {
      super(context);
      this.dbname = dbname;
      this.manager = manager;
   }

   @Override
   public Restlet createInboundRoot() {
      Filter userGuard = new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"eXist Administrators") {

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
            try {
               Subject user = manager.authenticate(identity, new String(secret));
               if (user!=null && user.hasDbaRole()) {
                  boolean valid = user.authenticate(new String(secret));
                  request.getAttributes().put(XMLDBResource.USER_NAME,user);
                  request.getAttributes().put(SECURITY_MANAGER_ATTR,manager);
                  return valid;
               } else {
                  if (user!=null) {
                     getLogger().info("User "+user.getName()+" is not a database administrator.");
                  } else {
                     getLogger().info("User "+identity+" not found.");
                  }
                  return false;
               }
            } catch (AuthenticationException ex) {
               getLogger().info("User "+identity+" did not pass authentication for administration.");
               return false;
            }
         }

      };

      Router router = new Router(getContext());
      userGuard.setNext(router);

      router.attach("users",UsersResource.class);
      router.attach("users/",UsersResource.class);
      router.attach("users/{name}",UserResource.class);

      return userGuard;
   }
}
