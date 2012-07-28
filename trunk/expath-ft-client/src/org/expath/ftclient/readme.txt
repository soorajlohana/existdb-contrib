The File Transfer Client XQuery extension module is for dealing with remote resources.

Its architecture is separated by protocols (FTP and SFTP, for now), so that one can use only the protocol needed. On the other hand, the module has two layers, one that is general purpose, to be used in any Java application, and the other is an eXist wrapper.

Along with developing this module, and EXPath specification was developed, too, which can be found at [2].

eXist user should use this module as follows:
1. Put the jars from archive in $eXist-home/lib/extensions.
2. Add <module uri="http://expath.org/ns/ft-client" class="org.expath.exist.ftclient.ExistExpathFTClientModule" /> to conf.xml
3. Restart eXist.


[1] http://code.google.com/p/existdb-contrib/downloads/list
[2] http://extxsltforms.sourceforge.net/specs/expath-ft-client/expath-ft-client.html