/*
 * WebComponent.java
 *
 * Created on March 26, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import org.restlet.Component;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;

/**
 *
 * @author alex
 */
public class WebComponent extends Component {
   
   /** Creates a new instance of WebComponent */
   public WebComponent(String hostname,String ipAddress, int port) {
      getLogService().setLoggerName("org.exist.restlet.www");
      
      // ------------------
      // Add the connectors
      // ------------------
      getServers().add(Protocol.HTTP, ipAddress, port);
      getClients().add(Protocol.FILE);
      getClients().add(XMLDBResource.EXIST);
      
      // ---------------
      // www.restlet.org
      // ---------------
      VirtualHost host = new VirtualHost(getContext());
      host.setHostDomain(hostname);
      host.setHostPort("80|" + Integer.toString(port));
      
      host.attach(new XMLDBApplication(getContext()));
      
      getHosts().add(host);
      
   }
   
}
