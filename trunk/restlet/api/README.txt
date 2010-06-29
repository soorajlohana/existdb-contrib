To build this project you must first have an embedded distribution of
eXist.  You can build this via the 'embedded' project in eXist.  

Once you have that, you can then import the necessary jar files
by the ant target 'import-embedded'.  You'll need to set the 'embedded.dir'
property to the directory of the eXist embedded project for this 
target to work properly.

Afterwards, everything should build normally via the 'jar' ant target.
