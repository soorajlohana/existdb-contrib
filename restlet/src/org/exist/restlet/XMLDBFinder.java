/*
 * ProtocolByIdFinder.java
 *
 * Created on March 27, 2007, 11:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import org.restlet.Application;
import org.restlet.Finder;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 *
 * @author alex
 */
public class XMLDBFinder extends Finder {

   Application app;
   String dbname;
   
   /** Creates a new instance of ProtocolByIdFinder */
   public XMLDBFinder(Application app,String dbname) {
      super(app.getContext());
      this.app = app;
      this.dbname = dbname;
   }
   
   public Resource findTarget(Request request, Response response) {
      return new XMLDBResource(app,request,response,new Reference("exist://"+dbname+request.getResourceRef().getRemainingPart()));
   }
   
}