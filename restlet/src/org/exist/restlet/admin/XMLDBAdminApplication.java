/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.admin;

import org.exist.restlet.XMLDB;
import org.exist.security.User;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;

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
   public Restlet createRoot() {
      Guard userGuard = new Guard(getContext(),ChallengeScheme.HTTP_BASIC,"eXist Administrators") {
         public int authenticate(Request request)
         {
            User user = (User)request.getAttributes().get(XMLDB.USER_ATTR);
            if (user!=null && user.hasDbaRole()) {
               return Guard.AUTHENTICATION_VALID;
            } else {
               getLogger().info("User "+user.getName()+" is not a database administrator.");
               return Guard.AUTHENTICATION_INVALID;
            }
         }

         public boolean authorize(Request request) {
            request.getAttributes().put(SECURITY_MANAGER_ATTR,manager);
            return true;
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
