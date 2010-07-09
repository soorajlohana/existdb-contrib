/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.util;

import org.restlet.Context;
import org.restlet.data.Reference;

/**
 *
 * @author alex
 */
public class Directory extends org.restlet.resource.Directory {

   public Directory(Context context) {
      super(context,"file:///"); // FIXME: So bad, ...
   }

   public void start() 
      throws Exception
   {
      super.start();
      String directory = getContext().getParameters().getFirstValue("directory");
      if (directory!=null) {
         getLogger().fine("Serving directory: "+directory);
         setRootRef(new Reference(directory));
      }
      String value = getContext().getParameters().getFirstValue("deep");
      if (value!=null) {
         setDeeplyAccessible("true".equals(value));
      }
      value = getContext().getParameters().getFirstValue("modifiable");
      if (value!=null) {
         setModifiable("true".equals(value));
      }
      value = getContext().getParameters().getFirstValue("listings");
      if (value!=null) {
         setListingAllowed("true".equals(value));
      }
      value = getContext().getParameters().getFirstValue("index");
      if (value!=null) {
         setIndexName(value);
      }
      value = getContext().getParameters().getFirstValue("negotiate");
      if (value!=null) {
         setNegotiatingContent("true".equals(value));
      }
   }
}
