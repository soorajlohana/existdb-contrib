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
import org.restlet.Client;
import org.restlet.Context;
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
   String collectionPath;
   Client client;
   
   /** Creates a new instance of ProtocolByIdFinder */
   public XMLDBFinder(Context context,String dbname) {
      this(context,dbname,"");
   }
   public XMLDBFinder(Context context,String dbname,String collectionPath) {
      super(context);
      this.dbname = dbname;
      this.client = new Client(XMLDBResource.EXIST);
      this.client.setContext(getContext());
      this.collectionPath = collectionPath;
      if (this.collectionPath==null) {
         this.collectionPath = "";
      }
   }
   
   public Resource findTarget(Request request, Response response) {
      return new XMLDBResource(getContext(),client,request,response,new Reference("exist://"+dbname+collectionPath+request.getResourceRef().getRemainingPart()));
   }
   
}
