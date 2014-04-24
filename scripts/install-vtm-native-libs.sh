#!/bin/sh
#
# This script installs the VTM project and native dependencies.
#
# Usage:
#   install-vtm-native-libs.sh

git clone https://github.com/mapzen/vtm-native-libs.git
cd vtm-native-libs && ./install-dependencies.sh
