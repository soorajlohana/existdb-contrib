#!/bin/sh
java -jar -Duser.home=. -Djava.util.logging.config.file=log.conf -jar dist/exist-restlet.jar $*
