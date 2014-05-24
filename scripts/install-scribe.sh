#!/bin/sh
#
# Installs scribe 1.3.6 jar into local maven repo.
#
# Usage:
#   install-scribe-jar.sh

echo "Installing org.scribe:scribe:1.3.6"
mvn install:install-file -DgroupId=org.scribe -DartifactId=scribe \
  -Dversion=1.3.6 -Dpackaging=jar -Dfile=lib/scribe-1.3.6.jar

echo "Done!"
