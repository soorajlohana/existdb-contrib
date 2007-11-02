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
import org.exist.protocolhandler.shared.GetThread;
import org.exist.protocolhandler.shared.PutThread;
import org.exist.protocolhandler.eXistURLStreamHandlerFactory;

/**
 *
 * @author Dannes Wessels
 */
public class XmlrpcParallelTest extends TestCase {
    
    private static Logger LOG = Logger.getLogger(XmlrpcParallelTest.class);
    private static boolean firstTime=true;
    
    protected void setUp() throws Exception {
        if(firstTime){
            URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
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
        System.out.println(this.getName());
        File file=new File("samples/shakespeare/r_and_j.xml");
        try {
            PutThread[] pt = new PutThread[10];
            for(int i=0; i<pt.length ; i++){
                LOG.info("Initializing URL "+i);
                URL url = new URL("xmldb:exist://localhost:8080/exist/xmlrpc/db/r_and_j-"+i+".xml");
                
                pt[i] = new PutThread(file, url);
            }
            
            for(int i=0; i<pt.length ; i++){
                LOG.info("Starting thread "+i);
                pt[i].start();
            }
            
            for(int i=0; i<pt.length ; i++){
                LOG.info("Joining thread "+i);
                pt[i].join(25000);
            }
            
            for(int i=0; i<pt.length ; i++){
                LOG.info("Check thread "+i);
                Exception ex = pt[i].getException();
                if(ex!=null){
                    LOG.error("Thread "+i , ex);
                    ex.printStackTrace();
                    fail(ex.getMessage());
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
    public void testRemoteParallelDownload(){
        System.out.println(this.getName());
        try {
            
            GetThread[] gt = new GetThread[10];
            for(int i=0; i<gt.length ; i++){
                LOG.info("Initializing URL "+i);
                URL url = new URL("xmldb:exist://localhost:8080/exist/xmlrpc/db/r_and_j-"+i+".xml");
                gt[i] = new GetThread(url);
            }
            
            for(int i=0; i<gt.length ; i++){
                LOG.info("Starting thread "+i);
                gt[i].start();
            }
            
            for(int i=0; i<gt.length ; i++){
                LOG.info("Joining thread "+i);
                gt[i].join(25000);
            }
            
            for(int i=0; i<gt.length ; i++){
                LOG.info("Check thread "+i);
                Exception ex = gt[i].getException();
                if(ex!=null){
                    LOG.error("Thread "+i , ex);
                    ex.printStackTrace();
                    fail(ex.getMessage());
                }
                assertTrue(gt[i].getSize()>0);
                assertEquals(211579,gt[i].getSize());  // other number, indenting?
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
    
}
