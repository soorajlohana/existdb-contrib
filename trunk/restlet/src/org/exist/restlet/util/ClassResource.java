/*
 * ClassResource.java
 *
 * Created on September 7, 2007, 11:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.util;

import java.io.InputStream;
import java.util.logging.Level;
import org.restlet.Application;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

/**
 *
 * @author alex
 */
public class ClassResource extends ServerResource
{
   
   Class baseClass;
   String path;
   MediaType type;
   
   /** Creates a new instance of ClassResource */
   public ClassResource(Class baseClass,String path)
   {
      setNegotiated(false);
      this.baseClass = baseClass;
      this.path = path;
      int extPos = path.lastIndexOf('.');
      Application app = this.getApplication();
      type = app.getMetadataService().getDefaultMediaType(); 
      if (extPos>=0) {
         String ext = path.substring(extPos+1);
         Metadata mdata = this.getApplication().getMetadataService().getMetadata(ext);
         if (mdata!=null) {
            type = MediaType.valueOf(mdata.getName());
         }
      }
   }
   
   public Representation get()
   {
      if (getLogger().isLoggable(Level.FINE)) {
         getLogger().info("Class resource: "+path);
      }
      InputStream is = baseClass.getResourceAsStream(path);
      if (is==null) {
         return null;
      } else {
         return new InputRepresentation(is,type);
      }
   }
   
}
