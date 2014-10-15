#!/bin/sh
#
# This script installs native dependencies for VTM to the local Maven repo.
#
# Usage:
#   install-vtm-native-libs.sh

git clone git@github.com:mapzen/vtm-ext-libs.git

cd vtm-ext-libs 

#!/bin/bash

mvn install:install-file -Dfile=vtm-android/armeabi/libvtm-jni.so -DgroupId=org.oscim -DartifactId=vtm-jni -Dversion=0.5.9-SNAPSHOT -Dpackaging=so -Dclassifier=armeabi
mvn install:install-file -Dfile=vtm-android/armeabi-v7a/libvtm-jni.so -DgroupId=org.oscim -DartifactId=vtm-jni -Dversion=0.5.9-SNAPSHOT -Dpackaging=so -Dclassifier=armeabi-v7a
mvn install:install-file -Dfile=vtm-android/x86/libvtm-jni.so -DgroupId=org.oscim -DartifactId=vtm-jni -Dversion=0.5.9-SNAPSHOT -Dpackaging=so -Dclassifier=x86

cd .. && rm -rf vtm-ext-libs
