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
public class UsersResource extends Resource {

   public UsersResource(Context context, Request request, Response response) {
      super(context, request, response);
   }

   public void handleGet() {
      getResponse().setEntity(new OutputRepresentation(MediaType.APPLICATION_XML) {

         public void write(OutputStream os)
            throws IOException {
            SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
            User[] users = manager.getUsers();
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write("<users>\n");
            for (int i = 0; i < users.length; i++) {
               w.write("<user name='");
               w.write(users[i].getName());
               w.write("' id='");
               w.write(Integer.toString(users[i].getUID()));
               w.write("'");

               String [] groups = users[i].getGroups();
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
            }
            w.write("</users>");
            w.flush();
            w.close();
         }
      });
      getResponse().getEntity().setCharacterSet(CharacterSet.UTF_8);
      getResponse().setStatus(Status.SUCCESS_OK);
   }

   public boolean allowPost() {
      return true;
   }

   public void handlePost() {
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
         if (!top.getLocalName().equals("user") || top.getNamespaceURI()!=null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Unrecognized doument element.");
            return;
         }
         String name = top.getAttribute("name");
         String password = top.getAttribute("password");

         SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);

         User user = new User(name);
         user.setPassword(password);

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
      } catch (ParserConfigurationException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Parse error: "+ex.getMessage());
      } catch (SAXException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Parse error: "+ex.getMessage());
      } catch (IOException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"I/O error: "+ex.getMessage());
      }
   }
}

