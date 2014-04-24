#!/bin/sh
#
# This script installs the VTM project and native dependencies.
#
# Usage:
#   install-vtm.sh

git clone --recursive https://github.com/mapzen/vtm.git
cd vtm/vtm-android && ../gradlew clean install
