/*
 * LoginView.java
 *
 * Created on September 7, 2007, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.logging.Level;
import org.exist.restlet.XMLDBResource;
import org.exist.security.Subject;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;
import org.restlet.util.Resolver;

/**
 *
 * @author alex
 */
public class LoginView extends LoginAction
{
   
   static Reference getReferenceAttribute(Request request,String name,Reference defaultValue)
   {
      Object o = request.getAttributes().get(name);
      return o==null ? defaultValue : (Reference)o;
   }

   static String toString(InputStream is)
      throws IOException
   {
      if (is==null) {
         return null;
      }
      StringBuilder builder = new StringBuilder();
      Reader r = new InputStreamReader(is,"UTF-8");
      char [] buffer = new char[1024];
      int len;
      while ((len=r.read(buffer))>0) {
         builder.append(buffer,0,len);
      }
      is.close();
      return builder.toString();
   }

   /**
    * Creates a new instance of LoginView
    */
   public LoginView()
   {
   }
   
   
   public Representation get()
   {
      final Subject user = (Subject)getRequest().getAttributes().get(XMLDBResource.USER_NAME);
      if (user==null) {
         final Form form = getRequest().getResourceRef().getQueryAsForm();
         try {
            LoginApplication.NonceCache nonceCache = (LoginApplication.NonceCache)getContext().getAttributes().get(LoginApplication.NONCES);
            final Template template = new Template(toString(LoginView.class.getResourceAsStream("login-form.xml")));
            final String action = form.getFirstValue("action")==null ? getRequest().getResourceRef().getPath() : form.getFirstValue("action");
            final String formAction = action;
            final String url = form.getFirstValue("url")==null ? getRequest().getResourceRef().toString() : form.getFirstValue("url");
            final int nonce = nonceCache.get();
            final String result = template.format(new Resolver<String>() {
               public String resolve(String name) {
                  if (name.equals("action")) {
                     return formAction;
                  } else if (name.equals("url")) {
                     return url;
                  } else if (name.equals("nonce")) {
                     return Integer.toString(nonce);
                  } else {
                     return form.getFirstValue(name);
                  }
               }
            });
            return new OutputRepresentation(MediaType.APPLICATION_XHTML) {
               public void write(OutputStream os) 
                  throws IOException
               {
                  OutputStreamWriter w = new OutputStreamWriter(os,"UTF-8");
                  w.write(result);
                  w.flush();
               }
            };
         } catch (IOException ex) {
            getLogger().log(Level.SEVERE,"Cannot get template.",ex);
            return null;
         }
      } else {
         try {
            final Template template = new Template(toString(LoginView.class.getResourceAsStream("logged-in.xml")));
            final String result = template.format(new Resolver<String>() {
               public String resolve(String name) {
                  if (name.equals("id")) {
                     return Integer.toString(user.getId());
                  } else if (name.equals("alias")) {
                     return user.getName();
                  }
                  return null;
               }
            });
            
            return new OutputRepresentation(MediaType.APPLICATION_XHTML) {
               public void write(OutputStream os) 
                  throws IOException
               {
                  OutputStreamWriter w = new OutputStreamWriter(os,"UTF-8");
                  w.write(result);
                  w.flush();
               }
            };
         } catch (IOException ex) {
            getLogger().log(Level.SEVERE,"Cannot get template.",ex);
            return null;
         }
         
      }
   }
   
}
