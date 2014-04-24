#!/bin/sh
#
# This script installs the latest snapshot version of Robolectric from the Mapzen fork.
#
# Usage:
#   install-robolectric.sh

git clone https://github.com/mapzen/robolectric.git
cd robolectric
./scripts/install-maps-jar.sh
mvn clean install
