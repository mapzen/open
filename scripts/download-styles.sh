#!/bin/sh

host=http://vector-styles.mapzen.com

wget -q $host/manifest
for asset in `cat manifest` 
do
  mkdir -p $(dirname $asset)
  wget -q $host/$asset -O $asset
done

rm manifest
