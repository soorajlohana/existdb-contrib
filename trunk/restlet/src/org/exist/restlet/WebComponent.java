/*
 * WebComponent.java
 *
 * Created on March 26, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.routing.Template;
import org.restlet.routing.VirtualHost;

/**
 *
 * @author alex
 */
public class WebComponent extends Component {

   List<XMLDB> databases;
   
   /** Creates a new instance of WebComponent */
   public WebComponent(String hostname,String ipAddress, int port) {

      databases = new ArrayList<XMLDB>();
      getLogService().setLoggerName("org.exist.restlet.www");
      
      // ------------------
      // Add the connectors
      // ------------------
      getServers().add(Protocol.HTTP, ipAddress.equals("*") ? null : ipAddress, port);
      getClients().add(Protocol.FILE);
      
      // ---------------
      // www.restlet.org
      // ---------------
      Context context = getContext().createChildContext();
      context.setLogger(getLogger());
      VirtualHost host = new VirtualHost();
      if (!hostname.equals("*")) {
         host.setHostDomain(hostname);
      }
      host.setHostPort(Integer.toString(port));

      try {
         for (XMLDB db : databases) {
            host.attach("/"+db.name+"/",new XMLDBApplication(getContext().createChildContext(),db.name)).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
         }
      } catch (Exception ex) {
         getLogger().log(Level.SEVERE,"Cannot attach XMLDB application.",ex);
      }
      
      getHosts().add(host);
      
      getInternalRouter().attach("/exist/",XMLDBResource.class).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
      
   }

   public void addDatabase(String name,File confFile) {
      XMLDB db = new XMLDB(name,confFile);
      databases.add(db);
   }

   public void start() 
      throws Exception
   {
      super.start();
      for (XMLDB db : databases) {
         db.start();
      }
   }
   
   public void stop()
      throws Exception
   {
      for (XMLDB db : databases) {
         db.stop();
      }
      super.stop();
   }

}
