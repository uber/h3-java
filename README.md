# H3-Java

[![Build Status](https://travis-ci.org/uber/h3-java.svg?branch=master)](https://travis-ci.org/uber/h3-java)
[![Coverage Status](https://coveralls.io/repos/github/uber/h3-java/badge.svg?branch=master)](https://coveralls.io/github/uber/h3-java?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

This library provides Java bindings for the [H3 Core Library](https://github.com/uber/h3). For API reference, please see the [H3 Documentation](https://uber.github.io/h3/).

# Usage

Add it to your pom.xml:

```
<dependency>
    <groupId>com.uber</groupId>
    <artifactId>h3</artifactId>
    <version>3.0.0</version>
</dependency>
```

Or, using Gradle:

```
compile("com.uber:h3:3.0.0")
```

Encode a location into a hexagon address:

```
H3Core h3 = H3Core.newInstance();

double lat = 37.775938728915946;
double lng = -122.41795063018799;
int res = 9;

String hexAddr = h3.geoToH3Address(lat, lng, res);
```

Decode a hexagon address into coordinates:

```
Vector2D[] geoCoords = h3.h3ToGeoBoundary(hexAddr);
```

# Development

Building the library requires a JDK, Maven, CMake, and a C compiler. To install to your local Maven cache, run:

```
mvn install
```

To build the library, run:

```
mvn package
```

Additional information on how the build process works is available in the [build process documentaiton](docs/library-build.md).

## Javadocs

To build Javadocs documentation:

```
mvn site
```

Then open the file `target/site/apidocs/index.html`.

## Benchmarking

To run benchmarks, either execute them from IntelliJ or run the following from shell: (Replace the class name as needed)

```
mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath com.uber.h3core.benchmarking.H3CoreBenchmark" -Dexec.classpathScope="test"
```

## Contributing

Pull requests and Github issues are welcome. Please include tests that show the bug is fixed or feature works as intended. Please open issues to discuss large features or changes which would break compatibility, before submitting pull requests.

Before we can merge your changes, you must agree to the [Uber Contributor License Agreement](http://t.uber.com/cla).

## Legal and Licensing

H3-Java is licensed under the [Apache 2.0 License](./LICENSE).

DGGRID
Copyright (c) 2015 Southern Oregon University
