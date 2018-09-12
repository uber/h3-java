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

# Arguments: [git-remote] [git-ref]
# git-remote - The git remote to pull from. An existing cloned repository
#              will not be deleted if a new remote is specified.
# git-ref    - A specific git ref to build, or "default" to use
#              the H3 version (next argument) to determine the tag.
#
# This script downloads H3, builds H3 and the H3-Java native library for
# 32-bit and 64-bit Windows.
#
# This script expects to be run from the project's base directory (where
# pom.xml is) as part of the Maven build process.

set -ex

GIT_REMOTE=$1
GIT_REVISION=$2

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

H3_SRC_ROOT="$(pwd)"

popd

#
# Now that H3 is downloaded, build H3-Java's native library for this platform.
#

for CONFIGURATION in "x64" "x86"; do
    case "$CONFIGURATION" in
        "x64") GENERATOR="Visual Studio 15 2017 Win64" ;;
        "x86") GENERATOR="Visual Studio 15 2017" ;;
    esac

    mkdir -p "h3-java-build$CONFIGURATION"
    pushd "h3-java-build$CONFIGURATION"

    mkdir -p build
    pushd build

    cmake -G "$GENERATOR" \
        -DBUILD_SHARED_LIBS=OFF \
        -DCMAKE_BUILD_TYPE=Release \
        ../../h3
    cmake --build . --target h3 --config Release
    cmake --build . --target binding-functions --config Release
    H3_BUILD_ROOT="$(pwd)"

    popd #build

    cmake -G "$GENERATOR" \
        -DUSE_NATIVE_JNI=ON \
        -DBUILD_SHARED_LIBS=ON \
        "-DH3_SRC_ROOT=$H3_SRC_ROOT" \
        "-DH3_BUILD_ROOT=$H3_BUILD_ROOT" \
        "-DH3_CORE_LIBRARY_PATH=bin/Release/h3" \
        -DCMAKE_BUILD_TYPE=Release \
        ../../src/main/c/h3-java
    cmake --build . --target h3-java --config Release

    popd #h3-java-build

    cp "h3-java-build$CONFIGURATION/build/binding-functions" .

    mkdir -p "../src/main/resources/windows-$CONFIGURATION"
    cp "h3-java-build$CONFIGURATION/lib/Release/h3-java.dll" "../src/main/resources/windows-$CONFIGURATION/libh3-java.dll"

done

popd
