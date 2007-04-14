/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: EmbeddedURLsTest.java 191 2007-03-30 15:49:34Z dizzzz $
 */

package org.exist.protocolhandler.xmlrpc;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.xmldb.GetThread;
import org.exist.protocolhandler.xmldb.PutThread;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;

/**
 *
 * @author Dannes Wessels
 */
public class XmlrpcParallelTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcParallelTest.class);
    private static boolean firstTime=true;
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());
            PropertyConfigurator.configure("log4j.conf");
            firstTime=false;
        }
    }
    
    protected void tearDown() throws Exception {
        // empty
    }
    
    public XmlrpcParallelTest(String testName) {
        super(testName);
    }
    
    public void testRemoteParallelUpload(){
        File file=new File("samples/shakespeare/r_and_j.xml");
        try {
            
            for(int i=0; i<10 ; i++){
                URL url = new URL("xmldb:exist://localhost:8080/exist/xmlrpc/db/r_and_j-"+i+".xml");

                PutThread ct = new PutThread(file, url);
                Thread thread = new Thread(ct);
                
                // Start the thread
                thread.start();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    public void testRemoteParallelDownload(){
        
        try {
            
            for(int i=0; i<10 ; i++){
                URL url = new URL("xmldb:exist://localhost:8080/exist/xmlrpc/db/r_and_j-"+i+".xml");

                GetThread ct = new GetThread(url);
                Thread thread = new Thread(ct);
                
                // Start the thread
                thread.start();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
}
