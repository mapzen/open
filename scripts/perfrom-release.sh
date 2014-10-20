#!/bin/bash

if [ -z ${CIRCLE_TOKEN} ]
  then
    echo "[ERROR] Please set CIRCLE_TOKEN environment variable and rerun."
    exit 1
fi

trigger_build_url=https://circleci.com/api/v1/project/mapzen/mapzen-android-demo/tree/master?circle-token=${CIRCLE_TOKEN}

post_data=$(cat <<EOF
{
  "build_parameters": {
    "PERFORM_RELEASE": "true"
  }
}
EOF)

curl \
--header "Accept: application/json" \
--header "Content-Type: application/json" \
--data "${post_data}" \
--request POST ${trigger_build_url}

echo
