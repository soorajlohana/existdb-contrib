Kind of playground for code that might be integrated with the eXist database in the future. Not now yet. Let's call it potential eXist-db goodies...

[![](http://www.exist-db.org/logo.jpg)](http://www.exist-db.org/)

### Featured ###

  * The [exist-modules.jar](http://code.google.com/p/existdb-contrib/downloads/list?can=1&q=exist-modules&colspec=Filename+Summary+Uploaded+Size+DownloadCount) of eXist-db 1.4 with all extensions enabled.

  * [eXist-db Custom Protocol Handler](http://code.google.com/p/existdb-contrib/wiki/CustomProtocolHandler)
> > Classes to deal with _xmldb:exist//_ style URL's. With these classes it is possible to  download (get) and upload (put) documents from/to the eXist-db using standard Java classes (java.net.URL).

  * [Another Set of Custom Ant Tasks](http://code.google.com/p/existdb-contrib/wiki/ASOCAT) (ASOCAT)
> > A set of ant tasks for unsigning jar files, compressing / repacking jarfiles using pack200 and retrieving Subversion revision info from a SVN repository. New: fetch and expand ZIP files from URL.

  * [eXist/Restlet Integration](http://code.google.com/p/existdb-contrib/wiki/Restlet)

> The eXist-restlet subproject provides an integration with the Restlet project. It provides a simple way to integrate an embedded version of eXist into a web application.

### SVN ###
The sources can be retrieved from:
  * http://existdb-contrib.googlecode.com/svn/trunk/protocolhandler/
  * http://existdb-contrib.googlecode.com/svn/trunk/asocat/
  * http://existdb-contrib.googlecode.com/svn/trunk/restlet/



### Notes ###
  * Java5 code is required.
  * One needs to install the 'default' eXist examples before running the test suite, using the HTML admin interface.