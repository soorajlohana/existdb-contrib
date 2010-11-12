/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exist.EXistException;
import org.exist.security.Account;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.SecurityManager;
import org.exist.security.internal.AccountImpl;
import org.exist.security.internal.RealmImpl;
import org.exist.security.realm.Realm;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;
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
public class UsersResource extends ServerResource {

   public UsersResource() {
      setNegotiated(false);
   }

   protected Representation get() {
      Representation entity = new OutputRepresentation(MediaType.APPLICATION_XML) {

         public void write(OutputStream os)
            throws IOException {
            SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
            Realm realm = manager.getRealm(RealmImpl.ID);
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write("<users>\n");
            for (Account user : realm.getAccounts()) {
               w.write("<user name='");
               w.write(user.getName());
               w.write("' id='");
               w.write(Integer.toString(user.getId()));
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
            }
            w.write("</users>");
            w.flush();
            w.close();
         }
      };
      entity.setCharacterSet(CharacterSet.UTF_8);
      getResponse().setStatus(Status.SUCCESS_OK);
      return entity;
   }

   protected Representation post(Representation entity) {
      if (!entity.getMediaType().equals(MediaType.APPLICATION_XML,true) && !entity.getMediaType().equals(MediaType.TEXT_XML,true)) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return new StringRepresentation("Invalid media type: "+entity.getMediaType());
      }
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         InputSource source = new InputSource(entity.getReader());
         Document doc = builder.parse(source);
         
         Element top = doc.getDocumentElement();
         if (!top.getLocalName().equals("user") || top.getNamespaceURI()!=null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Unrecognized doument element.");
         }
         String name = top.getAttribute("name");
         String password = top.getAttribute("password");

         SecurityManager manager = (SecurityManager) getRequest().getAttributes().get(XMLDBAdminApplication.SECURITY_MANAGER_ATTR);
         Realm realm = manager.getRealm(RealmImpl.ID);

         try {
            Account user = new AccountImpl((RealmImpl)realm,name);
            user.setPassword(password);

            NodeList children = top.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
               Node n = children.item(i);
               if (n.getNodeType()==Document.ELEMENT_NODE) {
                  Element e = (Element)n;
                  if (e.getLocalName().equals("group") && e.getNamespaceURI()==null) {
                     String groupName = e.getTextContent();
                     Group group = manager.getGroup(manager.getSystemSubject(),groupName);
                     if (group!=null) {
                        user.addGroup(group);
                     }
                  }
               }
            }
            manager.updateAccount(manager.getSystemSubject(), user);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         } catch (PermissionDeniedException ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            getLogger().log(Level.SEVERE,"Cannot create account "+name,ex);
         } catch (EXistException ex) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            getLogger().log(Level.SEVERE,"Cannot create account "+name,ex);
         }
         return null;
      } catch (ParserConfigurationException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return new StringRepresentation("Parse error: "+ex.getMessage());
      } catch (SAXException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return new StringRepresentation("Parse error: "+ex.getMessage());
      } catch (IOException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return new StringRepresentation("I/O error: "+ex.getMessage());
      }
   }
}

