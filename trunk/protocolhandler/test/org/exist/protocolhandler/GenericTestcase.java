/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.protocolhandler;

import java.net.URL;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import org.apache.log4j.PropertyConfigurator;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.util.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XPathQueryService;

/**
 *
 * @author wessels
 */
public class GenericTestcase {

    protected static Logger LOG = null;
    protected static XPathQueryService xpqservice;
    protected static Collection rootCollection = null;
    protected static Database database = null;
    protected static CollectionManagementService cmservice = null;
    protected static BrokerPool brokerPool = null;

    public static void initLog4J() {
        Layout layout = new PatternLayout("%d [%t] %-5p (%F [%M]:%L) - %m %n");
        Appender appender = new ConsoleAppender(layout);
        BasicConfigurator.configure(appender);
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("setUpClass");
//        
        try {
            URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
            //PropertyConfigurator.configure("log4j.conf");
            initLog4J();
            
            Configuration config = new Configuration();
            BrokerPool.configure(1, 5, config);
            
            Class cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);

            brokerPool = BrokerPool.getInstance();

            rootCollection = DatabaseManager.getCollection("xmldb:exist://" + DBBroker.ROOT_COLLECTION, "admin", null);
            xpqservice = (XPathQueryService) rootCollection.getService("XQueryService", "1.0");
            cmservice = (CollectionManagementService) rootCollection.getService("CollectionManagementService", "1.0");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("tearDownClass");
        brokerPool.stop();
    }
}
