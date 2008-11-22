#!/bin/sh
base=`dirname $0`
java -cp $base/dist/exist-restlet.jar:$base/dist/lib/com.noelios.restlet.jar:$base/dist/lib/org.restlet.jar org.exist.restlet.tools.Import $*
