#!/usr/bin/env bash

wget http://central.maven.org/maven2/org/robolectric/android-all/4.4_r1-robolectric-0/android-all-4.4_r1-robolectric-0.jar
OSM_AUTH_KEY_ENCODED=`bash encrypter.sh $ENCRYPTER_PASSWORD $OSM_AUTH_KEY`
OSM_AUTH_SECRET_ENCODED=`bash encrypter.sh $ENCRYPTER_PASSWORD $OSM_AUTH_SECRET`
mvn install -P jenkins -Dmixpanel.token=$MIXPANEL_TOKEN -Dosm_oauth.key=$OSM_AUTH_KEY_ENCODED -Dosm_oauth.secret=$OSM_AUTH_SECRET_ENCODED
