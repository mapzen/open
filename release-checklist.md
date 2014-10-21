Open by Mapzen Release Checklist
================================

1. Perform a dry run of the release to verify everything works.

        $ mvn clean release:clean release:prepare -DignoreSnapshots=true -DdryRun=true

2. Prepare release by creating tag and pushing release commits to GitHub.

        $ mvn clean release:clean release:prepare -DignoreSnapshots=true

3. Perform release build on Circle CI.

        $ ./scripts/perform-release.sh [TAG]

4. Download production APK from the "Releases" section on http://android.mapzen.com/.
