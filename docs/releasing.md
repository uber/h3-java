# Instructions for releasing the library

The H3-Java library is published to Maven Central via OSSRH.

The release is triggered via GitHub Actions, when a tag of the form `v1.2.3` is pushed, or via workflow dispatch. The release workflow handles all parts of the release.

## Old instructions for manual releasing

You must be a member of the `com.uber` group to release the library via OSSRH. You must have a [signing key](http://central.sonatype.org/pages/working-with-pgp-signatures.html) setup, and you must have your OSSRH username and password in the appropriate [Maven settings file](http://central.sonatype.org/pages/apache-maven.html).

Release builds pull artifacts from Github Actions. This is needed so that the deployed artifact contains all supported operating system/architecture combinations. (In particular, Mac OS artifacts must be built natively.) In order to release, there must be a completed build of the Git commit to release on Github. The build must be less than 30 days old, as artifacts are only kept for that time.

1. Ensure you are on branch `master` and that the library is building correctly in Github Actions.
2. Update [CHANGELOG.md](../CHANGELOG.md) to have the correct date and new version number, update [README.md](../README.md) to have the correct version numbers, and commit.
3. `mvn release:prepare` Use the new version number when prompted.
4. `mvn release:perform`
5. If this looks good, close and release the build in [Sonatype Nexus Manager](https://oss.sonatype.org/).
6. Update `CHANGELOG.md` to have an Unreleased section, and commit. The release is now done and development can resume from this point.

### Troubleshooting

#### Dependencies for `pull-from-github.sh`

* You should install the [Github CLI](https://cli.github.com) and authenticate with it first. You may need to use a personal access token (classic) with workflows scope.
* `jq`

#### gpg: signing failed: Inappropriate ioctl for device

Per [StackOverflow](https://stackoverflow.com/questions/57591432/gpg-signing-failed-inappropriate-ioctl-for-device-on-macos-with-maven), run the following before `mvn release:perform`:

```
export GPG_TTY=$(tty)
```
