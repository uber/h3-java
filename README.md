<img align="right" src="https://uber.github.io/img/h3Logo-color.svg" alt="H3 Logo" width="200">

# H3-Java

[![tests](https://github.com/uber/h3-java/workflows/tests/badge.svg)](https://github.com/uber/h3-java/actions)
[![Coverage Status](https://coveralls.io/repos/github/uber/h3-java/badge.svg?branch=master)](https://coveralls.io/github/uber/h3-java?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.uber/h3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.uber/h3)
[![H3 Version](https://img.shields.io/badge/h3-v4.3.0-blue.svg)](https://github.com/uber/h3/releases/tag/v4.3.0)

This library provides Java bindings for the [H3 Core Library](https://github.com/uber/h3). For API reference, please see the [H3 Documentation](https://h3geo.org/).

# Usage

Add it to your pom.xml:

```xml
<dependency>
    <groupId>com.uber</groupId>
    <artifactId>h3</artifactId>
    <version>4.4.0</version>
</dependency>
```

Or, using Gradle:

```gradle
compile("com.uber:h3:4.4.0")
```

Encode a location into a hexagon address:

```java
H3Core h3 = H3Core.newInstance();

double lat = 37.775938728915946;
double lng = -122.41795063018799;
int res = 9;

String hexAddr = h3.latLngToCellAddress(lat, lng, res);
```

Decode a hexagon address into coordinates:

```java
List<LatLng> LatLngs = h3.cellToGeoBoundary(hexAddr);
```

## Supported Operating Systems

H3-Java provides bindings to the H3 library, which is written in C. The built artifact supports the following:

| Operating System | Architectures
| ---------------- | -------------
| Linux            | x64, x86, ARM64, ARMv5, ARMv7, PPC64LE, s390x
| Windows          | x64, x86
| Darwin (Mac OSX) | x64, ARM64
| FreeBSD          | x64
| Android          | ARM, ARM64

You may be able to build H3-Java locally if you need to use an operating system or architecture not listed above.

# Development

Building the library requires a JDK, Gradle, CMake, and a C compiler. To build:

```sh
./gradlew assemble test -Ph3UseDocker=false
```

Additional information on how the build process works is available in the [build process documentaiton](docs/library-build.md).

## Building on FreeBSD

```sh
# To install build dependencies
sudo pkg install openjdk11 cmake bash
# Ensure /usr/local/openjdk11/bin is on your path
```

## Javadocs

To build Javadocs documentation:

```sh
./gradlew javadoc -Ph3UseDocker=false
```

Then open the file `build/docs/javadoc/index.html`.

## Benchmarking

To run benchmarks, either execute them from IntelliJ or run the following from shell:

```sh
./gradlew benchmark -Ph3UseDocker=false
```

## Contributing

Pull requests and Github issues are welcome. Please see our [contributing guide](./CONTRIBUTING.md) for more information.

Before we can merge your changes, you must agree to the [Uber Contributor License Agreement](http://cla-assistant.io/uber/h3-java).

## Legal and Licensing

H3-Java is licensed under the [Apache 2.0 License](./LICENSE).

DGGRID
Copyright (c) 2015 Southern Oregon University
