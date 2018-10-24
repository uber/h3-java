# Instructions for releasing the library

The H3-Java library is published to Maven Central via OSSRH.

You must be a member of the `com.uber` group to release the library via OSSRH. You must have a [signing key](http://central.sonatype.org/pages/working-with-pgp-signatures.html) setup, and you must have your OSSRH username and password in the appropriate [Maven settings file](http://central.sonatype.org/pages/apache-maven.html).

Release builds should only be run on Mac OSX with Docker. This is needed so that the deployed artifact contains all supported operating system/architecture combinations. Before deploying, run `mvn clean package` and check that at least the files `src/main/resources/darwin-x64/libh3-java.dylib` and `src/main/resources/linux-x64/libh3-java.so` exist.

1. Ensure you are on master and that the library is building correctly.
2. Update [CHANGELOG.md](../CHANGELOG.md) to have the correct date and new version number, update [README.md](../README.md) to have the correct version numbers, and commit.
3. `mvn release:prepare` Use the new version number when prompted.
4. `mvn release:perform`
5. If this looks good, release the build in [Sonatype Nexus Manager](https://oss.sonatype.org/).
6. Update `CHANGELOG.md` to have an Unreleased section, and commit. The release is now done and development can resume from this point.
