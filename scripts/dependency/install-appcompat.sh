#!/bin/sh
#
# This script installs the v7 AppCompat AAR in your local Maven repository.
#
# Usage:
#   install-appcompat.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.
#  3. You have installed the Android Support (compatibility) libraries from the SDK installer.

jarLocation="$ANDROID_HOME/extras/android/m2repository/com/android/support/appcompat-v7/22.2.0/appcompat-v7-22.2.0.aar"
if [ ! -f "$jarLocation" ]; then
  echo "appcompat-v7 artifact not found!";
  exit 1;
fi

pomLocation="$ANDROID_HOME/extras/android/m2repository/com/android/support/appcompat-v7/22.2.0/appcompat-v7-22.2.0.pom"
if [ ! -f "$pomLocation" ]; then
  echo "appcompat-v7 pom not found!";
  exit 1;
fi

echo "Installing com.support.android:appcompat-v7 from $jarLocation"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=appcompat-v7 \
  -Dversion=22.2.0 -Dpackaging=aar -Dfile="$jarLocation" -DpomFile="$pomLocation"

echo "Done!"
