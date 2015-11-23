#!/bin/sh
#
# This script installs the Android Support v4 jar in your local Maven repository.
#
# Usage:
#   install-support-jar_app.sh

version="22.2.1"
artifact="support_r$version"
file="$artifact.zip"

echo "Downloading $file"
wget "https://dl-ssl.google.com/android/repository/$file"
unzip "$file"

echo "Installing $artifact"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=support-v4 \
  -Dversion="$version" -Dpackaging=jar -Dfile=support/v4/android-support-v4.jar

echo "Deleting $file"
rm "$file"

echo "Deleting support/"
rm -rf support

echo "Done!"
