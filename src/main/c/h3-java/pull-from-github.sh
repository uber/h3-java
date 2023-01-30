#!/usr/bin/env bash
#
# Copyright 2023 Uber Technologies, Inc.
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

# Retrieves built artifacts from Github. The purpose of this script is
# to enable deployment from any system, regardless of whether it can
# run the particular cross compiling steps needed to build native shared
# objects. Some of the native shared objects require particular Docker
# setups, or indeed running on Mac OS, which is difficult to maintain.
#
# You must install the Github CLI and authenticate with a personal access
# token (classic) with workflows scope to use this. https://cli.github.com/
# This script also requires `jq`.
#
# This script expects to be run from the project's base directory (where
# pom.xml is) as part of the Maven build process.

set -eox pipefail

GITHUB_ARTIFACTS_RUN=$1

EXTRACT_TO=src/main/resources

if [ -z "$GITHUB_ARTIFACTS_RUN" ]; then
    mkdir -p target
    pushd target

    ARTIFACTS_LIST=$(gh api \
    -H "Accept: application/vnd.github+json" \
    /repos/{owner}/{repo}/actions/artifacts)

    echo "downloading artifacts for run $GITHUB_ARTIFACTS_RUN"
    TO_DOWNLOAD=$(echo "$ARTIFACTS_LIST" \
        | jq ".artifacts[] | select(.workflow_run.id == \"$GITHUB_ARTIFACTS_RUN\")")

    echo $TO_DOWNLOAD | jq -c '.' | while read artifactline; do
        ARTIFACT_NAME=$(echo $artifactline | jq -r .name)
        ARTIFACT_ID=$(echo $artifactline | jq .id)
        echo "Downloading $ARTIFACT_NAME: $ARTIFACT_ID"
        gh api "/repos/{owner}/{repo}/actions/artifacts/$ARTIFACT_ID/zip" > "$ARTIFACT_NAME.zip"
        unzip -o "$ARTIFACT_NAME.zip" -d "../$EXTRACT_TO"
    done

    popd
fi

echo Checking that expected images are present:

for image in android-arm android-arm64 linux-arm64 linux-armv5 linux-armv7 linux-s390x \
    linux-ppc64le linux-x64 linux-x86; do
    if [ -f "$EXTRACT_TO/$image/libh3-java.so" ]; then
        echo "$image" exists
    else
        echo "$image" missing!
        exit 1
    fi
done

for image in darwin-x64 darwin-arm64; do
    if [ -f "$EXTRACT_TO/$image/libh3-java.dylib" ]; then
        echo "$image" exists
    else
        echo "$image" missing!
        exit 1
    fi
done

for image in windows-x64 windows-x86; do
    if [ -f "$EXTRACT_TO/$image/libh3-java.dll" ]; then
        echo "$image" exists
    else
        echo "$image" missing!
        exit 1
    fi
done
