#!/bin/bash

if [ -z ${PERFORM_RELEASE} ]
  then
    mvn install -P jenkins -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET -Dmaven.test.skip=true
  else
    git reset --hard
    git config --global user.name "Circle CI"
    git config --global user.email "android-support@gmail.com"
    mvn clean release:clean release:prepare --batch-mode -DignoreSnapshots=true -Darguments="-Dmaven.test.skip=true"
    git clone $SIGN_REPO ~/android-config
    scripts/production-values.sh
    mvn release:perform --batch-mode -DignoreSnapshots=true -Darguments="-Dmaven.test.skip=true -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET"
    /usr/lib/jvm/jdk1.7.0/bin/jarsigner -verbose -keystore ~/android-config/mapzen.keystore -storepass "$SIGN_STOREPASS" -keypass "$SIGN_KEYPASS" target/*.apk mapzen_prod
fi
