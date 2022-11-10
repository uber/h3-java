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

# Arguments: [build-root] [upgrade-cmake]
# build-root    - Location to build the library.
# upgrade-cmake - Whether to download and install a new version of CMake
# cmake-root    - Where to download and install the new version of CMake
#
# Builds H3 and H3-Java in the given directory. This is intended to be
# called from build-h3.sh as part of the cross compilation process.

set -ex

BUILD_ROOT=$1
UPGRADE_CMAKE=$2
CMAKE_ROOT=$3

if $UPGRADE_CMAKE; then
    pushd "$CMAKE_ROOT"
    if ! [ -e cmake-3.23.2-linux-x86_64.sh ]; then
        wget -nv https://github.com/Kitware/CMake/releases/download/v3.23.2/cmake-3.23.2-linux-x86_64.sh
    fi
    echo "5cca63af386e5bd0bde67c87ffac915865abd7dcc48073528f58645abda8f695  cmake-3.23.2-linux-x86_64.sh" > cmake-3.23.2-SHA-256.txt
    sha256sum -c cmake-3.23.2-SHA-256.txt
    if ! [ -e ./bin/cmake ]; then
        chmod a+x cmake-3.23.2-linux-x86_64.sh
        ./cmake-3.23.2-linux-x86_64.sh --skip-license
    fi
    export PATH=$(pwd)/bin:$PATH
    cmake --version
    popd
fi

cd "$BUILD_ROOT"

mkdir -p build
pushd build

cmake -DBUILD_SHARED_LIBS=OFF \
    -DCMAKE_C_STANDARD_REQUIRED=ON \
    -DCMAKE_C_STANDARD=99 \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=lib \
    ../../h3
make h3
H3_BUILD_ROOT="$(pwd)"

popd

cmake -DBUILD_SHARED_LIBS=ON \
    "-DH3_BUILD_ROOT=$H3_BUILD_ROOT" \
    -DCMAKE_BUILD_TYPE=Release \
    /work/src/main/c/h3-java
make h3-java
