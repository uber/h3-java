# H3-Java Developer Documentation

This directory contains documentation for building and integrating H3-Java.

## Troubleshooting

### docker: Error response from daemon: error while creating mount source path

This has been seen when the source path is not shared from the host in the Docker settings. Even if the source path appears to have been shared, if the source path is a symbolic link, you may need to reshare it from Docker Preferences.

### fatal error: jni.h: No such file or directory

This can occur when `JAVA_HOME` is not set.

