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
# This script downloads H3, builds H3, and builds the H3-Java native library.
#
# This script expects to be run from the project's base directory (where
# pom.xml is) as part of the Maven build process.

set -e

GIT_REMOTE=$1
GIT_REVISION=$2

echo Downloading H3 from "$GIT_REMOTE"

mkdir -p target
pushd target

if [ ! -d "h3" ]; then
    git clone "$GIT_REMOTE" h3
fi

pushd h3
git pull origin master --tags

echo Using revision "$GIT_REVISION"
git checkout "$GIT_REVISION"

H3_ROOT=`pwd`

popd

#
# Now that H3 is downloaded, build H3-Java's native library and H3 along with
# it.
#

mkdir -p h3-java-build
pushd h3-java-build

cmake -DBUILD_SHARED_LIBS=ON "-DH3_ROOT=$H3_ROOT" -DCMAKE_BUILD_TYPE=Release ../../src/main/c/h3-java
make h3-java binding-functions

popd
popd

#
# Copy the built artifact into the source tree so it can be included in the
# built JAR.
#

cp target/h3-java-build/lib/libh3* src/main/resources/
