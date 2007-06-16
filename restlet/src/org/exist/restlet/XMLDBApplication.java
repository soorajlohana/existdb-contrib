/*
 * AtomApplication.java
 *
 * Created on March 28, 2007, 12:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;

/**
 *
 * @author alex
 */
public class XMLDBApplication extends Application {

   String dbname;
   /** Creates a new instance of AtomApplication */
   public XMLDBApplication(Context context,String dbname) {
      super(context);
      this.dbname = dbname;
   }
   
   @Override
   public Restlet createRoot() {   
      return new XMLDBFinder(this,dbname);
   }
   
}
