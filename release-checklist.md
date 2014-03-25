Mapzen Android Release Checklist
================================

Run the following commands to create the release:

```bash
$ mvn release:prepare -DignoreSnapshots=true -Darguments="-P release"
$ mvn release:perform -DignoreSnapshots=true -Darguments="-P release"
```

* Verify the new release is available on Amazon S3.
* Verify the new release is available on Sonatype Nexus (internal).
