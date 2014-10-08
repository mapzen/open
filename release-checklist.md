Mapzen Android Release Checklist
================================

Run the following commands to create the release:

```bash
$ mvn clean release:clean release:prepare -DignoreSnapshots=true -DdryRun=true
$ mvn clean release:clean release:prepare -DignoreSnapshots=true
$ mvn release:perform -DignoreSnapshots=true
```

* Verify new commits have been pushed to GitHub.
* Verify Circle CI build has completed successfully.
* Verify the new release is available on Amazon S3.
