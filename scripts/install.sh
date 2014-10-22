#!/usr/bin/env bash

wget http://central.maven.org/maven2/org/robolectric/android-all/4.4_r1-robolectric-0/android-all-4.4_r1-robolectric-0.jar
OSM_AUTH_KEY_ENCODED=`bash scripts/encrypter.sh $ENCRYPTER_PASSWORD $OSM_AUTH_KEY`
OSM_AUTH_SECRET_ENCODED=`bash scripts/encrypter.sh $ENCRYPTER_PASSWORD $OSM_AUTH_SECRET`

if [ -z ${PERFORM_RELEASE} ]
  then
    mvn install -P jenkins -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY_ENCODED -Dosm_oauth.secret=$OSM_AUTH_SECRET_ENCODED
  else
    ./scripts/production-values.sh
    mvn install -P release -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY_ENCODED -Dosm_oauth.secret=$OSM_AUTH_SECRET_ENCODED
    git clone $SIGN_REPO ~/android-config
    zip -d target/*.apk "META-INF/*"
    /usr/lib/jvm/jdk1.7.0/bin/jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/android-config/mapzen.keystore -storepass "$SIGN_STOREPASS" -keypass "$SIGN_KEYPASS" target/*.apk mapzen_prod
    mvn exec:exec -P zipalign
    rm target/*-unsigned.apk
fi
