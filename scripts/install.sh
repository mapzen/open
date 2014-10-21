#!/bin/bash

if [ -z ${PERFORM_RELEASE} ]
  then
    mvn install -P jenkins -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET -Dmaven.test.skip=true
  else
    mvn install -P release -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET -Dmaven.test.skip=true
    ./scripts/production-values.sh
    git clone $SIGN_REPO ~/android-config
    /usr/lib/jvm/jdk1.7.0/bin/jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/android-config/mapzen.keystore -storepass "$SIGN_STOREPASS" -keypass "$SIGN_KEYPASS" target/*.ap_ mapzen_prod
    mvn exec:exec -P zipalign
fi
