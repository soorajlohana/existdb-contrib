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

package org.exist.protocolhandler.embedded;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


import org.apache.log4j.Logger;
import org.exist.protocolhandler.GenericTestcase;
import org.exist.protocolhandler.shared.GetThread;
import org.exist.protocolhandler.shared.PutThread;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wessels
 */
public class EmbeddedParallelTest extends GenericTestcase {
    
    
    private static Logger LOG = Logger.getLogger(EmbeddedParallelTest.class);
        
    private void sendToURL(String URL, String file) throws Exception {
        
        URL url = new URL(URL);
        InputStream is = new BufferedInputStream( new FileInputStream(file) );
        OutputStream os = url.openConnection().getOutputStream();
        copyDocument(is,os);
        is.close();
        os.close();
        
    }
    
    private void getFromURL(String URL, OutputStream os) throws Exception {
        
        URL url = new URL(URL);
        InputStream is = url.openConnection().getInputStream();
        copyDocument(is,os);
        
        is.close();
        os.close();
        
    }
    
    // Transfer bytes from inputstream to outputstream
    private void copyDocument(InputStream is, OutputStream os) throws IOException{
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }
    
    // ======================================================================
    
    @Test
    public void embeddedParallelUpload(){
        System.out.println("testEmbeddedParallelUpload");
        File file=new File("samples/shakespeare/r_and_j.xml");
        try {
            PutThread[] pt = new PutThread[10];

            for(int i=0; i<pt.length ; i++){
                LOG.info("Initializing URL "+i);
                URL url = new URL("xmldb:exist:///db/r_and_j-"+i+".xml");
                
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
    
    @Test
    public void embeddedParallelDownload(){
        System.out.println("testEmbeddedParallelDownload");
        try {
            GetThread[] gt = new GetThread[10];

            for(int i=0; i<gt.length ; i++){
                LOG.info("Initializing URL "+i);
                URL url = new URL("xmldb:exist:///db/r_and_j-"+i+".xml");
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
                assertEquals(304526,gt[i].getSize());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
}
