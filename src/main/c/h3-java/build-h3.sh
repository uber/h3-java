#!/usr/bin/env bash
#
# Copyright 2018 Uber Technologies, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Arguments: [git-remote] [git-ref] [use-docker] [remove-images]
# git-remote    - The git remote to pull from. An existing cloned repository
#                 will not be deleted if a new remote is specified.
# git-ref       - Specific git ref of H3 to build.
# use-docker    - "true" to perform cross compilation via Docker, "false" to
#                 skip that step.
# system-prune  - If use-docker is true and this argument is true, Docker
#                 system prune will be run after each step
#                 (i.e. for disk space constrained environments like CI)
# dockcross-tag - Tag name for dockcross
#
# This script downloads H3, builds H3 and the H3-Java native library, and
# cross compiles via Docker.
#
# This script expects to be run from the project's base directory (where
# pom.xml is) as part of the Maven build process.

set -ex

GIT_REMOTE=$1
GIT_REVISION=$2
USE_DOCKER=$3
SYSTEM_PRUNE=$4
DOCKCROSS_TAG=$5

echo Downloading H3 from "$GIT_REMOTE"

mkdir -p target
pushd target

if [ ! -d "h3" ]; then
    git clone "$GIT_REMOTE" h3
fi

pushd h3
git fetch origin --tags

echo Using revision "$GIT_REVISION"
git checkout "$GIT_REVISION"

popd # h3

#
# Now that H3 is downloaded, build H3-Java's native library for this platform.
#

mkdir -p h3-java-build
pushd h3-java-build

mkdir -p build
pushd build

cmake -DBUILD_SHARED_LIBS=OFF \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    -DCMAKE_BUILD_TYPE=Release \
    ../../h3
cmake --build . --target h3 --config Release
cmake --build . --target binding-functions --config Release
H3_BUILD_ROOT="$(pwd)"

popd # build

cmake -DUSE_NATIVE_JNI=ON \
    -DBUILD_SHARED_LIBS=ON \
    "-DH3_BUILD_ROOT=$H3_BUILD_ROOT" \
    -DCMAKE_BUILD_TYPE=Release \
    ../../src/main/c/h3-java
cmake --build . --target h3-java --config Release

popd # h3-java-build

cp h3-java-build/build/binding-functions .

popd # target

# Copy the built artifact for this platform.
case "$(uname -sm)" in
    "Linux x86_64")  LIBRARY_DIR=linux-x64 ;;
    "Linux i386")    LIBRARY_DIR=linux-x86 ;;
    "Linux i486")    LIBRARY_DIR=linux-x86 ;;
    "Linux i586")    LIBRARY_DIR=linux-x86 ;;
    "Linux i686")    LIBRARY_DIR=linux-x86 ;;
    "Linux i786")    LIBRARY_DIR=linux-x86 ;;
    "Linux i886")    LIBRARY_DIR=linux-x86 ;;
    "Darwin x86_64") LIBRARY_DIR=darwin-x64 ;;
    "Darwin arm64")  LIBRARY_DIR=darwin-arm64 ;;
    "FreeBSD amd64") LIBRARY_DIR=freebsd-x64 ;;
    # TODO: Detect others
    *)               LIBRARY_DIR="" ;;
esac

mkdir -p src/main/resources/$LIBRARY_DIR
cp target/h3-java-build/lib/libh3-java* src/main/resources/$LIBRARY_DIR

# Cross compile from Mac x64 to Mac arm64
if [ "$(uname -sm)" == "Darwin x86_64" ]; then
    pushd target

    mkdir -p h3-java-build-mac-arm64
    pushd h3-java-build-mac-arm64

    mkdir -p build
    pushd build

    cmake -DBUILD_SHARED_LIBS=OFF \
        -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_OSX_ARCHITECTURES="arm64" \
        ../../h3
    cmake --build . --target h3 --config Release
    H3_BUILD_ROOT="$(pwd)"

    popd # build

    cmake -DUSE_NATIVE_JNI=ON \
        -DBUILD_SHARED_LIBS=ON \
        "-DH3_BUILD_ROOT=$H3_BUILD_ROOT" \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_OSX_ARCHITECTURES="arm64" \
        ../../src/main/c/h3-java
    cmake --build . --target h3-java --config Release

    popd # h3-java-build

    popd # target

    mkdir -p src/main/resources/darwin-arm64
    cp target/h3-java-build-mac-arm64/lib/libh3-java* src/main/resources/darwin-arm64
fi

#
# Now that H3 is downloaded, build H3-Java's native library for other platforms.
#

if ! $USE_DOCKER; then
    echo Docker disabled, skipping cross compilation.
    exit 0
fi
if ! command -v docker; then
    echo Docker not found, skipping cross compilation.
    exit 0
fi

# Needed for older versions of dockcross
UPGRADE_CMAKE=true
CMAKE_ROOT=target/cmake-3.23.2-linux-x86_64
mkdir -p $CMAKE_ROOT

# linux-armv6 excluded because of build failure
# linux-mips excluded due to manifest error
for image in android-arm android-arm64 linux-arm64 linux-armv5 linux-armv7 linux-s390x \
    linux-ppc64le linux-x64 linux-x86 windows-static-x64 windows-static-x86; do

    # Setup for using dockcross
    BUILD_ROOT=target/h3-java-build-$image
    mkdir -p $BUILD_ROOT
    docker pull dockcross/$image:$DOCKCROSS_TAG
    docker run --rm dockcross/$image:$DOCKCROSS_TAG > $BUILD_ROOT/dockcross
    chmod +x $BUILD_ROOT/dockcross

    # Perform the actual build inside Docker
    $BUILD_ROOT/dockcross --args "-v $JAVA_HOME:/java" src/main/c/h3-java/build-h3-docker.sh "$BUILD_ROOT" "$UPGRADE_CMAKE" "$CMAKE_ROOT"

    # Copy the built artifact into the source tree so it can be included in the
    # built JAR.
    OUTPUT_ROOT=src/main/resources/$image
    if [ "$image" -eq "windows-static-x64" ]; then
        OUTPUT_ROOT=src/main/resources/windows-x64
    fi
    if [ "$image" -eq "windows-static-x86" ]; then
        OUTPUT_ROOT=src/main/resources/windows-x86
    fi
    mkdir -p $OUTPUT_ROOT
    if [ -e $BUILD_ROOT/lib/libh3-java.so ]; then cp $BUILD_ROOT/lib/libh3-java.so $OUTPUT_ROOT ; fi
    if [ -e $BUILD_ROOT/lib/libh3-java.dylib ]; then cp $BUILD_ROOT/lib/libh3-java.dylib $OUTPUT_ROOT ; fi
    if [ -e $BUILD_ROOT/lib/libh3-java.dll ]; then cp $BUILD_ROOT/lib/libh3-java.dll $OUTPUT_ROOT ; fi

    if $SYSTEM_PRUNE; then
        docker system prune --force --all
        docker system df
        rm $BUILD_ROOT/dockcross
    fi
    echo Current disk usage:
    df -h
done
