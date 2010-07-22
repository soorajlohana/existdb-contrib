/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.HashSet;
import java.util.Set;
import org.exist.restlet.XMLDBResource;
import org.exist.security.User;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 *
 * @author alex
 */
public class RequireUserFilter extends Filter {

   String redirectPath;
   Set<String> groups;

   public RequireUserFilter() {
   }

   public RequireUserFilter(Context context) {
      super(context);
   }

   public void start()
      throws Exception
   {
      redirectPath = getContext().getParameters().getFirstValue("redirect");
      groups = new HashSet<String>();
      String [] groupsList = getContext().getParameters().getValuesArray("groups");
      for (int i=0; groupsList!=null && i<groupsList.length; i++) {
         String [] groupNames = groupsList[i].trim().split(",");
         for (int j=0; j<groupNames.length; j++) {
            groups.add(groupNames[j].trim());
         }
      }
      super.start();
   }

   protected int beforeHandle(Request request, Response response)
   {
      User user = (User)request.getAttributes().get(XMLDBResource.USER_NAME);
      if (user==null) {
         if (redirectPath!=null) {
            response.redirectSeeOther(redirectPath);
            response.setStatus(Status.REDIRECTION_SEE_OTHER);
         } else {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
         }
         return Filter.STOP;
      }

      for (String group : groups) {
         if (!user.hasGroup(group)) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return Filter.STOP;
         }
      }

      return Filter.CONTINUE;
   }
   
}
