/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Subject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 *
 * @author alex
 */
public class RequireUserFilter extends Filter {

   String redirectPath;
   String challenge;
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
      challenge = getContext().getParameters().getFirstValue("challenge");
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
      Subject user = (Subject)request.getAttributes().get(XMLDBResource.USER_NAME);
      if (user==null) {
         if (redirectPath!=null) {
            response.redirectSeeOther(redirectPath);
            response.setStatus(Status.REDIRECTION_SEE_OTHER);
         } else if (challenge!=null) {
            ChallengeResponse authResponse = request.getChallengeResponse();
            if (authResponse!=null) {
               String username = authResponse.getIdentifier();
               String password = new String(authResponse.getSecret());
               UserManager userManager = (UserManager)request.getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
               if (userManager==null) {
                  userManager = (UserManager)getContext().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
               }

               if (userManager!=null) {
                  user = userManager.authenticate(username, password);
                  if (user!=null) {
                     SessionManager sessionManager = (SessionManager)request.getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
                     if (sessionManager==null) {
                        sessionManager = (SessionManager)getContext().getAttributes().get(XMLDBResource.SESSION_MANAGER_NAME);
                     }
                     if (sessionManager!=null) {
                        String sessionId = sessionManager.newSession(user);
                        request.getAttributes().put(XMLDBResource.SESSION_NAME,sessionId);
                     }
                     request.getAttributes().put(XMLDBResource.USER_NAME,user);
                     request.getAttributes().put(XMLDBResource.NEW_USER_NAME,Boolean.TRUE);
                     return Filter.CONTINUE;
                  }
               }

            }
            List<ChallengeRequest> challenges = new ArrayList<ChallengeRequest>();
            challenges.add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC,challenge));
            response.setChallengeRequests(challenges);
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
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
