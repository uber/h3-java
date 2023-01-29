# Instructions for releasing the library

The H3-Java library is published to Maven Central via OSSRH.

You must be a member of the `com.uber` group to release the library via OSSRH. You must have a [signing key](http://central.sonatype.org/pages/working-with-pgp-signatures.html) setup, and you must have your OSSRH username and password in the appropriate [Maven settings file](http://central.sonatype.org/pages/apache-maven.html).

Release builds should only be run on Mac OSX with Docker. This is needed so that the deployed artifact contains all supported operating system/architecture combinations. Other architectures are built using cross compiler, but Mac OSX is built natively. Before deploying, run `mvn clean package` and check that at least the files `src/main/resources/darwin-x64/libh3-java.dylib` and `src/main/resources/linux-x64/libh3-java.so` exist.

1. Ensure you are on branch `master` and that the library is building correctly.
2. Update [CHANGELOG.md](../CHANGELOG.md) to have the correct date and new version number, update [README.md](../README.md) to have the correct version numbers, and commit.
3. `mvn release:prepare -Dh3.github.artifacts.use=true` Use the new version number when prompted.
4. `mvn release:perform -Dh3.github.artifacts.use=true`
5. If this looks good, close and release the build in [Sonatype Nexus Manager](https://oss.sonatype.org/).
6. Update `CHANGELOG.md` to have an Unreleased section, and commit. The release is now done and development can resume from this point.

## Troubleshooting

### Github artifacts

You should install the [Github CLI](https://cli.github.com) and authenticate with it first. You may need to use a personal access token (classic) with workflows scope.

### gpg: signing failed: Inappropriate ioctl for device

Per [StackOverflow](https://stackoverflow.com/questions/57591432/gpg-signing-failed-inappropriate-ioctl-for-device-on-macos-with-maven), run the following before `mvn release:perform`:

```
export GPG_TTY=$(tty)
```

### docker: Error response from daemon: error while creating mount source path

This has been seen when the source path is not shared from the host in the Docker settings. Even if the source path appears to have been shared, if the source path is a symbolic link, you may need to reshare it from Docker Preferences.

### fatal error: jni.h: No such file or directory

This can occur when `JAVA_HOME` is not set.
