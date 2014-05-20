#!/bin/sh
#
# This script installs all local dependencies required to build mapzen-android-demo.
#
# Usage:
#   install-dependencies.sh

for file in scripts/*.sh; do bash $file; done
