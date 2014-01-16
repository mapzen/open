#!/bin/sh
mvn install:install-file -Dfile=vtm/libs/annotations.jar -DgroupId=org.oscim -DartifactId=annotations -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=vtm/libs/slf4j-api-1.7.5.jar -DgroupId=org.oscim -DartifactId=slf4j-api -Dversion=1.7.5 -Dpackaging=jar
mvn install:install-file -Dfile=vtm-android/libs/native.jar -DgroupId=org.oscim -DartifactId=native -Dversion=1.0.0-SN‎APSHOT -Dpackaging=jar