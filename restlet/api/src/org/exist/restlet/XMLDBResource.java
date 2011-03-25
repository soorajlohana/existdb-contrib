/*
 * AtomResource.java
 *
 * Created on March 27, 2007, 11:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentImpl;
import org.exist.dom.MutableDocumentSet;
import org.exist.dom.QName;
import org.exist.restlet.auth.UserManager;
import org.exist.security.AuthenticationException;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.security.Subject;
import org.exist.security.xacml.AccessContext;
import org.exist.source.DBSource;
import org.exist.source.FileSource;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.source.URLSource;
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
import org.exist.xquery.AbstractExpression;
import org.exist.xquery.AnalyzeContextInfo;
import org.exist.xquery.Cardinality;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.Expression;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Option;
import org.exist.xquery.UserDefinedFunction;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xupdate.Modification;
import org.exist.xupdate.XUpdateProcessor;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * However we get here we expect that the remaining path after 'base-uri'
 * is formulated as:
 *
 * {base-uri}/{db-name}/{resource-path}
 * 
 * @author alex
 */
public class XMLDBResource extends ServerResource {

   public final static String USER_NAME               = "org.exist.xmldb.user";
   public final static String NEW_USER_NAME           = "org.exist.xmldb.user.new";
   public final static String SESSION_NAME            = "org.exist.xmldb.user.session";
   public final static String SESSION_MANAGER_NAME    = "org.exist.xmldb.user.session.manager";
   public final static String USER_MANAGER_NAME       = "org.exist.xmldb.user.manager";
   public final static String USER_MANAGER_KEY_NAME   = "org.exist.xmldb.user.manager.key";
   public final static String USER_MANAGER_CLASS_NAME = "org.exist.xmldb.user.manager.class";
   public final static String USER_LIST_NAME          = "org.exist.xmldb.user.list";
   public final static String USER_HREF_NAME          = "org.exist.xmldb.user.href";
   public final static String USER_DATABASES_CHECK    = "org.exist.xmldb.user.databases.check";
   public final static String REALM_NAME              = "org.exist.xmldb.user.realm";
   public final static String COOKIE_NAME             = "org.exist.xmldb.user.cookie";
   public final static String COOKIE_PATH_NAME        = "org.exist.xmldb.user.cookie.path";
   public final static String COOKIE_EXPIRY_NAME      = "org.exist.xmldb.user.cookie.expiry";
   public final static String XQUERY_NAME             = "org.exist.xmldb.xquery";
   public final static String DBNAME_NAME             = "org.exist.xmldb.db.name";
   public final static String DBPATH_NAME             = "org.exist.xmldb.db.path";
   public final static String DB_USER_NAME            = "org.exist.xmldb.db.user";
   public final static String DB_PASSWORD_NAME        = "org.exist.xmldb.db.password";
   public final static String DB_SECURITY_MANAGER     = "org.exist.xmldb.db.security-manager";
   
   protected final static String PARAMETER_NAME = "org.exist.xmldb.request.parameters";
   protected final static String DEFAULT_DB = "db";
   protected final static Properties defaultProperties = new Properties();
   protected final static String FUNC_NS = "http://code.google.com/p/existdb-contrib";
   protected final static String NS = "http://exist.sourceforge.net/NS/exist";
   protected final static String XUPDATE_NS = "http://www.xmldb.org/xupdate";

   protected final static QName PARAMETER_NAMES_QNAME = new QName("parameter-names",FUNC_NS);
   protected final static QName GET_PARAMETER_QNAME = new QName("get-parameter",FUNC_NS);

   static {
      defaultProperties.setProperty(OutputKeys.INDENT, "no");
      defaultProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
      defaultProperties.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "yes");
      defaultProperties.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, "elements");
      defaultProperties.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
   }

   static class ParameterNamesExpression extends AbstractExpression {
      ParameterNamesExpression(XQueryContext context) {
         super(context);
      }

      public int returnsType() {
         return Type.ITEM;
      }
      public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {}

      public Sequence eval(Sequence contextSequence,Item contextItem)
         throws XPathException
      {
         Form [] parameterSets = (Form [])context.getAttribute(PARAMETER_NAME);
			ValueSequence result = new ValueSequence();
         Set<String> nameSet = new HashSet<String>();
         for (int i=0; i<parameterSets.length; i++) {
            for (String name : parameterSets[i].getNames()) {
               nameSet.add(name);
            }
         }
         for (String name : nameSet) {
            result.add(new StringValue(name));
         }
         return result;
      }
   	public void dump(ExpressionDumper dumper) {
         dumper.display("{"+FUNC_NS+"}parameter-names()");
      }
   }

   static class GetParameterExpression extends AbstractExpression {
      static QName NAME = new QName("name",null);
      GetParameterExpression(XQueryContext context) {
         super(context);
      }

      public int returnsType() {
         return Type.ITEM;
      }
      public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {}

      public Sequence eval(Sequence contextSequence,Item contextItem)
         throws XPathException
      {
         Form [] parameterSets = (Form [])context.getAttribute(PARAMETER_NAME);
         String parameterName = context.getVariables().get(NAME).getValue().getStringValue();
         String [] values = null;
         for (int i=0; i<parameterSets.length; i++) {
            String [] temp = parameterSets[i].getValuesArray(parameterName);
            if (temp!=null && temp.length>0) {
               values = temp;
            }
         }
			ValueSequence result = new ValueSequence();
         if (values!=null) {
            for (int i=0; i<values.length; i++) {
               result.add(new StringValue(values[i]));
            }
         }
         return result;
      }
   	public void dump(ExpressionDumper dumper) {
         dumper.display("{"+FUNC_NS+"}get-parameter()");
      }
   }
   String dbName;
   String dbPath;
   boolean isCollection;
   boolean checkUserDatabases;
   UserManager userManager;
   String realmName;

   /** Creates a new instance of AtomResource */
   public XMLDBResource() {
      dbName = null;
      dbPath = null;
      userManager = null;
      checkUserDatabases = false;
      realmName = null;
      setNegotiated(false);
   }

   protected void doInit() {
      Object name = getContext().getAttributes().get(DBNAME_NAME);
      if (name==null) {
         name = getRequest().getAttributes().get(DBNAME_NAME);
      }
      if (name==null) {
         name = getContext().getParameters().getFirstValue(DBNAME_NAME);
      }
      Object pathSpec = getContext().getAttributes().get(DBPATH_NAME);
      if (pathSpec==null) {
         pathSpec = getRequest().getAttributes().get(DBPATH_NAME);
      }
      if (pathSpec==null) {
         pathSpec = getContext().getParameters().getFirstValue(DBNAME_NAME);
      }
      this.userManager = (UserManager)getRequest().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
      if (this.userManager==null) {
         this.userManager = (UserManager)getContext().getAttributes().get(XMLDBResource.USER_MANAGER_NAME);
      }
      if (this.userManager==null) {
         getLogger().finer("The UserManager instance is missing.");
      }
      this.realmName = getContext().getParameters().getFirstValue(XMLDBResource.REALM_NAME);
      checkUserDatabases = "true".equals(getContext().getParameters().getFirstValue(XMLDBResource.USER_DATABASES_CHECK));
      if (name!=null) {
         dbName = name.toString();
         dbPath = pathSpec==null ? null : pathSpec.toString();
         dbPath = getRequest().getResourceRef().getRemainingPart();
      } else {
         String [] dbRef = getDBRef(getRequest().getResourceRef().getRemainingPart());
         dbName = dbRef[0];
         dbPath = dbRef[1];
      }
      isCollection = false;
      if (dbPath.length()>0 && dbPath.charAt(dbPath.length()-1)=='/') {
         dbPath = dbPath.substring(0,dbPath.length()-1);
         isCollection = true;
      }
      if (getContext().getLogger().isLoggable(Level.FINE)) {
         getContext().getLogger().fine("XMLDBResource: db="+dbName+", path="+dbPath+", realm="+realmName);
      }

   }

   protected Representation doHandle() {
      if (checkUserDatabases) {
         Subject user = (Subject)getRequest().getAttributes().get(USER_NAME);
         if (user==null) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return null;
         }
         if (userManager==null) {
            getLogger().severe("No user manager with which to check user database grants.");
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return null;
         }
         if (!userManager.isUserAllowedDatabaseAccess(dbName, user.getName())) {
            getLogger().warning("User "+user.getName()+" is not allowed to access "+dbName);
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return null;
         }
      }
      try {
         return super.doHandle();
      } catch (SecurityException ex) {
         getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
         getLogger().log(Level.SEVERE,"Security exception in database access.",ex);
         return new StringRepresentation("Security exception in database access.");
      }
   }

   protected Object getXQueryReference() {
      Object xqueryRefAttr = getRequest().getAttributes().get(XMLDBResource.XQUERY_NAME);
      if (xqueryRefAttr==null) {
         xqueryRefAttr = getContext().getParameters().getFirstValue(XMLDBResource.XQUERY_NAME);
      }
      return xqueryRefAttr;
   }

   protected Form getAttributesAsForm() {
      Form form = new Form();
      Map<String,Object> attrs = getRequest().getAttributes();
      for (String key : attrs.keySet()) {
         Object value = attrs.get(key);
         if (value instanceof String) {
            form.add(key, value.toString());
         }
      }
      return form;
   }
   
   protected Subject getUser(BrokerPool pool) {
      if (pool==null) {
         throw new IllegalArgumentException("The BrokerPool instance is null.");
      }
      if (pool.getSecurityManager()==null) {
         throw new IllegalStateException("The BrokerPool instance "+pool.getId()+" does not have a SecurityManager instance.");
      }
      Subject user = (Subject)getRequest().getAttributes().get(USER_NAME);
      if (realmName!=null) {
         if (user==null) {
            throw new SecurityException("User is not specified for realm "+realmName);
         }
         if (user.getRealm()==null) {
            throw new SecurityException("The user "+user.getName()+" does not have a realm.");
         }
         getLogger().info("user realm="+user.getRealm()+", realm="+realmName);
         if (!user.getRealmId().equals(realmName)) {
            throw new SecurityException("The realm "+user.getRealmId()+" for user "+user.getName()+" does not match realm "+realmName);
         }
      }
      if (user==null) {
         String dbUserName = getContext().getParameters().getFirstValue(DB_USER_NAME);
         String dbUserPassword = getContext().getParameters().getFirstValue(DB_PASSWORD_NAME);
         if (dbUserName!=null) {
            if (userManager!=null) {
               user = userManager.authenticate(dbUserName, dbUserPassword);
            } else {
               try {
                  user = pool.getSecurityManager().authenticate(dbUserName, dbUserPassword);
               } catch (AuthenticationException ex) {}
            }
            if (user==null) {
               throw new SecurityException("Unable to get user "+dbUserName+" for operation.");
            }
         }
      }
      return user==null ? pool.getSecurityManager().getGuestSubject() : user;
   }

   protected String [] getDBRef(String path) {

      if (path.length()==0) {
         return null;
      }
      if (path.charAt(0)=='/') {
         path = path.substring(1);
      }
      int slashPos = path.indexOf('/');
      if (slashPos<0) {
         return null;
      }
      String [] dbRef = new String[2];
      dbRef[0] = path.substring(0,slashPos);
      dbRef[1] = path.substring(slashPos+1);
      if (dbRef[0].length()==0) {
         dbRef[0] = DEFAULT_DB;
      }
      return dbRef;

   }

   /*
   protected Source getXQuerySource(DBBroker broker, Reference xqueryRef)
      throws PermissionDeniedException
   {
      Reference xqueryDBRef = xqueryRef.getRelativeRef(getRequest().getResourceRef().getBaseRef());
      if (xqueryDBRef==xqueryRef) {
         // the xquery is not in the database
         if (xqueryDBRef.getScheme().equals("file")) {
            return new FileSource(new File(xqueryRef.getSchemeSpecificPart(true)),"UTF-8",true);
         } else {
            return new URLSource(xqueryRef.toUrl());
         }
      } else {
         // the xquery is in the database
         String [] dbRef = getDBRef(xqueryDBRef.getRemainingPart());
         if (!dbRef[0].equals(dbName)) {
            // TODO: remove this limitation
            throw new IllegalArgumentException("Cannot ref to XQuery outside of the current database.");
         }
         DocumentImpl xqueryResource = (DocumentImpl)broker.getXMLResource(XmldbURI.create(dbRef[1]), Lock.READ_LOCK);
         return new DBSource(broker, (BinaryDocument)xqueryResource, true);
      }
   }
    *
    */
   protected Representation makeResultRepresentation(final BrokerPool brokerPool, final Subject user, final Sequence results,int howmany, final int start, long queryTime,final Properties outputProperties, final boolean wrap) {
      if (!results.isEmpty()) {
         int rlen = results.getItemCount();
         if ((start < 1) || (start > rlen)) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
         }
         // FD : correct bound evaluation
         if (((howmany + start) > rlen) || (howmany <= 0)) {
            howmany = rlen - start + 1;
         }
      } else {
         howmany = 0;
      }
      outputProperties.setProperty(Serializer.GENERATE_DOC_EVENTS, "false");
      final int currentHowmany = howmany;
      //getLogger().info("active="+brokerPool.active()+", available="+brokerPool.available());
      OutputRepresentation rep = new OutputRepresentation(MediaType.APPLICATION_XML) {

         public void write(OutputStream os)
                 throws IOException {
            DBBroker broker = null;
            try {
               broker = brokerPool.get(user);
               Serializer serializer = broker.getSerializer();
               serializer.reset();
               //Serialize the document
               Writer w = new OutputStreamWriter(os, "UTF-8");

               SAXSerializer sax = new SAXSerializer();
               sax.setOutput(w, outputProperties);
               serializer.setContentHandler(sax);

               serializer.toSAX(results, start, currentHowmany, wrap,false /* typed*/);

               w.flush();
               //w.close();
            } catch (SAXException ex) {
               throw new IOException("Cannot serialize query result due to: " + ex.getMessage());
            } catch (EXistException ex) {
               throw new IOException("Cannot serialize query result due to: " + ex.getMessage());
            } finally {
               if (broker != null) {
                  brokerPool.release(broker);
               }
            }
         }
      };
      rep.setCharacterSet(CharacterSet.UTF_8);
      return rep;
   }

   protected Representation executeXQuery(BrokerPool brokerPool,DBBroker broker,Subject user,Reference xqueryRef,XmldbURI [] knownDocuments,int howmany,int start,Properties outputProperties,Form [] parameterSets)
      throws XPathException,PermissionDeniedException
   {
      Logger log = getLogger();
      boolean isFineLog = log.isLoggable(Level.FINE);
      if (isFineLog) {
         log.fine("Using query resource: "+xqueryRef);
      }
      DocumentImpl xqueryResource = null;
      try {
         Reference xqueryDBRef = xqueryRef.getRelativeRef(getRequest().getResourceRef().getBaseRef());
         Source source = null;
         if (xqueryDBRef==xqueryRef) {
            // the xquery is not in the database
            //getLogger().info("xquery "+xqueryRef+" is outside database.");
            if (xqueryDBRef.getScheme().equals("file")) {
               source = new FileSource(new File(xqueryRef.getSchemeSpecificPart(true)),"UTF-8",true);
            } else {
               source = new URLSource(xqueryRef.toUrl());
            }
         } else {
            // the xquery is in the database
            String [] dbRef = getDBRef(xqueryDBRef.getRemainingPart());
            if (!dbRef[0].equals(dbName)) {
               // TODO: remove this limitation
               throw new PermissionDeniedException("Cannot ref to XQuery in "+dbRef[0]+" outside of the current database "+dbName);
            }
            //getLogger().info("xquery "+xqueryRef+" is inside database.");
            xqueryResource = (DocumentImpl)broker.getXMLResource(XmldbURI.create(dbRef[1]), Lock.READ_LOCK);
            source =  new DBSource(broker, (BinaryDocument)xqueryResource, true);
         }
         return executeXQuery(brokerPool,broker,user,source,knownDocuments,howmany,start,outputProperties,parameterSets);
      } finally {
         if (xqueryResource!=null) {
            if (isFineLog) {
               log.fine("Unlocking query resource: "+xqueryRef);
            }
            xqueryResource.getUpdateLock().release(Lock.READ_LOCK);
         }
      }
   }
   protected Representation executeXQuery(BrokerPool brokerPool,DBBroker broker,Subject user,Source source,XmldbURI [] knownDocuments,int howmany,int start,Properties outputProperties, Form [] parameterSets)
      throws XPathException,PermissionDeniedException
   {
      Logger log = getLogger();
      boolean isFineLog = log.isLoggable(Level.FINE);
      if (isFineLog) {
         log.fine("Query source, getting compiled.");
      }
      XQuery xquery = broker.getXQueryService();
      XQueryPool pool = xquery.getXQueryPool();
      CompiledXQuery compiled = pool.borrowCompiledXQuery(broker, source);
      XQueryContext context = null;
      if (compiled == null) {
         context = xquery.newContext(AccessContext.REST);
      } else {
         context = compiled.getContext();
      }
      if (isFineLog) {
         log.fine("Compiled found: "+(compiled!=null ? "yes" : "no"));
      }

      for (int i=0; i<parameterSets.length; i++) {
         for (String name : parameterSets[i].getNames()) {
            context.declareVariable(name,parameterSets[i].getValues(name));
         }
      }
      context.setAttribute(PARAMETER_NAME, parameterSets);

      context.setStaticallyKnownDocuments(knownDocuments);

      if (compiled == null) {
         if (isFineLog) {
            log.fine("Compiling source.");
         }
         Expression parameterNamesExpr = new ParameterNamesExpression(context);
         UserDefinedFunction parameterNamesFunction = new UserDefinedFunction(context,new FunctionSignature(PARAMETER_NAMES_QNAME,FunctionSignature.NO_ARGS,new SequenceType(Type.STRING,Cardinality.ZERO_OR_MORE)));
         parameterNamesFunction.setFunctionBody(parameterNamesExpr);
         context.declareFunction(parameterNamesFunction);

         Expression getParameterExpr = new GetParameterExpression(context);
         SequenceType nameArg = new SequenceType(Type.STRING,Cardinality.EXACTLY_ONE);
         UserDefinedFunction getParameterFunction = new UserDefinedFunction(context,new FunctionSignature(GET_PARAMETER_QNAME,FunctionSignature.singleArgument(nameArg),new SequenceType(Type.STRING,Cardinality.ZERO_OR_MORE)));
         getParameterFunction.setFunctionBody(getParameterExpr);
         getParameterFunction.addVariable("name");
         context.declareFunction(getParameterFunction);

         try {
            compiled = xquery.compile(context, source);
         } catch (IOException ex) {
            getContext().getLogger().log(Level.SEVERE,"Failed to compile xquery: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return new StringRepresentation("Failed to compile xquery: "+ex.getMessage());
         }
      }
      if (isFineLog) {
         log.fine("Running query.");
      }
      checkPragmas(context, outputProperties);
      try {
         Sequence result = xquery.execute(compiled, null);
         return makeResultRepresentation(brokerPool,user, result, howmany, start, 0, outputProperties, false);
      } finally {
         pool.returnCompiledXQuery(source, compiled);
         if (isFineLog) {
            log.fine("Query finished");
         }
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

   protected Representation get() {
      if (dbName==null) {
         getContext().getLogger().warning("Missing database name in request URI.");
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }

      BrokerPool pool = null;
      try {
         pool = BrokerPool.getInstance(dbName);
         if (pool==null) {
            getContext().getLogger().severe("eXist database "+dbName+" is not available.");
            getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return null;
         }
         //getLogger().info("Get on: "+request.getResourceRef());
         //getLogger().info("active="+pool.active()+", available="+pool.available());
         final Subject user = getUser(pool);

         final DBBroker broker = pool.get(user);
         try {
            Object xqueryRefAttr = getXQueryReference();
            DocumentImpl resource = null;
            final XmldbURI pathUri = XmldbURI.create(dbPath);
            try {
               resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);

               if (resource == null) {

                  // Must be a collection
                  final Collection collection = broker.getCollection(pathUri);
                  if (collection != null) {
                     if (!collection.getPermissions().validate(broker.getUser(), Permission.READ)) {
                        throw new PermissionDeniedException("Not allowed to read collection");
                     }
                     if (xqueryRefAttr==null) {

                        Representation output = new OutputRepresentation(MediaType.APPLICATION_XML) {
                           public void write(OutputStream os)
                              throws IOException
                           {
                              Writer out = new OutputStreamWriter(os,"UTF-8");
                              out.write("<contents>\n");
                              for (Iterator i = collection.collectionIterator(); i.hasNext();) {
                                  XmldbURI child = (XmldbURI) i.next();
                                  out.write("<collection name='"+child.toString()+"'/>\n");
                              }

                              for (Iterator i = collection.iterator(broker); i.hasNext();) {
                                  DocumentImpl doc = (DocumentImpl) i.next();
                                  XmldbURI resource = doc.getFileURI();
                                  out.write("<resource name='"+resource.toString()+"'/>\n");
                              }
                              out.write("</contents>\n");
                              out.flush();
                           }
                        };
                        output.setCharacterSet(CharacterSet.UTF_8);
                        getResponse().setStatus(Status.SUCCESS_OK);
                        return output;
                     } else {
                        Reference xqueryRef = xqueryRefAttr instanceof Reference ? (Reference)xqueryRefAttr : new Reference(getRequest().getResourceRef(),xqueryRefAttr.toString());

                        Properties outputProperties = new Properties(defaultProperties);
                        try {
                           Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), getAttributesAsForm() };
                           return executeXQuery(pool,broker,user,xqueryRef,new XmldbURI[] { collection.getURI() },-1,1,outputProperties,parameterSets);
                        } catch (XPathException ex) {
                           getLogger().log(Level.SEVERE,"Exception while processing query",ex);
                           getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                           return new StringRepresentation("Error while executing query: "+ex.getMessage());
                        }

                     }
                  } else {
                     getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                     return null;
                  }
               } else {
                  final BrokerPool currentPool = pool;
                  if (resource.getResourceType() == DocumentImpl.BINARY_FILE) {
                     Representation entity = new OutputRepresentation(MediaType.valueOf(resource.getMetadata().getMimeType())) {
                        public void write(OutputStream os)
                           throws IOException
                        {
                           DBBroker broker = null;
                           DocumentImpl currentResource = null;
                           try {
                              broker = currentPool.get(user);
                              currentResource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
                              if (getLogger().isLoggable(Level.FINE)) {
                                 getLogger().fine("Reading binary resource "+currentResource.getBaseURI());
                              }
                              broker.readBinaryResource((BinaryDocument)currentResource,os);
                              if (getLogger().isLoggable(Level.FINE)) {
                                 getLogger().fine("Finsihed reading binary resource "+currentResource.getBaseURI());
                              }
                           } catch (Exception ex) {
                              getLogger().log(Level.SEVERE,"Cannot get document for serialization.",ex);
                           } finally {
                              if (currentResource!=null) {
                                 currentResource.getUpdateLock().release(Lock.READ_LOCK);
                              }
                              if (broker!=null) {
                                 currentPool.release(broker);
                              }
                           }
                        }
                     };
                     long tstamp = resource.getMetadata().getLastModified();
                     entity.setTag(new Tag(Long.toString(tstamp),false));
                     getResponse().setStatus(Status.SUCCESS_OK);
                     return entity;
                  } else {
                     if (xqueryRefAttr==null) {
                        resource.getUpdateLock().release(Lock.READ_LOCK);
                        Representation rep = new OutputRepresentation(MediaType.valueOf(resource.getMetadata().getMimeType())) {
                           public void write(OutputStream os)
                              throws IOException
                           {
                              DBBroker broker = null;
                              DocumentImpl currentResource = null;
                              try {
                                 broker = currentPool.get(user);
                                 currentResource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
                                 Serializer serializer = broker.getSerializer();
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
                              } catch (Exception ex) {
                                 getLogger().log(Level.SEVERE,"Cannot get document for serialization.",ex);
                              } finally {
                                 if (currentResource!=null) {
                                    currentResource.getUpdateLock().release(Lock.READ_LOCK);
                                 }
                                 if (broker!=null) {
                                    currentPool.release(broker);
                                 }
                              }
                           }
                        };
                        long tstamp = resource.getMetadata().getLastModified();
                        rep.setCharacterSet(CharacterSet.UTF_8);
                        rep.setTag(new Tag(Long.toString(tstamp),false));
                        getResponse().setStatus(Status.SUCCESS_OK);
                        return rep;
                     } else {

                        Reference xqueryRef = xqueryRefAttr instanceof Reference ? (Reference)xqueryRefAttr : new Reference(getRequest().getResourceRef(),xqueryRefAttr.toString());

                        Properties outputProperties = new Properties(defaultProperties);
                        try {
                           Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), getAttributesAsForm() };
                           return executeXQuery(pool,broker,user,xqueryRef,new XmldbURI[] { resource.getURI() },-1,1,outputProperties,parameterSets);
                        } catch (XPathException ex) {
                           getLogger().log(Level.SEVERE,"Exception while processing query",ex);
                           getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                           return new StringRepresentation("Error while executing query: "+ex.getMessage());
                        } finally {
                           if (resource!=null) {
                              resource.getUpdateLock().release(Lock.READ_LOCK);
                           }
                        }
                        
                     }
                  }
               }
            } catch(PermissionDeniedException ex) {
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,ex.getMessage());
               if (resource!=null) {
                  resource.getUpdateLock().release(Lock.READ_LOCK);
               }
               return null;
            }
         } finally {
            pool.release(broker);
         }
      } catch (EXistException ex) {
         getContext().getLogger().log(Level.SEVERE,"XMLDB request failed: "+ex.getMessage(),ex);
         getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
         return new StringRepresentation("XMLDB request failed: "+ex.getMessage());
      }
   }

   protected Representation handleFormPost(Representation entity, BrokerPool pool, DBBroker broker, Subject user) {
      Object xqueryRefAttr = getXQueryReference();
      if (xqueryRefAttr==null) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }
      
      DocumentImpl resource = null;
      final XmldbURI pathUri = XmldbURI.create(dbPath);
      try {
         resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);

         if (resource == null) {

            // Must be a collection
            final Collection collection = broker.getCollection(pathUri);
            if (collection != null) {
               if (!collection.getPermissions().validate(broker.getUser(), Permission.READ)) {
                  throw new PermissionDeniedException("Not allowed to read collection");
               }
               Reference xqueryRef = xqueryRefAttr instanceof Reference ? (Reference)xqueryRefAttr : new Reference(getRequest().getResourceRef(),xqueryRefAttr.toString());

               Properties outputProperties = new Properties(defaultProperties);
               try {
                  Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), new Form(entity), getAttributesAsForm() };
                  return executeXQuery(pool,broker,user,xqueryRef,new XmldbURI[] { collection.getURI() },-1,1,outputProperties,parameterSets);
               } catch (XPathException ex) {
                  getLogger().log(Level.SEVERE,"Exception while processing query",ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return new StringRepresentation("Error while executing query: "+ex.getMessage());
               }

            } else {
               getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
               return null;
            }
         } else {
            final BrokerPool currentPool = pool;
            if (resource.getResourceType() == DocumentImpl.BINARY_FILE) {
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return null;
            } else {
               Reference xqueryRef = xqueryRefAttr instanceof Reference ? (Reference)xqueryRefAttr : new Reference(getRequest().getResourceRef(),xqueryRefAttr.toString());

               Properties outputProperties = new Properties(defaultProperties);
               try {
                  Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), new Form(entity), getAttributesAsForm() };
                  return executeXQuery(pool,broker,user,xqueryRef,new XmldbURI[] { resource.getURI() },-1,1,outputProperties,parameterSets);
               } catch (XPathException ex) {
                  getLogger().log(Level.SEVERE,"Exception while processing query",ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return new StringRepresentation("Error while executing query: "+ex.getMessage());
               } finally {
                  if (resource!=null) {
                     resource.getUpdateLock().release(Lock.READ_LOCK);
                  }
               }
            }
         }
      } catch (PermissionDeniedException ex) {
         getLogger().log(Level.SEVERE,"Permission denied to query.",ex);
         getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
         return null;
      }

   }
   
   protected Representation post(Representation entity) {
      if (dbName==null) {
         getContext().getLogger().warning("Missing database name in request URI.");
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }

      Properties outputProperties = new Properties(defaultProperties);
      XmldbURI pathUri = XmldbURI.create(dbPath);
      DocumentImpl resource = null;
      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         try {
            pool = BrokerPool.getInstance(dbName);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+dbName+" is not available.");
               getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return null;
            }
            //getLogger().info("active="+pool.active()+", available="+pool.available());
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+dbName+" is not available.",ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         Subject user = getUser(pool);
         try {
            broker = pool.get(user);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         try {
            // check if path leads to an XQuery resource.
            // if yes, the resource is loaded and the XQuery executed.
            resource = (DocumentImpl) broker.getXMLResource(pathUri, Lock.READ_LOCK);
            if (resource != null && resource.getResourceType() == DocumentImpl.BINARY_FILE &&
                resource.getMetadata().getMimeType()!=null &&
                resource.getMetadata().getMimeType().startsWith("application/xquery")) {
               // found an XQuery resource
               try {
                  Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), getAttributesAsForm() };
                  return executeXQuery(pool,broker,user, new DBSource(broker, (BinaryDocument)resource, true), new XmldbURI[] { resource.getCollection().getURI() },-1,1,outputProperties,parameterSets);
               } catch (XPathException ex) {
                  getLogger().log(Level.WARNING,"Exception while executing xquery.",ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return new StringRepresentation("Error while processing query: "+ex.getMessage());

               }
            }
         } catch (PermissionDeniedException ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return new StringRepresentation("Unauthorized to access: "+ex.getMessage());
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
         String incomingMediaType = entity.getMediaType().getName();
         if (incomingMediaType.startsWith("application/x-www-form-urlencoded")) {
            try {
               return handleFormPost(entity,pool,broker,user);
            } finally {
               entity.release();
            }
         } else if (incomingMediaType.startsWith("application/xquery")) {
            try {
               query = entity.getText();
            } catch (IOException ex) {
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return new StringRepresentation("I/O error processing request: "+ex.getMessage());
            } finally {
               entity.release();
            }
         } else if (incomingMediaType.equals("text/xml") ||
                    incomingMediaType.equals("application/xml") ||
                    incomingMediaType.endsWith("+xml")) {
            mime = incomingMediaType;
            String content = null;

            try {
               content = entity.getText();
            } catch (IOException ex) {
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return new StringRepresentation("I/O error processing request: "+ex.getMessage());
            } finally {
               entity.release();
            }

            InputSource src = new InputSource(new StringReader(content));
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;
            try {
               docBuilder = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
               getLogger().log(Level.SEVERE,"Error building document builder.",ex);
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            }

            Document doc = null;

            try {
               doc = docBuilder.parse(src);
            } catch (IOException ex) {
               getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
               return new StringRepresentation("I/O error while processing document: "+ex.getMessage());
            } catch (SAXException ex) {
               getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
               return new StringRepresentation("Cannot parse document: "+ex.getMessage());
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
                 MutableDocumentSet docs = new DefaultDocumentSet();
                 Collection collection = broker.getCollection(pathUri);
                 if (collection != null) {
                    collection.allDocs(broker, docs, true, true);
                 } else {
                    try {
                       DocumentImpl xupdateDoc = (DocumentImpl) broker.getXMLResource(pathUri);
                       if (xupdateDoc != null) {
                           if (!xupdateDoc.getPermissions().validate(
                                   broker.getUser(), Permission.READ)) {
                              getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                              return new StringRepresentation("Not allowed to read collection.");
                           }
                           docs.add(xupdateDoc);
                       } else {
                          broker.getAllXMLResources(docs);
                       }
                    } catch (PermissionDeniedException ex) {
                       getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                       return new StringRepresentation("Permission denied to document "+pathUri+": "+ex.getMessage());
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

                    getResponse().setStatus(Status.SUCCESS_OK);
                    return new StringRepresentation(
                           "<?xml version='1.0'?>\n"
                           + "<exist:modifications xmlns:exist='" + NS
                           + "' count='" + mods + "'>" + mods
                           + "modifications processed.</exist:modifications>",MediaType.APPLICATION_XML);
                 } catch (XPathException ex) {
                    transact.abort(transaction);
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return new StringRepresentation("XQuery error while processing update: "+ex.getMessage());
                 } catch (EXistException ex) {
                    transact.abort(transaction);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    return new StringRepresentation("Error processing update: "+ex.getMessage());
                 } catch (LockException ex) {
                    transact.abort(transaction);
                    getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                    return new StringRepresentation("Resource cannot be locked: "+ex.getMessage());
                 } catch (SAXException ex) {
                    transact.abort(transaction);
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return new StringRepresentation("XML error parsing update: "+ex.getMessage());
                 } catch (IOException ex) {
                    getContext().getLogger().log(Level.SEVERE,"I/O error during update.",ex);
                    transact.abort(transaction);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    return new StringRepresentation("I/O error during update: "+ex.getMessage());
                 } catch (ParserConfigurationException ex) {
                    getContext().getLogger().log(Level.SEVERE,"Parser configuration error.",ex);
                    transact.abort(transaction);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    return null;
                 } catch (PermissionDeniedException ex) {
                    transact.abort(transaction);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                    return new StringRepresentation("Permission denied for update to "+pathUri+": "+ex.getMessage());
                 }
            } else {
               // just compile the doc as a query and hope for the best!
               query = content;
            }
         }

         if (query==null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No query was specified.");
         }

         if (resource==null) {
            // must be a collection
            Collection collection = broker.getCollection(pathUri);
            if (collection==null) {
               getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
               return null;
            }
            if (!collection.getPermissions().validate(broker.getUser(), Permission.READ)) {
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation("Unauthorized to read collection.");
            }
         }
         TransactionManager transact = broker.getBrokerPool().getTransactionManager();
         Txn transaction = transact.beginTransaction();
         try {
             // execute query
            Form [] parameterSets = { getRequest().getResourceRef().getQueryAsForm(), getAttributesAsForm() };
            Representation rep = executeXQuery(pool,broker,user, new StringSource(query), new XmldbURI[] { pathUri },howmany,start,outputProperties,parameterSets);
            transact.commit(transaction);
            return rep;
         } catch (XPathException ex) {
            transact.abort(transaction);
            getLogger().log(Level.WARNING,"Exception while processing query.",ex);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Exception while processing query: "+ex.getMessage());
         } catch (EXistException e) {
            transact.abort(transaction);
            getLogger().log(Level.WARNING,"Exception while processing query.",e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation(e.getMessage());
         } catch (PermissionDeniedException ex) {
            transact.abort(transaction);
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return new StringRepresentation("Permission denied for update to "+pathUri+": "+ex.getMessage());
         }
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }

   }
   
   protected Representation delete() {
      if (dbName==null) {
         getContext().getLogger().warning("Missing database name in request URI.");
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }
      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         try {
            pool = BrokerPool.getInstance(dbName);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+dbName+" is not available.");
               getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return null;
            }
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+dbName+" is not available.",ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         try {
            Subject user = getUser(pool);
            broker = pool.get(user);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         XmldbURI pathUri = XmldbURI.create(dbPath);
         TransactionManager transact = broker.getBrokerPool().getTransactionManager();
         Txn txn = transact.beginTransaction();
         try {
            Collection collection = broker.getCollection(pathUri);
            if (collection != null) {
                // remove the collection
              broker.removeCollection(txn, collection);
              getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
            } else {
               DocumentImpl doc = (DocumentImpl) broker.getXMLResource(pathUri);
               if (doc == null) {
                  transact.abort(txn);
                  getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
               } else {
                  if (doc.getResourceType() == DocumentImpl.BINARY_FILE) {
                     doc.getCollection().removeBinaryResource(txn, broker,pathUri.lastSegment());
                  } else {
                     doc.getCollection().removeXMLResource(txn, broker, pathUri.lastSegment());
                  }
                  getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
               }
            }
            transact.commit(txn);
            return null;
         } catch (PermissionDeniedException ex) {
            transact.abort(txn);
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return new StringRepresentation(ex.getMessage());
         } catch (TriggerException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Trigger failed: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         } catch (IOException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"I/O error: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         } catch (LockException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Unable to get lock: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         } catch (TransactionException ex) {
            transact.abort(txn);
            getContext().getLogger().log(Level.SEVERE,"Transaction failed: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }
   }
   
   protected Representation put(Representation entity) {
      if (dbName==null) {
         getContext().getLogger().warning("Missing database name in request URI.");
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }

      DBBroker broker = null;
      BrokerPool pool = null;
      try {
         try {
            pool = BrokerPool.getInstance(dbName);
            if (pool==null) {
               getContext().getLogger().severe("eXist database "+dbName+" is not available.");
               getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
               return null;
            }
            //getLogger().info("active="+pool.active()+", available="+pool.available());
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"eXist database "+dbName+" is not available.",ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         try {
            final Subject user = getUser(pool);
            broker = pool.get(user);
         } catch (EXistException ex) {
            getContext().getLogger().log(Level.SEVERE,"Cannot get broker from pool: "+ex.getMessage(),ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
         }
         XmldbURI pathUri = XmldbURI.create(dbPath);

         // Test to see if the path is a collection (put should fail)
         Collection collection = broker.getCollection(pathUri);
         if (collection!=null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("A put is not allowed to a collection path.  A file segment must be specified.");
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
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation("Unauthorized to read colleciton "+collectionUri);
            } catch (Exception ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Failed to create collection "+collectionUri+" in eXist: "+ex.getMessage(),ex);
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            }
         }

         // Check for XML media type
         MediaType type = entity.getMediaType();
         if (type==null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Media type is missing on content body.");
         }
         
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
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            }
            String charset = type.getParameters().getValues("charset");
            if (charset==null) {
               charset = "UTF-8";
            }
            //getContext().getLogger().info("charset="+charset);
            try {
               InputStream is = entity.getStream();

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
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            }

            // Check to see if it is a binary and remove it.
            try {
               DocumentImpl resource = (DocumentImpl) broker.getXMLResource(docUri);
               if (resource!=null && resource.getResourceType() == DocumentImpl.BINARY_FILE) {
                  resource.getCollection().removeBinaryResource(transaction, broker,docUri.lastSegment());

               }
            } catch (PermissionDeniedException ex) {
               transact.abort(transaction);
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
            } catch (LockException ex) {
               transact.abort(transaction);
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
            } catch (TriggerException ex) {
               transact.abort(transaction);
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
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
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            } catch (PermissionDeniedException ex) {
               transact.abort(transaction);
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
            } catch (SAXException ex) {
               transact.abort(transaction);
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
            } catch (Exception ex) {
               transact.abort(transaction);
               getContext().getLogger().log(Level.SEVERE,"Failed to store xml into eXist at "+dbPath+" : "+ex.getMessage(),ex);
               getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
               return null;
            }

         } else {
            long size = entity.getSize();
            if (size<0) {
               File tempFile = null;
               try {
                  tempFile = File.createTempFile("exist","bin");
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot create temporary file: "+ex.getMessage(),ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               }
               try {
                  InputStream is = entity.getStream();

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
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               }
               try {
                  FileInputStream is = new FileInputStream(tempFile);
                  collection.addBinaryResource(transaction, broker, docUri, is, type.toString(), (int) tempFile.length());
                  is.close();
                  transact.commit(transaction);
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot read temp file for binary storage: "+ex.getMessage(),ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               } catch (PermissionDeniedException ex) {
                  transact.abort(transaction);
                  getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,ex.getMessage());
                  return null;
               } catch (Exception ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Failed to store binary into eXist at" +dbPath+" : "+ex.getMessage(),ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               }
            } else {
               if (size>Integer.MAX_VALUE) {
                  getResponse().setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                  return new StringRepresentation("Entity size "+size+" is too large.");
               }
               try {
                  collection.addBinaryResource(transaction, broker, docUri, entity.getStream(), type.toString(), (int)size);
                  transact.commit(transaction);
               } catch (IOException ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Cannot read temp file for binary storage: "+ex.getMessage(),ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               } catch (PermissionDeniedException ex) {
                  transact.abort(transaction);
                  getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,ex.getMessage());
                  return null;
               } catch (Exception ex) {
                  transact.abort(transaction);
                  getContext().getLogger().log(Level.SEVERE,"Failed to store binary into eXist at "+dbPath+" : "+ex.getMessage(),ex);
                  getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                  return null;
               }
            }
         }
         getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         return null;
      } finally {
         if (broker!=null) {
            pool.release(broker);
         }
      }

   }
   
   protected Representation head() {
      if (dbName==null) {
         getContext().getLogger().warning("Missing database name in request URI.");
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         return null;
      }

      try {
         BrokerPool pool = BrokerPool.getInstance(dbName);
         if (pool==null) {
            getContext().getLogger().severe("eXist database "+dbName+" is not available.");
            getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return null;
         }
         //getLogger().info("active="+pool.active()+", available="+pool.available());
         final Subject user = getUser(pool);
         DBBroker broker = pool.get(user);
         DocumentImpl resource = null;
         try {
            XmldbURI pathUri = XmldbURI.create(dbPath);
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
                  if (collection != null && dbPath.equals(colPath)) {
                     getResponse().setStatus(Status.SUCCESS_OK);
                  } else {
                     getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                  }
                  return null;
               } else {
                  String mediaTypeS = resource.getMetadata().getMimeType();
                  int semicolon = mediaTypeS.indexOf(';');
                  String charset = null;
                  if (semicolon>0) {
                     String rest = mediaTypeS.substring(semicolon);
                     rest = rest.trim();
                     if (rest.startsWith("charset=")) {
                        charset = rest.substring(8);
                     }
                     mediaTypeS = mediaTypeS.substring(0,semicolon);
                  }
                  MediaType type = MediaType.valueOf(mediaTypeS);
                  Representation rep = new StringRepresentation("",type);
                  rep.setModificationDate(new Date(resource.getMetadata().getLastModified()));
                  if (charset!=null) {
                     rep.setCharacterSet(CharacterSet.valueOf(charset));
                  }
                  long tstamp = resource.getMetadata().getLastModified();
                  rep.setTag(new Tag(Long.toString(tstamp),false));
                  getResponse().setStatus(Status.SUCCESS_OK);
                  return rep;
               }
            } catch(PermissionDeniedException ex) {
               getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
               return new StringRepresentation(ex.getMessage());
            }
         } finally {
            if (resource!=null) {
               resource.getUpdateLock().release(Lock.READ_LOCK);
            }
            pool.release(broker);
         }
      } catch (EXistException ex) {
         getContext().getLogger().log(Level.SEVERE,"XMLDB request failed: "+ex.getMessage(),ex);
         getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
         return new StringRepresentation("XMLDB request failed: "+ex.getMessage());
      }
   }
   
}
