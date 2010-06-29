/*
 * LoginForm.java
 *
 * Created on September 7, 2007, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.login;

import java.util.Map;
import java.util.UUID;
import org.exist.restlet.XMLDBResource;
import org.exist.security.User;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

/**
 *
 * @author alex
 */
public class StatusAction extends ServerResource
{
   
   /** Creates a new instance of LoginForm */
   public StatusAction()
   {
      setNegotiated(false);
   }
   
   
   public Representation get()
   {
      User user = (User)getRequest().getAttributes().get(XMLDBResource.USER_NAME);
      Representation rep = null;
      if (user==null) {
         rep = new StringRepresentation("<none/>",MediaType.APPLICATION_XML);
      } else {
         String xml = "<user uid='"+user.getUID()+"' alias='"+user.getName()+"'>";
         String [] groups = user.getGroups();
         if (groups!=null) {
            for (int i=0; i<groups.length; i++) {
               xml += "<group name='"+groups[i]+"'/>";
            }
         }
         xml += "</user>";
         rep = new StringRepresentation(xml,MediaType.APPLICATION_XML);
      }
      rep.setCharacterSet(CharacterSet.UTF_8);
      return rep;
   }
   
}
