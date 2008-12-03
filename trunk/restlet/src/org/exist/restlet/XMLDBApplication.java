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
import org.restlet.Finder;
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
public class XMLDBApplication extends Application {

   String dbname;
   org.exist.security.SecurityManager manager;
   /** Creates a new instance of AtomApplication */
   public XMLDBApplication(Context context,String dbname) 
      throws Exception
   {
      super(context);
      this.dbname = dbname;
      this.manager = BrokerPool.getInstance(dbname).getSecurityManager();
   }
   
   @Override
   public Restlet createRoot() {
      Guard userGuard = new Guard(getContext(),ChallengeScheme.HTTP_BASIC,"eXist Users") {
         public boolean checkSecret(String identity,char [] secret)
         {
            User user = manager.getUser(identity);
            if (user!=null) {
               boolean valid = user.validate(new String(secret),manager);
               return valid;
            } else {
               getLogger().info("User "+identity+" not found.");
               return false;
            }
         }
         
         public void accept(Request request,Response response) {
            String identity = request.getChallengeResponse().getIdentifier();
            User user = manager.getUser(identity);
            request.getAttributes().put(XMLDB.USER_ATTR,user);
            super.accept(request,response);
         }
      };
      
      Finder next = new XMLDBFinder(getContext(),dbname);
      Router router = new Router(getContext());
      userGuard.setNext(router);
      router.attachDefault(next);
      router.attach("/_/admin/",new XMLDBAdminApplication(getContext(),dbname,manager));
      return userGuard;
   }
   
}
