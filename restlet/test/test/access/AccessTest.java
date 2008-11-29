/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.exist.restlet.XMLDB;
import org.exist.restlet.XMLDBResource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

/**
 *
 * @author alex
 */
public class AccessTest extends TestCase {
    
    public AccessTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    void copy(File in,File out)
       throws IOException
    {
       FileInputStream is = new FileInputStream(in);
       FileOutputStream os = new FileOutputStream(out);
       byte [] buffer = new byte[16384];
       int len;
       while ((len=is.read(buffer))>0) {
          os.write(buffer,0,len);
       }
       is.close();
       os.close();
    }

   public static boolean deltree(File dir)
   {
      final List<File> queue = new ArrayList<File>();
      queue.add(dir);
      boolean ok = true;
      int mark = -1;
      while (ok && queue.size()>0) {
         File target = queue.remove(queue.size()-1);
         if (target.isDirectory()) {
            if (mark==queue.size()) {
               ok = target.delete();
               mark = -1;
            } else {
               mark = queue.size();
               queue.add(target);
               target.listFiles(new FileFilter() {
                  public boolean accept(File f)
                  {
                     queue.add(f);
                     return false;
                  }
               });
            }
         } else {
            ok = target.delete();
         }
      }
      return ok;
   }

    public void testAccess() throws Exception {

       File dir = new File("test.out/access");
       if (dir.exists()) {
          deltree(dir);
       }
       if (!dir.mkdirs()) {
          throw new Exception("Cannot make directory "+dir.getAbsolutePath());
       }

       String [] dirs = { "data", "logs" };
       for (int i=0; i<dirs.length; i++) {
          File dataDir = new File(dir,dirs[i]);
          if (!dataDir.mkdir()) {
             throw new Exception("Cannot make directory "+dataDir.getAbsolutePath());
          }
       }
       String [] files = { "catalog.xml", "log4j.xml", "log4j.dtd", "conf.xml" };
       for (int i=0; i<files.length; i++) {
          copy(new File(files[i]),new File(dir,files[i]));
       }

       XMLDB xmldb = new XMLDB(XMLDB.DEFAULT_DB,new File(dir,"conf.xml"));
       xmldb.start();

       Logger log = Logger.getLogger(AccessTest.class.getName());
       Client client = new Client(new Context(),XMLDBResource.EXIST);
       client.getContext().setLogger(log);

       MediaType queryType = MediaType.valueOf("application/xquery");

       for (int i=0; i<1000; i++) {
          log.info("PUT "+i);
          String docRef = "exist://db/"+i+"/test.xml";
          String otherRef = "exist://db/"+i+"/other.xml";

          Response response = client.put(docRef, new StringRepresentation("<test id='"+i+"'><target/></test>",MediaType.APPLICATION_XML));
          if (response.isEntityAvailable()) {
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
          response = client.get(docRef);
          if (response.isEntityAvailable()) {
             log.info("GET "+i+" "+response.getEntity().getText());
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
          response = client.put(otherRef, new StringRepresentation("<other id='"+i+"'><target/></other>",MediaType.APPLICATION_XML));
          if (response.isEntityAvailable()) {
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
          response = client.get(otherRef);
          if (response.isEntityAvailable()) {
             log.info("GET "+i+" "+response.getEntity().getText());
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
          response = client.post(docRef,new StringRepresentation("update replace /test/target with <updated>"+System.currentTimeMillis()+"</updated>",queryType));
          if (response.isEntityAvailable()) {
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
          response = client.get(docRef);
          if (response.isEntityAvailable()) {
             log.info("GET "+i+" "+response.getEntity().getText());
             response.getEntity().release();
          }
          assertTrue(response.getStatus().isSuccess());
       }
       xmldb.stop();
    }

}
