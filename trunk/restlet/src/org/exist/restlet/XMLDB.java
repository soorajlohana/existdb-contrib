/*
 * Main.java
 *
 * Created on March 29, 2007, 3:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.io.File;
import org.exist.EXistException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.util.Configuration;
import org.exist.util.DatabaseConfigurationException;
import org.exist.xmldb.DatabaseInstanceManager;
import org.exist.xmldb.ShutdownListener;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author alex
 */
public class XMLDB
{
   
   final static String DEFAULT_URI = "xmldb:exist://" + DBBroker.ROOT_COLLECTION;
   final static String DRIVER = "org.exist.xmldb.DatabaseImpl";
   static class ShutdownListenerImpl implements ShutdownListener {

      public void shutdown(String dbname, int remainingInstances) {
         System.err.println("Shutdown of "+dbname+", remaining="+remainingInstances);
      }
   }
   
   File configFile;
   Collection top;
   
   /** Creates a new instance of Main */
   public XMLDB(File configFile)
   {
      this.configFile = configFile;
   }
   
   
   /**
    * @param args the command line arguments
    */
   public void start()
      throws Exception
   {
      // Setup the log4j configuration
      String log4j = System.getProperty("log4j.configuration");
      if (log4j == null) {
         File lf = new File("log4j.xml");
         if (lf.canRead()) {
            System.setProperty("log4j.configuration", lf.toURI().toASCIIString());
         }
      }
      
      System.setProperty("exist.home",configFile.getAbsoluteFile().getParent());
      
      int threads = 5;
      // Configure the database
      Configuration config = new Configuration(configFile.getAbsolutePath());
      BrokerPool.configure( 1, threads, config );
      BrokerPool.getInstance().registerShutdownListener(new ShutdownListenerImpl());

      // Load the database & initiate
      Class cl = Class.forName(DRIVER);
      Database database = (Database)cl.newInstance();
      database.setProperty("create-database", "true");
      DatabaseManager.registerDatabase(database);

      top = DatabaseManager.getCollection(DEFAULT_URI, "admin", null);
         
   }
   
   public void stop() 
      throws Exception
   {
      DatabaseInstanceManager manager = (DatabaseInstanceManager)top.getService("DatabaseInstanceManager","1.0");
      manager.shutdown();
      top.close();
   }
   
}
