# Change Log
All notable changes to this project will be documented in this file.
This project adheres to a [versioning policy](./docs/versioning.md).

The public API of this library consists of the public functions declared in
file [H3Core.java](./src/main/java/com/uber/h3core/H3Core.java), and support
for the Linux x64 and Darwin x64 platforms.

## [Unreleased]
## Fixed
- Fixes local build script to support Apple M1

## [3.7.1] - 2021-08-18
## Fixed
- Fixes for non-English locales (#80)

## Changed
- Updated the core library to v3.7.2. (#81)

## [3.7.0] - 2020-12-03
## Added
- Area and haversine distance functions (#70)
    - `cellArea`
    - `pointDist`
    - `exactEdgeLength`
## Changed
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
