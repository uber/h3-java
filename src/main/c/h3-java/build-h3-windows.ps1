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
# git-ref    - Specific git ref of H3 to build.
#
# This script downloads H3, builds H3 and the H3-Java native library for
# 32-bit and 64-bit Windows.
#
# This script expects to be run from the project's base directory (where
# pom.xml is) as part of the Maven build process.

Param([parameter(Position=0)]$gitRemote,
      [parameter(Position=1)]$gitRevision)

Set-PSDebug -Trace 1

Write-Output "Downloading H3 from $gitRemote"

New-Item -ItemType Directory -Name target
Push-Location target

if (-Not (Test-Path -PathType container h3)) {
    git clone $gitRemote h3
}

Push-Location h3
git fetch origin --tags

Write-Output "Using revision $gitRevision"
git checkout $gitRevision

$h3SrcRoot = Get-Location

Pop-Location #h3

#
# Now that H3 is downloaded, build H3-Java's native library for this platform.
#

# Tuple is h3-java name for the platform as the first item, and Visual 
# Studio name for the architecture as the second item.
ForEach ($Configuration in
    (New-Object "tuple[String,String]" "x64", "x64"),
    (New-object "tuple[String,String]" "x86", "Win32")) {

    $buildDirectory = "h3-java-build$($Configuration.Item1)"
    New-Item -ItemType Directory -Name $buildDirectory
    Push-Location $buildDirectory

    New-Item -ItemType Directory -Name build
    Push-Location build

    cmake -A $Configuration.Item2 `
        -DBUILD_SHARED_LIBS=OFF `
        -DCMAKE_POSITION_INDEPENDENT_CODE=ON `
        -DCMAKE_BUILD_TYPE=Release `
        ../../h3
    cmake --build . --target h3 --config Release
    cmake --build . --target binding-functions --config Release
    $h3BuildRoot = Get-Location

    Pop-Location #build

    cmake -A $Configuration.Item2 `
        -DUSE_NATIVE_JNI=ON `
        -DBUILD_SHARED_LIBS=ON `
        "-DH3_BUILD_ROOT=$h3BuildRoot" `
        "-DH3_CORE_LIBRARY_PATH=bin/Release/h3" `
        -DCMAKE_BUILD_TYPE=Release `
        ../../src/main/c/h3-java
    cmake --build . --target h3-java --config Release

    Pop-Location #h3-java-build

    Copy-Item "$buildDirectory/build/binding-functions" .

    $targetDirectory = "../src/main/resources/windows-$($Configuration.Item1)"
    New-Item -ItemType Directory -Name $targetDirectory
    Copy-Item "$buildDirectory/lib/Release/h3-java.dll" "$targetDirectory/libh3-java.dll"
}

Pop-Location #target
