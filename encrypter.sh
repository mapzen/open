#!/usr/bin/env bash

sed -i.orig 's/Logger\.e/\/\/Logger.e/gi' src/main/java/com/mapzen/open/util/SimpleCrypt.java
javac -cp android-all-4.4_r1-robolectric-0.jar src/main/java/com/mapzen/open/util/SimpleCrypt.java
mv src/main/java/com/mapzen/open/util/SimpleCrypt.java.orig src/main/java/com/mapzen/open/util/SimpleCrypt.java

ruby encrypter.rb $1 $2
