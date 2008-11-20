/*
 * WebComponent.java
 *
 * Created on March 26, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.util.logging.Level;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;

/**
 *
 * @author alex
 */
public class WebComponent extends Component {
   
   /** Creates a new instance of WebComponent */
   public WebComponent(String dbname,String hostname,String ipAddress, int port) {
      getLogService().setLoggerName("org.exist.restlet.www");
      
      // ------------------
      // Add the connectors
      // ------------------
      getServers().add(Protocol.HTTP, ipAddress.equals("*") ? null : ipAddress, port);
      getClients().add(Protocol.FILE);
      getClients().add(XMLDBResource.EXIST);
      
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
         host.attach(new XMLDBApplication(getContext().createChildContext(),dbname));
      } catch (Exception ex) {
         getLogger().log(Level.SEVERE,"Cannot attach XMLDB application.",ex);
      }
      
      getHosts().add(host);
      
   }
   
}
