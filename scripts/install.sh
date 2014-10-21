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
    mvn release:perform --batch-mode -DignoreSnapshots=true -Darguments="-Dmaven.test.skip=true -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET -Dsign.keystore=~/android-config/mapzen.keystore -Dsign.alias=mapzen_prod -Dsign.storepass=$SIGN_STOREPASS -Dsign.keypass=$SIGN_KEYPASS"
fi
