/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.exist.security.PermissionDeniedException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.exist.security.SecurityManager;
import org.exist.security.User;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;

/**
 *
 * @author alex
 */
public class UserResource extends Resource {

   public UserResource(Context context, Request request, Response response) {
      super(context, request, response);
   }

   public void handleGet() {
      String name = getRequest().getAttributes().get("name").toString();
      SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
      final User user = manager.getUser(name);
      if (user==null) {
         getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      } else {
         getResponse().setEntity(new OutputRepresentation(MediaType.APPLICATION_XML) {

            public void write(OutputStream os)
               throws IOException {
               Writer w = new OutputStreamWriter(os, "UTF-8");
               w.write("<user name='");
               w.write(user.getName());
               w.write("' id='");
               w.write(Integer.toString(user.getUID()));
               w.write("'");

               String [] groups = user.getGroups();
               if (groups==null || groups.length==0) {
                  w.write("/>\n");
               } else {
                  w.write(">\n");
                  for (int g=0; g<groups.length; g++) {
                     w.write("<group>");
                     w.write(groups[g]);
                     w.write("</group>\n");
                  }
                  w.write("</user>\n");
               }
               w.flush();
               w.close();
            }
         });
         getResponse().getEntity().setCharacterSet(CharacterSet.UTF_8);
         getResponse().setStatus(Status.SUCCESS_OK);
      }
   }

   public boolean allowDelete() {
      return true;
   }

   public void handleDelete() {
      String name = getRequest().getAttributes().get("name").toString();
      SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
      final User user = manager.getUser(name);
      if (user==null) {
         getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      } else {
         try {
            manager.deleteUser(user);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         } catch (PermissionDeniedException ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
         }
      }
   }

}
