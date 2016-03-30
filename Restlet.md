# 1. Introduction #

The eXist-restlet subproject provides an integration with the [Restlet](http://www.restlet.org) project.  It provides a simple way to integrate an embedded version of [eXist](http://www.exist-db.org) into a web application.

The handling of services, web interfaces, etc. is all handled via regular Restlet
code.  You can then attach exist services either directly or indirectly using the XMLDBResource
class that is an instance of [ServerResource](http://www.restlet.org/documentation/2.0/jse/api/index.html?org/restlet/resource/ServerResource.html).

Interactions with the eXist database are usually performed with internal requests as if you are client of the database but with optimized local routing via the internals of Restlet.

# 2. Running Restlet with eXist Embedded #

You can run a Restlet application or bind resources and queries within an eXist database directly to paths on the server using the Component configuration XML syntax available in Restlet.  For example, the following is an example configuration:

```
<component xmlns="http://www.restlet.org/schemas/2.0/Component">
    <client protocol="CLAP" />
    <client protocol="FILE" />
    <client protocols="HTTP HTTPS" />
    <server protocols="HTTP" port='8080'/>
    <server protocols="HTTPS" port='8081'/>
    <!-- start a database named 'db' from the configuration file -->
    <parameter name="org.exist.xmldb.db.conf" value="db=conf.xml"/>
    <host hostDomain="localhost" hostPort='8080'>
       <!-- attaches the database application to /db -->
       <attach uriPattern="/db/" matchingMode="1" targetClass="org.exist.restlet.XMLDBApplication">
          <parameter name="org.exist.xmldb.db.name" value="db"/>
       </attach>
       <!-- attaches an example XQuery to /echo -->
       <attach uriPattern="/echo" matchingMode="1" targetClass="org.exist.restlet.XMLDBResource">
          <parameter name="org.exist.xmldb.xquery" href="echo.xql"/>
          <parameter name="org.exist.xmldb.db.name" value="db"/>
       </attach>
       <!-- attaches an example XQuery to /*/*/echo -->
       <attach uriPattern="/{A}/{B}/echo" matchingMode="1" targetClass="org.exist.restlet.XMLDBResource">
          <parameter name="org.exist.xmldb.xquery" href="echo-with-vars.xql"/>
          <parameter name="org.exist.xmldb.db.name" value="db"/>
       </attach>
    </host>
    <internalRouter>
       <!-- attaches the database to the internal router for RIAP access -->
       <attach uriPattern="/exist/" matchingMode="1" targetClass="org.exist.restlet.XMLDBResource"/>
    </internalRouter>
</component>
```

This configuration file can be run directly using the `org.exist.restlet.WebComponent` class.  For example, if you are using the distribution jar files:

```
   java -jar exist-restlet.jar server.xml
```

otherwise, you can do:

```
   java -cp exist-restlet.jar:...  org.exist.restlet.WebComponent server.xml
```

A component configuration should contain a number of database configuration parameters.  The name of the parameter is `org.exist.xmldb.db.conf` and it must have the format of `{name}={file}`.  The database will then be accessible via the name given.

For example:

```
    <parameter name="org.exist.xmldb.db.conf" value="db=conf.xml"/>
```

If you plan on using RIAP to access your database, you should add the following to the `internalRouter` element:

```
    <internalRouter>
       <attach uriPattern="/exist/" matchingMode="1" targetClass="org.exist.restlet.XMLDBResource"/>
    </internalRouter>
```

You can complete expose the database by adding a similar `attach` element to any of the `host` elements.

To have an authenticated database resource hierarchy, add this configuration to one of your `host` elements:

```
       <attach uriPattern="/db/" matchingMode="1" targetClass="org.exist.restlet.XMLDBApplication">
          <parameter name="org.exist.xmldb.db.name" value="db"/>
       </attach>
```

The `uriPattern` attribute can contain any URI path pattern you wish but the configuration must have a `parameter` element with the name of the database.

Finally, you can expose resources via queries using:

```
       <attach uriPattern="/echo" matchingMode="1" targetClass="org.exist.restlet.XMLDBResource">
          <parameter name="org.exist.xmldb.xquery" href="echo.xql"/>
          <parameter name="org.exist.xmldb.db.name" value="db"/>
       </attach>
```

The `org.exist.xmldb.xquery` parameter can be any URI.  If you want to point into the database, use an RIAP URI reference (e.g. riap://component/exist/db/echo.xql).

# 3. API Overview #

## 3.1 Databases ##

While eXist has an API for manipulating databases, the Restlet integration has a simplified API for starting, stopping, and interacting with databases.  All you need to do is import the org.exist.restlet.XMLDB class and reference your eXist configuration file as follows:

```
   XMLDB xmldb = new XMLDB("db",new File("conf.xml"));
```

Once you've created your database, you can start you database in the appropriate place.  Typically, this is done in your Restlet [Component](http://www.restlet.org/documentation/2.0/jse/api/index.html?org/restlet/Component.html) derived `start()` method but can happen wherever you'd like:

```
   xmldb.start();
```

You'll also want to add a call to `stop()` in the appropriate place so your database shutdown happens when the server is shutdown.

Finally, you can reindex collections by the following:

```
   xmldb.reindex("/my/docs/");
```

## 3.2 Using RIAP Schemes for eXist Access ##

One way to access eXist from you Restlet code is attach the `XMLDBResource` class to the internal router of your `Component` instance:

```
      getInternalRouter().attach("/exist/",XMLDBResource.class).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
```

This will allow you to access eXist using URI references as:

```
   riap://component/exist/db/doc.xml
```

The resource expects the first path segment it receives to be the database name you used when you created and started your database using the `XMLDB` class.  The remaining path is the collection or resource path with which you are interacting.  The way this is parsed is the authority `component` tells Restlet to route using the internal router.  The next segment `exist` is what we specified when we attached the `XMLDBResource` class to the router.  The remaining path (`db/doc.xml`) is used by the XMLDBResource instance to find the content in the database.

You can attach this to your internal component or application router however you like.  The subsequent path segments will be interpreted the same.   For example, using this code:

```
getInternalRouter().attach("/data/xml/",XMLDBResource.class).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
```

changes the previous URI to:

```
   riap://component/data/xml/db/doc.xml
```

## 3.3 Creating a Simple Proxy Restlet ##

```

      Restlet proxy = new Restlet() {
         public void handle(Request request, Response response) {
            // The reference to the resource
            Reference ref = request.getResourceRef();
            // The remaining part after the matching prefix
            String path = ref.getRemainingPart();
            
            // Create a RIAP scheme reference to the database
            Reference dbRef = LocalReference.createRiapReference(LocalReference.RIAP_COMPONENT, "/exist/"+dbName+"/"+path);
            dbRef.setQuery(ref.getQuery());
            dbRef.setFragment(ref.getFragment());
            
            // Create a database request with the same method
            Request dbRequest = new Request(request.getMethod(),dbRef);
            // Set the entity both from the request
            dbRequest.setEntity(request.getEntity());
            // Copy the headers from the request
            dbRequest.getAttributes().put("org.restlet.http.headers",request.getAttributes().get("org.restlet.http.headers"));
            
            // Interact with eXist
            Response dbResponse = getContext().getClientDispatcher().handle(dbRequest);
            
            // Copy the results to the response
            response.setEntity(dbResponse.getEntity());
            response.setStatus(dbResponse.getStatus());
            response.getAttributes().put("org.restlet.http.headers",dbResponse.getAttributes().get("org.restlet.http.headers"));
         }
      };

```

## 4 Interacting with eXist ##
## 4.1 Getting a Document ##

To get a resource out of eXist, just make a simple GET request in Restlet:

```
    Response response = getContext().getClientDispatcher().handle(new Request(Method.GET,"riap://component/exist/db/doc.xml"));
```

If you want to apply a query to the resource or collection, you can add an attribute that points to the query:

```
    Request getWithQuery = new Request(Method.GET,"riap://component/exist/db/doc.xml");
    getWithQuery.getAttributes().put(XMLDBResource.XQUERY_ATTR,"riap://component/exist/db/query.xq");
    Response response = getContext().getClientDispatcher().handle(getWithQuery);
```

Your xquery can be wherever you want as long as you can point to it by a URI.  The only current limitation is that you can't use an XQuery stored in a different database than the resource you are querying.  This limitation will be removed in the future.

The HEAD method works almost exactly the same except you change the code to use `Method.HEAD`.

## 4.2 Storing a Document ##

Storing a document is accomplished by a PUT request:

```
    Request storeRequest = new Request(Method.PUT,"riap://component/exist/db/doc.xml");
    storeRequest.setEntity(new StringRepresentation("<doc><title>Something</title></doc>",MediaType.APPLICATION_XML));
    Response response = getContext().getClientDispatcher().handle(storeRequest);
```

## 4.3 Querying ##

You can make adhoc queries via a POST request:
```
    MediaType queryType = MediaType.valueOf("application/xquery");
    Request queryRequest = new Request(Method.POST,"riap://component/exist/db/doc.xml");
    queryRequest.setEntity(new StringRepresentation("<doc><{//a}</doc>",queryType));
    Response response = getContext().getClientDispatcher().handle(queryRequest);
```

The XUpdate and eXist XML structures for querying and modifying your database are available as well via the same method.

## 4.4 Removing Content ##

The DELETE method is used to delete resources (documents or collections).  For example:
```
    Response response = getContext().getClientDispatcher().handle(new Request(Method.DELETE,"riap://component/exist/db/doc.xml"));
```


# 5. Building #

The restlet integration relies upon the embedded version of [eXist](http://www.exist-db.org).  You can build the embedded version by the following instructions

  1. Checkout the `trunk/eXist` and `trunk/embedded` directories of eXist from eXist's subversion
  1. Build eXist.
  1. Execute the `import-exist` ant target.
  1. Build the embedded project via the `jar` ant target.

You can also skip this step and use the version checked into subversion for this project.

Once you have the embedded version of eXist, you can build the restlet project by:

  1. Checkout the `trunk/restlet` from this project's subversion.
  1. If you have build your own copy of eXist/embeded, execute the `import-embedded` ant target.  You'll probably need to set the `embedded.dir` parameter to directory of your embedded version of eXist.
  1. Build the project with the `jar` ant target.

All of these projects (eXist, embedded eXist, and this project) are also [Netbeans](http://www.netbeans.org) projects.

# 6. Classes #

The `org.exist.restlet` package:

  * The `XMLDB` class allows you to start, stop, and managed named database instances.
  * The `XMLDBApplication` proxies the eXist as resources served by the Restlet engine and includes the admin application at `/_/admin/`.
  * The `XMLDBResource` represents a resource stored in the eXist database.
  * The `WebComponent` is a component that wraps the Application and can be used to instantiate a server bound to a host address.  It is also a simple example of how to create your own components and attach eXist to internal routers.  You can run this directly from the command-line and pass it the XML configuration.

The `org.exist.restlet.admin` package:

  * The `XMLDBAdminApplication` is a Restlet [Application](http://www.restlet.org/documentation/2.0/jse/api/index.html?org/restlet/Application.html) that provides simple user administration.
  * The `UsersResource` is the interface to the list of users.
  * The `UserResource` is the interface to a particular user.

The `org.exist.restlet.tools` package:

  * The `Importer` is a simple tool for importing content via the server over HTTP.  It will import a set of files or directories verbatim into your database via the server run via the `org.exist.restlet.Main` class.