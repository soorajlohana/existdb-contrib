/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.io.File;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.util.Configuration;
import org.exist.xmldb.ShutdownListener;
import org.exist.xmldb.XmldbURI;

/**
 *
 * @author alex
 */
public class XMLDB
{

   final static String DRIVER = "org.exist.xmldb.DatabaseImpl";
   static class ShutdownListenerImpl implements ShutdownListener {

      public void shutdown(String dbname, int remainingInstances) {
         System.err.println("Shutdown of "+dbname+", remaining="+remainingInstances);
      }
   }

   File configFile;
   String name;
   BrokerPool pool;

   /** Creates a new instance of Main */
   public XMLDB(String name,File configFile)
   {
      this.name = name;
      this.configFile = configFile;
      this.pool = null;
   }

   public String getName() {
      return name;
   }

   public File getConfigurationFile() {
      return configFile;
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

      int threads = 5;
      // Configure the database
      Configuration config = new Configuration(configFile.getAbsolutePath());
      BrokerPool.configure(name, 1, threads, config );
      pool = BrokerPool.getInstance(name);
      pool.registerShutdownListener(new ShutdownListenerImpl());

   }

   public void reindex(String path)
      throws Exception
   {
      DBBroker broker = null;
      try {
         broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         broker.reindexCollection(XmldbURI.create(path));
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }
   }

   public void stop()
      throws Exception
   {
      pool.shutdown();
   }

}

