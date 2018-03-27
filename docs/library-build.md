# Building the native H3-Java library

The H3 library is implemented in C, so the Java bindings must use a version of the native H3 library built for your system. Artifacts are provided for the Linux x64 architecture, and other operating systems and architectures may be supported in the future.

H3-Java uses JNI to interface with the native H3 library. Because of requirements for how JNI functions are called, additional functions must be included in the native H3 library.

For most users, running `mvn package` is sufficient to build the library. Read on if you want to modify the build process in some way.

## Build process

When building H3-Java, the build process goes through the following steps:

1. Compiles the Java sources and generates a header file for the JNI-specific native functions.
2. Downloads the sources for the H3 library. The version number set in the H3-Java project is used.
3. Builds the native H3-Java library, which includes the H3 library and the JNI-specific functions.
4. Copies this library into the resources of the built JAR.

## Making changes

### Building a specific version of the library

You can use the Maven properties `h3.git.remote` and `h3.git.reference` to control where the H3 library is cloned from, and which revision is used respectively.

If you need to modify the build of the native library, modify the file [src/main/c/h3-java/build-h3.sh](../src/main/c/h3-java/build-h3.sh) and [src/main/c/h3-java/CMakeLists.txt](src/main/c/h3-java/CMakeLists.txt) as needed.

### Building for different platforms

Specific instructions for cross-compiling are coming soon.

### Shading/relocating the library

Relocating the H3 classes may not work, because some class names are hard coded in the JNI code.
