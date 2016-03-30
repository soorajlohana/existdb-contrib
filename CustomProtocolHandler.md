# Custom protocol handler "xmldb:exist://" #

[![](http://www.exist-db.org/logo.jpg)](http://www.exist-db.org/)

## In short ##

Classes to deal with xmldb:exist// style URL's. With these classes it is possible to download (get) and upload (put) documents from/to the eXist-db using standard Java classes (java.net.URL).

**As of 20070430 this code has been integrated into the core of eXist-db - 1.2+**

## Introduction ##

Under certain conditions it is handy to be able to access data stored in eXist via a **xmldb:exist://** type URL, straight out of javacode. The code below shows how to do this.

To be able to use this special URL you can either
  * set the value 'org.exist.protocols' to (java) system property 'java.protocol.handler.pkgs'
  * call URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());

## Usage for JVM ##
start jvm with `java -Djava.protocol.handler.pkgs=org.exist.protocolhandler.protocols -jar ......`


## Usage in Java code (example) ##

```
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.exist.protocolhandler.xmldb.XmldbURLStreamHandlerFactory;

...

// ************************
// Setup protocol handler

BasicConfigurator.configure();

// OR use this
System.setProperty( 'java.protocol.handler.pkgs', 'org.exist.protocolhandler.protocols' );

// OR use this
URL.setURLStreamHandlerFactory(new XmldbURLStreamHandlerFactory());

URL url = new URL('xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/shakespeare/plays/macbeth.xml');


// ************************
// Read from URL
 
// InputStream os = url.openConnection().getInputStream ();
InputStream is = url.openStream();
            
... (handle inputstream) ...
            
is.close();


// ************************
// Write to URL
OutputStream os = url.openConnection().getOutputStream();

```

## Dependancies ##
The code does have a dependancy with the eXist jar files, in particular:
  * exist.jar (1.1.2dev, rev5524+)
  * exist-optional.jar (to be checked)
  * log4j-1.2.14.jar
  * xmldb.jar
  * xmlrpc-1.2-patched.jar

## Status ##
The code is fully functional for both embedded and remote URLs. It is also possible to supply username/password information to the URL. Example:

  * xmldb:exist:///db/foobar/document.xml
  * xmldb:exist://username:password@/db/foobar/document.xml
  * xmldb:exist://localhost:8080/exist/xmlrpc/db/foobar/document.xml
  * xmldb:exist://username:password@localhost:8080/exist/xmlrpc/db/foobar/document.xml
  * xmldb:exist://demo.exist-db.org/xmlrpc/db/foobar/document.xml

## ToDo ##
  * ~~refactor code~~
    * ~~nearly finished~~
  * ~~XmlrpcDownload line 96 ; why a flush on output stream here? might indicate an issue in BlockingIOstream~~
  * ~~add tests regarding binary files in embedded mode~~
  * ~~fix issues with demo server urls~~
    * ~~more tests are always welcome~~
  * ~~add tests for parallel requests~~

## Known Issues ##
  * URL parsing is not yet perfect :-)
    * but good enough right now

## Ideas ##
  * add handling of exist instance name
    * for what purpose? what kind of code will use it? how to use it in embedded mode?
  * add https support
    * xmldbs:exist:// ?