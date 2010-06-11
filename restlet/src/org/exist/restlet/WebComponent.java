/*
 * WebComponent.java
 *
 * Created on March 26, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import org.restlet.Component;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 *
 * @author alex
 */
public class WebComponent extends Component {

   public final static String DBCONF_NAME = "org.exist.xmldb.db.conf";

   List<XMLDB> databases;

   public WebComponent() {
      super();
      databases = new ArrayList<XMLDB>();
   }

   public WebComponent(String ref) {
      super(ref);
      databases = new ArrayList<XMLDB>();
      getLogger().info(getContext().getParameters().getValues(DBCONF_NAME));

   }

   public WebComponent(Reference ref) {
      super(ref);
      databases = new ArrayList<XMLDB>();
      getLogger().info(getContext().getParameters().getValues(DBCONF_NAME));
   }

   public WebComponent(Representation xmlConf) {
      super(xmlConf);
      databases = new ArrayList<XMLDB>();
      getLogger().info(getContext().getParameters().getValues(DBCONF_NAME));
   }

   /*
   public WebComponent(String hostname,String ipAddress, int port) {

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
            Application app = new XMLDBApplication(getContext().createChildContext());
            host.attach("/"+db.name+"/",app).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);

         }
      } catch (Exception ex) {
         getLogger().log(Level.SEVERE,"Cannot attach XMLDB application.",ex);
      }
      
      getHosts().add(host);
      
      getInternalRouter().attach("/exist/",XMLDBResource.class).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
      
   }
   */

   public void addDatabase(String name,File confFile) {
      XMLDB db = new XMLDB(name,confFile);
      databases.add(db);
   }

   public void start() 
      throws Exception
   {
      String [] dbConfs = getContext().getParameters().getValuesArray(DBCONF_NAME);
      if (dbConfs!=null) {
         for (int i=0; i<dbConfs.length; i++) {
            dbConfs[i] = dbConfs[i].trim();
            int eqPos = dbConfs[i].indexOf('=');
            if (eqPos<1) {
               getLogger().severe("Invalid db configuration (missing or misplaced equals): "+dbConfs[i]);
               continue;
            }
            String name = dbConfs[i].substring(0,eqPos);
            String fileName = dbConfs[i].substring(eqPos+1).trim();
            if (fileName.length()==0) {
               getLogger().severe("Invalid db configuration (bad file name): "+dbConfs[i]);
               continue;
            }
            File confFile = new File(fileName);
            getLogger().info("Configuration database "+name+" -> "+confFile.getAbsolutePath());
            addDatabase(name,confFile);
         }
      }
      for (XMLDB db : databases) {
         getLogger().info("Starting database: "+db.getName());
         db.start();
      }
      super.start();
   }
   
   public void stop()
      throws Exception
   {
      super.stop();
      for (XMLDB db : databases) {
         getLogger().info("Stopping database: "+db.getName());
         db.stop();
      }
   }

static String fineLog =
"handlers= java.util.logging.ConsoleHandler\n"+
".level= FINE\n"+
"java.util.logging.ConsoleHandler.level = FINE\n"+
"java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n";
;
static String finerLog =
"handlers= java.util.logging.ConsoleHandler\n"+
".level= FINER\n"+
"java.util.logging.ConsoleHandler.level = FINER\n"+
"java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n";
;
static String finestLog =
"handlers= java.util.logging.ConsoleHandler\n"+
".level= FINEST\n"+
"java.util.logging.ConsoleHandler.level = FINEST\n"+
"java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n";
;
    /**
     * Used as bootstrap for configuring and running a component in command
     * line. Just provide as first and unique parameter the URI to the XML file.
     *
     * @param args
     *            The list of in-line parameters.
     */
    public static void main(String[] args) throws Exception {
        try {
           int argStart = 0;
           Level logLevel = Level.INFO;
           if (args.length>2) {
              if (args[0].equals("-l")) {
                 if (args[1].equals("fine")) {
                    logLevel = Level.FINE;
                 } else if (args[1].equals("finer")) {
                    logLevel = Level.FINER;
                 } else if (args[1].equals("finest")) {
                    logLevel = Level.FINEST;
                 }
                 argStart = 2;
              }
           }
           if (logLevel == Level.FINE) {
              try {
                 LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(fineLog.getBytes()));
              } catch (java.io.IOException ex) {
                 ex.printStackTrace();
              }
           } else if (logLevel == Level.FINER) {
              try {
                 LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(finerLog.getBytes()));
              } catch (java.io.IOException ex) {
                 ex.printStackTrace();
              }
           } else if (logLevel == Level.FINEST) {
              try {
                 LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(finestLog.getBytes()));
              } catch (java.io.IOException ex) {
                 ex.printStackTrace();
              }

           }
            if ((args == null) || ((args.length-argStart) != 1)) {
                // Display program arguments
                System.err
                        .println("Can't launch the component. Requires the path to an XML configuration file.\n");
            } else {
                // Create and start the component
                URI currentDirURI = (new File(".")).toURI();
                URI confURI = currentDirURI.resolve(args[argStart]);
                new WebComponent(confURI.toString()).start();
            }
        } catch (Exception e) {
            System.err
                    .println("Can't launch the component.\nAn unexpected exception occurred:");
            e.printStackTrace(System.err);
        }
    }
}
