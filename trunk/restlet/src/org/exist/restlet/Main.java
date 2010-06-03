/*
 * Main.java
 *
 * Created on March 26, 2007, 2:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.io.File;

/**
 *
 * @author alex
 */
public class Main {
   
   /** Creates a new instance of Main */
   public Main() {
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      if (args.length<4) {
         System.err.println("org.exist.restlet.Main hostname ipaddress port name=conf.xml ...");
         System.exit(1);
      }
      
      try {
         
         String host = args[0];
         String ipAddress = args[1];
         int port = Integer.parseInt(args[2]);
         
         WebComponent www = new WebComponent(host,ipAddress,port);
         for (int i=3; i<args.length; i++) {
            int eqPos = args[i].indexOf('=');
            if (eqPos<=0) {
               System.err.println("Bad database configuration: "+args[i]);
               System.exit(1);
            }
            String name = args[i].substring(0,eqPos);
            String ref = args[i].substring(eqPos+1);
            www.addDatabase(name, new File(ref));
         }
         www.start();

      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
   
}