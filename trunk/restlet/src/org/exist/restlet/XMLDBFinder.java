/*
 * ProtocolByIdFinder.java
 *
 * Created on March 27, 2007, 11:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.util.UUID;
import java.util.logging.Level;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 *
 * @author alex
 */
public class XMLDBFinder extends Finder {

   Application app;
   
   /** Creates a new instance of ProtocolByIdFinder */
   public XMLDBFinder(Application app) {
      super(app.getContext());
      this.app = app;
   }
   
   public Resource findTarget(Request request, Response response) {
      return new XMLDBResource(app,request,response,request.getResourceRef().getRemainingPart());
   }
   
}
