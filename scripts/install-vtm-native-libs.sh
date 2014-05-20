#!/bin/sh
#
# This script installs native dependencies for VTM to the local Maven repo.
#
# Usage:
#   install-vtm-native-libs.sh

git clone https://github.com/mapzen/vtm-native-libs.git
cd vtm-native-libs && ./install-dependencies.sh
cd .. && rm -rf vtm-native-libs
