# Instructions for releasing the library

The H3-Java library is published to Maven Central via OSSRH.

You must be a member of the `com.uber` group to release the library via OSSRH. You must have a [signing key](http://central.sonatype.org/pages/working-with-pgp-signatures.html) setup, and you must have your OSSRH username and password in the appropriate [Maven settings file](http://central.sonatype.org/pages/apache-maven.html).

Release builds should only be run on Mac OSX with Docker. This is needed so that the deployed artifact contains all supported operating system/architecture combinations. Before deploying, run `mvn clean package` and check that at least the files `src/main/resources/darwin-x64/libh3-java.dylib` and `src/main/resources/linux-x64/libh3-java.so` exist.

1. Ensure you are on master and that the library is building correctly.
2. Change the version number of the library to remove "-SNAPSHOT". Update [CHANGELOG.md](../CHANGELOG.md) to have the correct date for this version.
3. Commit. Make sure this revision builds in CI.
4. `mvn clean deploy`
5. If this looks good, release the build inn Sonatype Nexus Manager.
6. Update the version number to be higher and include "-SNAPSHOT'. Update `CHANGELOG.md` to have an Unreleased section.
7. Commit. The release is now done and development can resume from this point.
