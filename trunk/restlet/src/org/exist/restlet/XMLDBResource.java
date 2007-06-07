/*
 * AtomResource.java
 *
 * Created on March 27, 2007, 11:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 *
 * @author alex
 */
public class XMLDBResource extends Resource {
   
   static Protocol EXIST = Protocol.valueOf("exist");
   String path;
   /** Creates a new instance of AtomResource */
   public XMLDBResource(Application app,Request request,Response response,String path) {
      super(app.getContext(),request,response);
      this.path = path;
   }
   
   public Representation getRepresentation(Variant v) {
      getContext().getLogger().info("Getting representation for: "+path);
      return new StringRepresentation("OK",MediaType.TEXT_PLAIN);
   }
   
   public boolean allowGet() { return true; }
   public boolean allowPost() { return true; }
   public boolean allowPut() { return true; }
   public boolean allowDelete() { return true; }
   
   public void handleGet() {
      getContext().getLogger().info("Handling Get...");
      Client client = new Client(EXIST);
      Response response = client.handle(getRequest());
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handlePost() {
      Client client = new Client(EXIST);
      Response response = client.handle(getRequest());
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handleDelete() {
      Client client = new Client(EXIST);
      Response response = client.handle(getRequest());
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handlePut() {
      Client client = new Client(EXIST);
      Response response = client.handle(getRequest());
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handleHead() {
      Client client = new Client(EXIST);
      Response response = client.handle(getRequest());
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
}
