/*
 * XMPPClientHelper.java
 *
 * Created on March 23, 2007, 1:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.helpers;

import java.net.URI;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.ClientHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.restlet.XMLDB;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.security.xacml.AccessContext;
import org.exist.source.DBSource;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.XQueryPool;
import org.exist.storage.lock.Lock;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionException;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.LockException;
import org.exist.util.serializer.SAXSerializer;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.Option;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xupdate.Modification;
import org.exist.xupdate.XUpdateProcessor;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alex
 */
public class EXistClientHelper  extends ClientHelper {

    protected final static String NS = "http://exist.sourceforge.net/NS/exist";
    
    protected final static String XUPDATE_NS = "http://www.xmldb.org/xupdate";
    protected final static Properties defaultProperties = new Properties();
    
    static {
        defaultProperties.setProperty(OutputKeys.INDENT, "no");
        defaultProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        defaultProperties.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "yes");
        defaultProperties.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, "elements");
        defaultProperties.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
    }
    
   /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
   public EXistClientHelper(Client client) {
      super(client);
      getProtocols().add(Protocol.valueOf("exist"));
   }
   
   static String join(String [] values, int start, int length,char delimiter) {
      StringBuilder buffer = new StringBuilder();
      int end = start+length;
      for (int i=start; i<end; i++) {
         if (i!=start) {
            buffer.append(delimiter);
         }
         buffer.append(values[i]);
      }
      return buffer.toString();
   }
    
   public void start() {
   }
   
   public void stop() {
   }

   public void handle(Request request, Response response)
   {
      if (request.getMethod().equals(Method.GET)) {
         get(request,response);
      } else if (request.getMethod().equals(Method.POST)) {
         post(request,response);
      } else if (request.getMethod().equals(Method.PUT)) {
         put(request,response);
      } else if (request.getMethod().equals(Method.HEAD)) {
         head(request,response);
      } else if (request.getMethod().equals(Method.DELETE)) {
         delete(request,response);
      } else {
         response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      }
   }
   
   public void head(Request request, Response response)
   {
      try {
         String name = request.getResourceRef().getHostDomain();
         if (name==null || name.length()==0) {
            name = XMLDB.DEFAULT_DB;
         }
         BrokerPool pool = BrokerPool.getInstance(name);
         if (pool==null) {
            getContext().getLogger().severe("eXist database "+name+" is not available.");
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return;
         }
         DBBroker broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         try {
            String path = request.getResourceRef().getPath();
            DocumentImpl resource = null;
            boolean isCollection = false;
            if (path.length()>0 && path.charAt(path.length()-1)=='/') {
               path = path.substring(0,path.length()-1);
               isCollection = true;
            }
            if (getContext().getLogger().isLoggable(Level.FINE)) {
               getContext().getLogger().info("Head on: "+path);
            }
            XmldbURI pathUri = XmldbURI.create(path);
            try {
               resource = isCollection ? null : broker.getXMLResource(pathUri, Lock.READ_LOCK);

               if (resource == null) {

                  // Must be a collection
                  Collection collection = broker.getCollection(pathUri);
                  String colPath = null;
                  if (collection!=null) {
                     colPath = collection.getURI().toString();
                     getContext().getLogger().info("collection path: "+colPath);
                     if (colPath.startsWith("/db")) {
                        colPath = colPath.substring(3);
                     }
                  }
                  if (collection != null && path.equals(colPath)) {
                     response.setStatus(Status.SUCCESS_OK);
                  } else {
                     response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                  }
               } else {
                  resource.getUpdateLock().release(Lock.READ_LOCK);
                  MediaType type = MediaType.valueOf(resource.getMetadata().getMimeType());
                  Representation rep = new StringRepresentation("",type);
                  rep.setModificationDate(new Date(resource.getMetadata().getLastModified()));
                  response.setEntity(rep);
                  response.setStatus(Status.SUCCESS_OK);
               }
            } catch(PermissionDeniedException ex) {
               response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
         } finally {
            pool.release(broker);
         }
      } catch (EXistException ex) {
         getContext().getLogger().log(Level.SEVERE,"XMLDB request failed: "+ex.getMessage(),ex);
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
         response.setEntity(new StringRepresentation("XMLDB request failed: "+ex.getMessage()));
      }      
   }
    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
   public void get(Request request, Response response)
   {
      BrokerPool pool = null;
      try {
         String name = request.getResourceRef().getHostDomain();
         if (name==null || name.length()==0) {
            name = XMLDB.DEFAULT_DB;
         }
         pool = BrokerPool.getInstance(name);
         if (pool==null) {
            getContext().getLogger().severe("eXist database "+name+" is not available.");
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return;
         }
         DBBroker broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         boolean release = true;
         try {
            String path = request.getResourceRef().getPath();
            if (getContext().getLogger().isLoggable(Level.FINE)) {
               getContext().getLogger().fine("Get on: "+path);
            }
            DocumentImpl resource = null;
            XmldbURI pathUri = XmldbURI.create(path);
            try {
               resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);

               if (resource == null) {

                  // Must be a collection
                  Collection collection = broker.getCollection(pathUri);
                  if (collection != null) {
                     response.setEntity(new StringRepresentation("Browsing collections not implemented."));
                     response.setStatus(Status.SUCCESS_OK);
                  } else {
                     response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                  }
               } else {
                  final DBBroker currentBroker = broker;
                  final DocumentImpl currentResource = resource;
                  if (resource.getResourceType() == DocumentImpl.BINARY_FILE) {
                     release = false;
                     final BrokerPool currentPool = pool;
                     response.setEntity(new OutputRepresentation(MediaType.valueOf(resource.getMetadata().getMimeType())) {
                        public void write(OutputStream os)
                           throws IOException
                        {
                           try {
                              currentBroker.readBinaryResource((BinaryDocument)currentResource,os);
                           } finally {
                              currentResource.getUpdateLock().release(Lock.READ_LOCK);
                              currentPool.release(currentBroker);
                           }
                        }
                     });
                     response.setStatus(Status.SUCCESS_OK);
                  } else {
                     release = false;
                     final BrokerPool currentPool = pool;
                     Representation rep = new OutputRepresentation(MediaType.valueOf(resource.getMetadata().getMimeType())) {
                        public void write(OutputStream os)
                           throws IOException
                        {
                           try {
                              Serializer serializer = currentBroker.getSerializer();
                              serializer.reset();

                              try {
                                 //Serialize the document
                                 Writer w = new OutputStreamWriter(os,"UTF-8");
                                 serializer.serialize(currentResource,w);
                                 w.flush();
                                 //w.close();
                              } catch (SAXException ex) {
                                 throw new IOException(ex.getMessage());
                              }
                           } finally {
                              currentResource.getUpdateLock().release(Lock.READ_LOCK);
                              currentPool.release(currentBroker);
                           }
                        }
                     };
                     rep.setCharacterSet(CharacterSet.UTF_8);
                     response.setEntity(rep);
                     response.setStatus(Status.SUCCESS_OK);
                  }
               }
            } catch(PermissionDeniedException ex) {
               response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
         } finally {
            if (release) {
               pool.release(broker);
            }
         }
      } catch (EXistException ex) {
         getContext().getLogger().log(Level.SEVERE,"XMLDB request failed: "+ex.getMessage(),ex);
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
         response.setEntity(new StringRepresentation("XMLDB request failed: "+ex.getMessage()));
      }
   }

  public void post(Request request, Response response)
   {
      String path = request.getResourceRef().getPath();
      Properties outputProperties = new Properties(defaultProperties);
      XmldbURI pathUri = XmldbURI.create(path);
      DocumentImpl resource = null;
      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         String name = request.getResourceRef().getHostDomain();
         if (name==null || name.length()==0) {
            name = XMLDB.DEFAULT_DB;
         }
         try {
            pool = BrokerPool.getInstance(name);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+name+" is not available.");
               response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return;
            }
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+name+" is not available.",ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         try {
            broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         try {
            // check if path leads to an XQuery resource.
            // if yes, the resource is loaded and the XQuery executed.
            resource = (DocumentImpl) broker.getXMLResource(pathUri, Lock.READ_LOCK);
            if (resource != null && resource.getResourceType() == DocumentImpl.BINARY_FILE &&
                "application/xquery".equals(resource.getMetadata().getMimeType())) {
               // found an XQuery resource
               try {
                  executeXQuery(broker, new DBSource(broker, (BinaryDocument)resource, true), new XmldbURI[] { resource.getCollection().getURI() },-1,0,request, response, outputProperties);
               } catch (XPathException ex) {
                  response.setStatus(Status.SERVER_ERROR_INTERNAL,"Exception while processing query: "+ex.getMessage());
               }
               return;
            }
         } catch (PermissionDeniedException ex) {
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,"Unauthorized access: "+ex.getMessage());
         } finally {
            if (resource != null) {
               resource.getUpdateLock().release(Lock.READ_LOCK);
            }
         }

         // third, normal POST: read the request content and check if
         // it is an XUpdate or a query request.        
         int howmany = 10;
         int start = 1;
         boolean enclose = true;
         String mime = "text/xml";
         String query = null;
         String incomingMediaType = request.getEntity().getMediaType().getName();
         if (incomingMediaType.equals("application/xquery")) {
            try {
               query = request.getEntity().getText();
            } catch (IOException ex) {
               response.setStatus(Status.SERVER_ERROR_INTERNAL,"I/O error processing request: "+ex.getMessage());
               return;
            }
         } else if (incomingMediaType.equals("text/xml") ||
                    incomingMediaType.equals("application/xml") ||
                    incomingMediaType.endsWith("+xml")) {
            mime = incomingMediaType;
            String content = null;

            try {
               content = request.getEntity().getText();
            } catch (IOException ex) {
               response.setStatus(Status.SERVER_ERROR_INTERNAL,"I/O error processing request: "+ex.getMessage());
               return;
            }

            InputSource src = new InputSource(new StringReader(content));
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;
            try {
               docBuilder = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
               response.setStatus(Status.SERVER_ERROR_INTERNAL,ex.getMessage());
               return;
            }

            Document doc = null;

            try {
               doc = docBuilder.parse(src);
            } catch (IOException ex) {
               response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"I/O error while processing document: "+ex.getMessage());
               return;
            } catch (SAXException ex) {
               response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Cannot parse document: "+ex.getMessage());
               return;
            }
            Element root = doc.getDocumentElement();
            String rootNS = root.getNamespaceURI();
            if (rootNS != null && rootNS.equals(NS)) {
               if (root.getLocalName().equals("query")) {
                       // process <query>xpathQuery</query>
                  String option = root.getAttribute("start");
                  if (option != null) {
                     try {
                        start = Integer.parseInt(option);
                     } catch (NumberFormatException e) {
                     }
                  }
                  option = root.getAttribute("max");
                  if (option != null) {
                     try {
                        howmany = Integer.parseInt(option);
                     } catch (NumberFormatException e) {
                     }
                  }

                  option = root.getAttribute("enclose");
                  if (option != null) {
                     if (option.equals("no")) {
                        enclose = false;
                     }
                  }

                  option = root.getAttribute("mime");
                  mime = "text/xml";
                  if ((option != null) && (!option.equals(""))) {
                     mime = option;
                  }

                  NodeList children = root.getChildNodes();
                  for (int i = 0; i < children.getLength(); i++) {
                     Node child = children.item(i);
                     if (child.getNodeType() == Node.ELEMENT_NODE && child.getNamespaceURI().equals(NS)) {
                        if (child.getLocalName().equals("text")) {
                           StringBuffer buf = new StringBuffer();
                           Node next = child.getFirstChild();
                           while (next != null) {
                              if (next.getNodeType() == Node.TEXT_NODE || next.getNodeType() == Node.CDATA_SECTION_NODE) {
                                 buf.append(next.getNodeValue());
                              }
                              next = next.getNextSibling();
                           }
                           query = buf.toString();
                        } else if (child.getLocalName().equals("properties")) {
                           Node node = child.getFirstChild();
                           while (node != null) {
                              if (node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(NS)  &&
                                  node.getLocalName().equals("property")) {
                                 Element property = (Element) node;
                                 String key = property.getAttribute("name");
                                  String value = property.getAttribute("value");
                                  if (key != null && value != null) {
                                      outputProperties.setProperty(key,value);
                                  }
                              }
                              node = node.getNextSibling();
                           }
                       }
                   }
                }
             }
           } else if (rootNS != null && rootNS.equals(XUPDATE_NS)) {
                 DocumentSet docs = new DocumentSet();
                 Collection collection = broker.getCollection(pathUri);
                 if (collection != null) {
                    collection.allDocs(broker, docs, true, true);
                 } else {
                    try {
                       DocumentImpl xupdateDoc = (DocumentImpl) broker.getXMLResource(pathUri);
                       if (xupdateDoc != null) {
                           if (!xupdateDoc.getPermissions().validate(
                                   broker.getUser(), Permission.READ)) {
                              response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,"Not allowed to read collection.");
                              return;
                           }
                           docs.add(xupdateDoc);
                       } else {
                          broker.getAllXMLResources(docs);
                       }
                    } catch (PermissionDeniedException ex) {
                       response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,"Permission denied to document "+pathUri+": "+ex.getMessage());
                       return;
                    }
                 }

                 TransactionManager transact = broker.getBrokerPool().getTransactionManager();
                 Txn transaction = transact.beginTransaction();
                 try {
                    XUpdateProcessor processor = new XUpdateProcessor(broker, docs, AccessContext.REST);
                    Modification modifications[] = processor.parse(new InputSource(
                            new StringReader(content)));
                    long mods = 0;
                    for (int i = 0; i < modifications.length; i++) {
                       mods += modifications[i].process(transaction);
                       broker.flush();
                    }
                    transact.commit(transaction);

                    response.setStatus(Status.SUCCESS_OK);
                    response.setEntity(new StringRepresentation(
                           "<?xml version='1.0'?>\n"
                           + "<exist:modifications xmlns:exist='" + NS
                           + "' count='" + mods + "'>" + mods
                           + "modifications processed.</exist:modifications>",MediaType.APPLICATION_XML));
                    return;
                 } catch (XPathException ex) {
                    transact.abort(transaction);
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"XQuery error while processing update: "+ex.getMessage());
                    return;
                 } catch (EXistException ex) {
                    transact.abort(transaction);
                    response.setStatus(Status.SERVER_ERROR_INTERNAL,"Error processing update: "+ex.getMessage());
                    return;
                 } catch (LockException ex) {
                    transact.abort(transaction);
                    response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,"Resource cannot be locked: "+ex.getMessage());
                    return;
                 } catch (SAXException ex) {
                    transact.abort(transaction);
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"XML error parsing update: "+ex.getMessage());
                    return;
                 } catch (IOException ex) {
                    getContext().getLogger().log(Level.SEVERE,"I/O error during update.",ex);
                    transact.abort(transaction);
                    response.setStatus(Status.SERVER_ERROR_INTERNAL,"I/O error during update: "+ex.getMessage());
                    return;
                 } catch (ParserConfigurationException ex) {
                    getContext().getLogger().log(Level.SEVERE,"Parser configuration error.",ex);
                    transact.abort(transaction);
                    response.setStatus(Status.SERVER_ERROR_INTERNAL,ex.getMessage());
                    return;
                 } catch (PermissionDeniedException ex) {
                    transact.abort(transaction);
                    response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,"Permission denied for update to "+pathUri+": "+ex.getMessage());
                    return;
                 }
            } else {
               // just compile the doc as a query and hope for the best!
               query = content;
            }
         }

         if (query==null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"No query was specified.");
            return;
         }

         if (resource==null) {
            // must be a collection
            Collection collection = broker.getCollection(pathUri);
            if (collection==null) {
               response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
               return;
            }
         }
         TransactionManager transact = broker.getBrokerPool().getTransactionManager();
         Txn transaction = transact.beginTransaction();
         try {
             // execute query
             try {
                executeXQuery(broker, new StringSource(query), new XmldbURI[] { pathUri },howmany,start,request, response, outputProperties);
             } catch (XPathException ex) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL,"Exception while processing query: "+ex.getMessage());
             }
             transact.commit(transaction);
         } catch (EXistException e) {
            transact.abort(transaction);
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
         }
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }
   }
   
   public void put(Request request, Response response)
   {
      Representation rep = request.getEntity();
      if (rep==null) {
         response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Entity body is missing.");
         return;
      }
      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         String name = request.getResourceRef().getHostDomain();
         if (name==null || name.length()==0) {
            name = XMLDB.DEFAULT_DB;
         }
         try {
            pool = BrokerPool.getInstance(name);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+name+" is not available.");
               response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return;
            }
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+name+" is not available.",ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         try {
            broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         String path = request.getResourceRef().getPath();
         XmldbURI pathUri = XmldbURI.create(path);
         
         // Test to see if the path is a collection (put should fail)
         Collection collection = broker.getCollection(pathUri);
         if (collection!=null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"A put is not allowed to a collection path.  A file segment must be specified.");
            return;
         }
         
         // Find the collection
         XmldbURI docUri = pathUri.lastSegment();
         XmldbURI collectionUri = pathUri.removeLastSegment();
         
         TransactionManager transact = pool.getTransactionManager();
         Txn transaction = transact.beginTransaction();
         
         // Find the collection
         collection = broker.getCollection(collectionUri);
         
         // Create it if it doesn't exist
         if (collection==null) {
            try {
               collection = broker.getOrCreateCollection(transaction,collectionUri);
               broker.saveCollection(transaction, collection);
            } catch (PermissionDeniedException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return;
            } catch (Exception ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Failed to create collection "+collectionUri+" in eXist: "+ex.getMessage(),ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
               return;
            }
         }
         
         // Check for XML media type
         MediaType type = rep.getMediaType();
         /*
         getContext().getLogger().info(type.toString());
         getContext().getLogger().info(type.getName());
         for (String name : type.getParameters().getNames()) {
            String value = type.getParameters().getValues(name);
            getContext().getLogger().info(name+"="+value);
         }*/
         if (type.getName().equals(MediaType.APPLICATION_XML.getName()) ||
             type.getName().equals(MediaType.TEXT_XML.getName()) ||
             type.getName().endsWith("+xml")) {
            
            // an XML document
            
            // Create temporary storage
            File tempFile = null;
            try {
               tempFile = File.createTempFile("exist","xml");
            } catch (IOException ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Cannot create temporary file: "+ex.getMessage(),ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
               return;
            }
            String charset = type.getParameters().getValues("charset");
            if (charset==null) {
               charset = "UTF-8";
            }
            //getContext().getLogger().info("charset="+charset);
            try {
               InputStream is = rep.getStream();

               // TODO: optimize to remove first read of put
               FileOutputStream os = new FileOutputStream(tempFile);
               byte [] buffer = new byte[16384];
               int len;
               while ((len=is.read(buffer))>0) {
                  os.write(buffer,0,len);
               }
               os.close();
               is.close();
            } catch (IOException ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Cannot buffer XML into temp file due to I/O error: "+ex.getMessage(),ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
               return;
            }
            
            // Check to see if it is a binary and remove it.
            try {
               DocumentImpl resource = (DocumentImpl) broker.getXMLResource(docUri);
               if (resource!=null && resource.getResourceType() == DocumentImpl.BINARY_FILE) {
                  resource.getCollection().removeBinaryResource(transaction, broker,docUri.lastSegment());

               }           
            } catch (PermissionDeniedException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return;
            } catch (LockException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return;
            } catch (TriggerException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return;
            }
            try {
               InputStreamReader r = new InputStreamReader(new FileInputStream(tempFile),charset);
               IndexInfo info = collection.validateXMLResource(transaction, broker, docUri, new InputSource(r));
               r.close();

               info.getDocument().getMetadata().setMimeType(type.getName());

               r = new InputStreamReader(tempFile.toURL().openStream(),charset);
               collection.store(transaction, broker, info, new InputSource(r), false);
               r.close();

               //getContext().getLogger().info(tempFile.getAbsolutePath());
               tempFile.delete();
               
               transact.commit(transaction);
               
            } catch (IOException ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Cannot read temp file for XML storage: "+ex.getMessage(),ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
               return;
            } catch (PermissionDeniedException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return;
            } catch (SAXException ex) {
               transact.abort(transaction);
               response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,ex.getMessage());
               return;
            } catch (Exception ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Failed to store xml into eXist: "+ex.getMessage(),ex);
               response.setStatus(Status.SERVER_ERROR_INTERNAL);
               return;
            }
            
         } else {
            long size = rep.getSize();
            if (size<0) {
               File tempFile = null;
               try {
                  tempFile = File.createTempFile("exist","bin");
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot create temporary file: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               }
               try {
                  InputStream is = rep.getStream();

                  // TODO: optimize to remove first read of put
                  FileOutputStream os = new FileOutputStream(tempFile);
                  byte [] buffer = new byte[16384];
                  int len;
                  while ((len=is.read(buffer))>0) {
                     os.write(buffer,0,len);
                  }
                  os.close();
                  is.close();
               } catch (IOException ex) {
                  getContext().getLogger().log(Level.SEVERE,"Cannot buffer binary into temp file due to I/O error: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               }
               try {
                  FileInputStream is = new FileInputStream(tempFile);
                  collection.addBinaryResource(transaction, broker, docUri, is, type.toString(), (int) tempFile.length());
                  is.close();
                  transact.commit(transaction);
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot read temp file for binary storage: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               } catch (PermissionDeniedException ex) {
                  transact.abort(transaction);
                  response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                  return;
               } catch (Exception ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Failed to store binary into eXist: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               }
            } else {
               if (size>Integer.MAX_VALUE) {
                  response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE,"Entity size "+size+" is too large.");
                  return;
               }
               try {
                  collection.addBinaryResource(transaction, broker, docUri, rep.getStream(), type.toString(), (int)size);
                  transact.commit(transaction);
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot read temp file for binary storage: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               } catch (PermissionDeniedException ex) {
                  transact.abort(transaction);
                  response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                  return;
               } catch (Exception ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Failed to store binary into eXist: "+ex.getMessage(),ex);
                  response.setStatus(Status.SERVER_ERROR_INTERNAL);
                  return;
               }
            }
         }
         response.setStatus(Status.SUCCESS_NO_CONTENT);
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }
   }
   
   public void delete(Request request, Response response)
   {
      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         String name = request.getResourceRef().getHostDomain();
         if (name==null || name.length()==0) {
            name = XMLDB.DEFAULT_DB;
         }
         try {
            pool = BrokerPool.getInstance(name);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+name+" is not available.");
               response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return;
            }
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+name+" is not available.",ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         try {
            broker = pool.get(pool.getSecurityManager().SYSTEM_USER);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
         }
         String path = request.getResourceRef().getPath();
         XmldbURI pathUri = XmldbURI.create(path);
         TransactionManager transact = broker.getBrokerPool().getTransactionManager();
         Txn txn = transact.beginTransaction();
         try {
            Collection collection = broker.getCollection(pathUri);
            if (collection != null) {
                // remove the collection
              broker.removeCollection(txn, collection);
              response.setStatus(Status.SUCCESS_NO_CONTENT);
            } else {
               DocumentImpl doc = (DocumentImpl) broker.getXMLResource(pathUri);
               if (doc == null) {
                  transact.abort(txn);
                  response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
               } else {
                  if (doc.getResourceType() == DocumentImpl.BINARY_FILE) {
                     doc.getCollection().removeBinaryResource(txn, broker,pathUri.lastSegment());
                  } else {
                     doc.getCollection().removeXMLResource(txn, broker, pathUri.lastSegment());
                  }
                  response.setStatus(Status.SUCCESS_NO_CONTENT);
               }
            }
            transact.commit(txn);
         } catch (PermissionDeniedException ex) {
            transact.abort(txn);
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
         } catch (TriggerException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Trigger failed: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
         } catch (IOException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"I/O error: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
         } catch (LockException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Unable to get lock: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
         } catch (TransactionException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Transaction failed: "+ex.getMessage(),ex);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
         }
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }
   }
   
    private void executeXQuery(DBBroker broker,Source source,XmldbURI [] knownDocuments,int howmany,int start,Request request, Response response,Properties outputProperties) 
       throws XPathException 
    {
        XQuery xquery = broker.getXQueryService();
        XQueryPool pool = xquery.getXQueryPool();
        XQueryContext context;
        CompiledXQuery compiled = pool.borrowCompiledXQuery(broker, source);
        if (compiled == null) {
           context = xquery.newContext(AccessContext.REST);
    	} else {
           context = compiled.getContext();
        }
        
        //TODO: don't hardcode this?
        //context.setModuleLoadPath(XmldbURI.EMBEDDED_SERVER_URI.append(resource.getCollection().getURI()).toString());
        context.setStaticallyKnownDocuments(knownDocuments);
        
        if (compiled == null) {
           try {
              compiled = xquery.compile(context, source);
           } catch (IOException ex) {
              getContext().getLogger().log(Level.SEVERE,"Failed to compile xquery: "+ex.getMessage(),ex);
              response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,"Failed to compile xquery: "+ex.getMessage());
              return;
            }
        }
        checkPragmas(context, outputProperties);
        try {
            Sequence result = xquery.execute(compiled, null);
            makeResultRepresentation(broker, result, howmany, start, 0, outputProperties, false,response);
        } finally {
            pool.returnCompiledXQuery(source, compiled);
        }
    }
       
    protected void checkPragmas(XQueryContext context, Properties properties)
       throws XPathException 
    {
        Option pragma = context.getOption(Option.SERIALIZE_QNAME);
        if (pragma == null)
            return;
        String[] contents = pragma.tokenizeContents();
        for (int i = 0; i < contents.length; i++) {
            String[] pair = Option.parseKeyValuePair(contents[i]);
            if (pair == null) {
                throw new XPathException("Unknown parameter found in "
                        + pragma.getQName().getStringValue() + ": '" + contents[i]
                        + "'");
            }
            properties.setProperty(pair[0], pair[1]);
        }
    }
   protected void makeResultRepresentation(DBBroker broker, final Sequence results,
            int howmany, final int start, long queryTime,
            final Properties outputProperties, final boolean wrap,Response response) {        
      if (!results.isEmpty()) {
         int rlen = results.getItemCount();
         if ((start < 1) || (start > rlen)) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Start parameter "+start+" is out of range.");
            return;
         }
         // FD : correct bound evaluation
         if (((howmany + start) > rlen) || (howmany <= 0)) {
             howmany = rlen - start + 1;
         }
      } else {
         howmany = 0;
      }
      outputProperties.setProperty(Serializer.GENERATE_DOC_EVENTS, "false");
      final Serializer serializer = broker.getSerializer();
      serializer.reset();
      final int currentHowmany = howmany;
      OutputRepresentation rep = new OutputRepresentation(MediaType.APPLICATION_XML) {
         public void write(OutputStream os)
            throws IOException
         {
            try {

               //Serialize the document
               Writer w = new OutputStreamWriter(os,"UTF-8");

               SAXSerializer sax = new SAXSerializer();
               sax.setOutput(w,outputProperties);
               serializer.setContentHandler(sax);

               serializer.toSAX(results, start, currentHowmany, wrap);

               w.flush();
               //w.close();
            } catch (SAXException ex) {
               throw new IOException("Cannot serialize query result due to: "+ex.getMessage());
            }
         }
      };
      response.setEntity(rep);
    }
}
