/*
 * AtomResource.java
 *
 * Created on March 27, 2007, 11:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.WrapperRequest;

/**
 *
 * @author alex
 */
public class XMLDBResource extends Resource {
   
   public static Protocol EXIST = Protocol.valueOf("exist");
   Reference reference;
   Client client;
   /** Creates a new instance of AtomResource */
   public XMLDBResource(Context context,Client client,Request request,Response response,Reference ref) {
      super(context,request,response);
      this.reference = ref;
      this.client = client;
   }
   
   public Representation getRepresentation(Variant v) {
      getContext().getLogger().info("Getting representation for: "+reference);
      return new StringRepresentation("OK",MediaType.TEXT_PLAIN);
   }
   
   public boolean allowGet() { return true; }
   public boolean allowPost() { return true; }
   public boolean allowPut() { return true; }
   public boolean allowDelete() { return true; }
   
   public void handleGet() {
      Response response = client.handle(new WrapperRequest(getRequest()) {
         public Reference getResourceRef() {
            return reference;
         }
      });
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handlePost() {
      Response response = client.handle(new WrapperRequest(getRequest()) {
         public Reference getResourceRef() {
            return reference;
         }
      });
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handleDelete() {
      Response response = client.handle(new WrapperRequest(getRequest()) {
         public Reference getResourceRef() {
            return reference;
         }
      });
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handlePut() {
      Response response = client.handle(new WrapperRequest(getRequest()) {
         public Reference getResourceRef() {
            return reference;
         }
      });
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
   public void handleHead() {
      Response response = client.handle(new WrapperRequest(getRequest()) {
         public Reference getResourceRef() {
            return reference;
         }
      });
      Response outgoing = getResponse();
      outgoing.setEntity(response.getEntity());
      outgoing.setStatus(response.getStatus());
   }
   
}
