All notable changes to this project will be documented in this file.
This project adheres to a [versioning policy](./docs/versioning.md).

The public API of this library consists of the public functions declared in
file [H3Core.java](./src/main/java/com/uber/h3core/H3Core.java), and support
for the Linux x64 and Darwin x64 platforms.

## Unreleased Changes
### Changed
- Added option to build and publish as an Android module into h3-android. (#184)

### Fixed
- Removed duplicate native code resources from the built artifacts. (#186)

## [4.3.1] - 2025-08-27
### Changed
- Upgraded build process for Android 16kb support. (#181)

### Fixed
- Fixed the build process crashing at the end of the release. (#181)

## [4.3.0] - 2025-08-21
### Added
- `polygonToCellsExperimental` functions from H3 v4.2.0. (#163)
- `gridRing` function from H3 v4.3.0. (#169)

### Fixed
- Corrected order of `polygonToCellsExperimental` arguments. (#166)
- Fixed build on ARM Linux. (#162)

### Changed
- Converted build system to Gradle and automated deploys. Added separate build steps for mac OS on M1 (ARM) and x64. (#167, #168)
- Upgraded the core library to v4.2.1. (#165)

## [4.1.2] - 2024-11-01
Note: This release is not available in Maven Central.

### Fixed
- Fixed a memory leak in `polygonToCells` and optimize JNI calls. (#150)
- Use `Files.createTempFile` so temporary file permissions are more restrictive. (#141)
- Fixed a potential segfault in `cellsToMultiPolygon` on error. (#129)

### Changed
- Optimize JNI calls. (#154)
- Added JNI config and tests for native image. (#153, #155)
- Bumped dockcross versions (#151, #152)
- Upgrade Guava test dependency. (#146)
- Test Java 22. (#144)

## [4.1.1] - 2023-02-03
The changelog for this release is the same as v4.1.0. The release was run again due to an issue with the release process.

## [4.1.0] - 2023-01-19
### Added
- `cellToChildPos` and `childPosToCell` functions. (#121)
- Made the `cellToChildrenSize` function public. (#121)

### Changed
- Upgraded the core library to v4.1.0. (#121)
- Release artifacts are now built in Github Actions. (#124)

## [4.0.2] - 2022-09-21
### Changed
- Upgraded the core library to v4.0.1. (#113)

## [4.0.1] - 2022-09-14
### Fixed
- Fixed the path to Windows resources. (#109)

## [4.0.0] - 2022-08-23
### Breaking Changes
- Upgraded the core library to v4.0.0. (#104, #103, #102, #91)

### Added
- `H3CoreV3` for users who wish to temporarily use old function names. (#91)
- Vertex mode API. (#91)

### Changed
- Required version of glibc on Linux is 2.26. (#98)

### Removed
- Removed support for Linux MIPS and MIPSEL (#98, #92)

## [4.0.0-rc4] - 2022-08-17
### Breaking Changes
- Upgraded the core library to v4.0.0-rc5. (#104)
- `exactEdgeLength` function renamed to `edgeLength`. (#104)

## [4.0.0-rc3] - 2022-07-26
### Breaking Changes
- Upgraded the core library to v4.0.0-rc4. (#102)
- `distance` function renamed to `greatCircleDistance`. (#102)

## [4.0.0-rc2] - 2022-06-09
### Changed
- Required version of glibc on Linux is 2.26. (#98)

### Removed
- Removed support for Linux MIPS (#98)

## [4.0.0-rc1] - 2022-03-29
### Breaking Changes
- Changed the API of `H3Core` to align it with the core library. (#91)

### Added
- `H3CoreV3` for users who wish to temporarily use old function names. (#91)
- Vertex mode API. (#91)

### Changed
- Upgraded the core library to v4.0.0-rc2. (#91)

### Removed
- Removed support for Linux MIPSEL (#92)

## [3.7.2] - 2022-02-07
### Added
- Added Apple M1 build (#89)
### Fixed
- Fixes local build script to support Apple M1 (#86)

## [3.7.1] - 2021-08-18
### Fixed
- Fixes for non-English locales (#80)

### Changed
- Updated the core library to v3.7.2. (#81)

## [3.7.0] - 2020-12-03
### Added
- Area and haversine distance functions (#70)
    - `cellArea`
    - `pointDist`
    - `exactEdgeLength`
### Changed
- Updated the core library to v3.7.1. (#70)

## [3.6.4] - 2020-06-29
### Changed
- Updated the core library to v3.6.4. (#64)
- Updated the core library to v3.6.3. (#61)

## [3.6.3] - 2020-01-10
### Changed
- Avoid reloading the native library once loaded (#58)

## [3.6.2] - 2019-12-16
### Changed
- Updated the core library to v3.6.2, fixing some regressions in `polyfill`. (#56)

## [3.6.1] - 2019-11-21
### Changed
- Updated the core library to v3.6.1. (#53)

## [3.6.0] - 2019-08-19
### Added
- `getPentagonIndexes` and `h3ToCenterChild` functions. (#49)
### Changed
- Updated the core library to v3.6.0. (#49)
- Native implementations of `getRes0Indexes` and `getPentagonIndexes` changed to throw `OutOfMemoryError` if the output array size is too small. (#49)

## [3.5.0] - 2019-07-22
### Changed
- Updated the core library to v3.5.0. (#47)

## [3.4.1] - 2019-05-03
### Changed
- Updated the core library to v3.4.3. (#44)

## [3.4.0] - 2019-02-22
### Added
- `getRes0Indexes` function. (#38)
### Changed
- Updated the core library to v3.4.2. (#38)

## [3.3.0] - 2019-02-13
### Added
- `h3Line` function. (#36)
### Changed
- Updated the core library to v3.3.0. (#36)

## [3.2.0] - 2018-10-16
### Added
- Support for building on Windows. (#26)
- `experimentalH3ToLocalIj` and `experimentalLocalIjToH3` functions. (#32)
### Changed
- Updated the core library to v3.2.0. (#32)
### Fixed
- Don't require a C++ compiler. (#30)

## [3.1.0] - 2018-10-04
### Added
- `h3Distance` function. (#21)
- `newInstance` override that accepts specific operating system and architecture values. (#24)
### Changed
- Updated the core library to v3.1.0. (#21)
- Updated the core library to v3.1.1. (#28)

## [3.0.4] - 2018-07-25
### Changed
- Updated the core library to v3.0.8, fixing a possible segfault. (#19)

## [3.0.3] - 2018-05-22
### Breaking Changes
- Replaced Vector2D with GeoCoord.
- Removed the geoJsonOrder parameter from polyfill functions.

## [3.0.2] - 2018-05-08
### Fixed
- Fixed memory management in polyfill with multiple holes.
- Fixed build on Linux without Docker.
### Changed
- Improve detection of x86 architecture.

## [3.0.1] - 2018-04-30
### Added
- Added release settings.
- Added script for cross compiling using dockcross.
### Changed
- Updated the core library to v3.0.5.
- Changed the native library loader to detect more operating systems.

## [3.0.0] - 2018-03-27
### Added
- First public release.
