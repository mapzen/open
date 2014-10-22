#!/bin/bash

if [ -z ${PERFORM_RELEASE} ]
  then
    mvn install -P jenkins -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET
  else
    ./scripts/production-values.sh
    mvn install -P release -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY -Dosm_oauth.secret=$OSM_AUTH_SECRET
    git clone $SIGN_REPO ~/android-config
    zip -d target/*.apk "META-INF/*"
    /usr/lib/jvm/jdk1.7.0/bin/jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/android-config/mapzen.keystore -storepass "$SIGN_STOREPASS" -keypass "$SIGN_KEYPASS" target/*.apk mapzen_prod
    mvn exec:exec -P zipalign
    rm target/*-unsigned.apk
fi
