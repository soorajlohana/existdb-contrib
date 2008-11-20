/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exist.security.Group;
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
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

   public boolean allowPost() {
      return true;
   }

   public void handlePost() {
      String name = getRequest().getAttributes().get("name").toString();
      SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
      final User user = manager.getUser(name);
      if (user==null) {
         getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
         return;
      }
      Representation entity = getRequest().getEntity();
      if (!entity.getMediaType().equals(MediaType.APPLICATION_XML,true) && !entity.getMediaType().equals(MediaType.TEXT_XML,true)) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid media type.");
         return;
      }
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         InputSource source = new InputSource(entity.getReader());
         Document doc = builder.parse(source);

         Element top = doc.getDocumentElement();
         if (top.getLocalName().equals("user") && top.getNamespaceURI()==null) {
            String checkName = top.getAttribute("name");
            if (checkName!=null && !checkName.equals(name)) {
               getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"The user name does not match.");
               return;
            }
            String password = top.getAttribute("password");

            if (password!=null) {
               user.setPassword(password);
            }

            NodeList children = top.getChildNodes();
            List<String> groups = new ArrayList<String>();
            for (int i=0; i<children.getLength(); i++) {
               Node n = children.item(i);
               if (n.getNodeType()==Document.ELEMENT_NODE) {
                  Element e = (Element)n;
                  if (e.getLocalName().equals("group") && e.getNamespaceURI()==null) {
                     String groupName = e.getTextContent();
                     Group group = manager.getGroup(groupName);
                     if (group!=null) {
                        groups.add(groupName);
                     }
                  }
               }
            }
            if (groups.size()>0) {
               user.setGroups(groups.toArray(new String[groups.size()]));
            }
            manager.setUser(user);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         } else if (top.getLocalName().equals("password") && top.getNamespaceURI()==null) {
            String password = top.getTextContent();
            if (password.length()==0) {
               password = null;
            }
            user.setPassword(password);
            manager.setUser(user);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Unrecognized doument element.");
         }
      } catch (ParserConfigurationException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Parse error: "+ex.getMessage());
      } catch (SAXException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Parse error: "+ex.getMessage());
      } catch (IOException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"I/O error: "+ex.getMessage());
      }
   }
}
