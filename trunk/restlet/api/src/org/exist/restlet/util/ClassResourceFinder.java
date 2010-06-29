/*
 * ClassResourceFinder.java
 *
 * Created on September 7, 2007, 11:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 *
 * @author alex
 */
public class ClassResourceFinder extends Finder
{

   Class baseClass;
   String packageName;
   
   /** Creates a new instance of ClassResourceFinder */
   public ClassResourceFinder(Context context,Class baseClass,String path)
   {
      super(context);
      this.baseClass = baseClass;
      this.packageName = path.length()>0 && path.charAt(0)=='/' ? path : "/"+baseClass.getPackage().getName().replace('.','/')+"/"+path;
      if (!this.packageName.endsWith("/")) {
         this.packageName += "/";
      }
   }
   
   public ServerResource find(Request request,Response response)
   {
      String path = packageName+request.getResourceRef().getRemainingPart();
      ServerResource r = new ClassResource(baseClass,path);
      r.setRequest(request);
      r.setResponse(response);
      return r;
   }
   
}
