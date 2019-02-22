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

# Arguments: [build-root]
# build-root - Location to build the library.
#
# Builds H3 and H3-Java in the given directory. This is intended to be
# called from build-h3.sh as part of the cross compilation process.

set -ex

BUILD_ROOT=$1

cd $BUILD_ROOT

mkdir -p build
pushd build

cmake -DBUILD_SHARED_LIBS=OFF \
    -DCMAKE_C_STANDARD_REQUIRED=ON \
    -DCMAKE_C_STANDARD=99 \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    -DCMAKE_BUILD_TYPE=Release \
    ../../h3
make h3
H3_BUILD_ROOT="$(pwd)"

popd

cmake -DBUILD_SHARED_LIBS=ON \
    "-DH3_BUILD_ROOT=$H3_BUILD_ROOT" \
    -DCMAKE_BUILD_TYPE=Release \
    /work/src/main/c/h3-java
make h3-java
