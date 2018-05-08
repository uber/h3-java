# Change Log
All notable changes to this project will be documented in this file.
This project adheres to a [versioning policy](./docs/versioning.md).

The public API of this library consists of the public functions declared in
file [H3Core.java](./src/main/java/com/uber/h3core/H3Core.java), and support
for the Linux x64 and Darwin x64 platforms.

## [Unreleased]

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
