/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.service.MetadataService;

/**
 *
 * @author alex
 */
public class Import {

   public static void main(String[] args) {
      if (args.length<4) {
         System.err.println(Import.class.getName()+" uri username password file1 file2 ...");
         System.exit(1);
      }

      Import importer = new Import(new Reference(args[0]),args[1],args[2]);
      for (int i=3; i<args.length; i++) {
         File file = new File(args[i]);
         importer.importFile(file);
      }
   }
   
   Reference base;
   Client client;
   ChallengeResponse identity;
   MetadataService metadataService;
   public Import(Reference base,String username,String password) {
      this.base = base;
      this.client = new Client(base.getSchemeProtocol());
      Context context = new Context(Logger.getLogger(Import.class.getName()));
      client.setContext(context);
      this.identity = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,username,password);
      this.metadataService = new MetadataService();
   }

   public void importFile(File file) {
      List<File> queue = new ArrayList<File>();
      queue.add(file);
      while (queue.size()>0) {
         File last = queue.remove(queue.size()-1);
         if (last.isDirectory()) {
            client.getLogger().warning("Ignoring directory: "+last.getAbsolutePath());
         } else {
            MediaType type = getMediaType(last);
            Reference resourceRef = new Reference(base.toString()+last.getName());
            client.getLogger().info("PUT "+resourceRef+" as "+type);
            Request request = new Request(Method.PUT,resourceRef);
            request.setChallengeResponse(identity);
            request.setEntity(new FileRepresentation(last,type));
            Response response = client.handle(request);
            if (response.getStatus().isError()) {
               if (response.isEntityAvailable()) {
                  try {
                     client.getLogger().severe("Status code: "+response.getStatus()+", "+response.getEntity().getText());
                  } catch (IOException ex) {
                     client.getLogger().severe("Status code: "+response.getStatus());
                  }
               } else {
                  client.getLogger().severe("Status code: "+response.getStatus());
               }
            }
         }
      }
   }

   public MediaType getMediaType(File file)
   {
      int pos = file.getName().lastIndexOf('.');
      if (pos<0) {
         return MediaType.APPLICATION_OCTET_STREAM;
      }
      String ext = file.getName().substring(pos+1);
      if (ext.equals("xml")) {
         return MediaType.APPLICATION_XML;
      } else {
         Metadata data = metadataService.getMetadata(ext);
         return (MediaType)data;
      }
   }

}
