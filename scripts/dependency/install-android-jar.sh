#!/bin/sh
#
# This script installs the Android SDK jar in your local Maven repository.
#
# Usage:
#   install-android-jar.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.
#  3. You have installed the Android Support (compatibility) libraries from the SDK installer.
#
# Adapted from https://github.com/robolectric/robolectric/blob/master/scripts/install-support-jar.sh

platformPath="android-21"
platformVersion="5.0.1_r2"

jarLocation="$ANDROID_HOME/platforms/$platformPath/android.jar"
if [ ! -f "$jarLocation" ]; then
  echo "$platformPath artifact not found!";
  exit 1;
fi

echo "Installing android:android from $jarLocation"
mvn -q install:install-file -DgroupId=android -DartifactId=android \
  -Dversion="$platformVersion" -Dpackaging=jar -Dfile="$jarLocation"

echo "Done!"
