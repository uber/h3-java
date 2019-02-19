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

cmake_minimum_required(VERSION 3.1)

set(CMAKE_C_STANDARD_REQUIRED 1)
set(CMAKE_C_STANDARD 11)

include(CMakeDependentOption)

# Needed due to CMP0042
set(CMAKE_MACOSX_RPATH 1)
set(CMAKE_TOOLCHAIN_FILE "${CMAKE_CURRENT_SOURCE_DIR}/cmake/toolchain.cmake"
    CACHE FILEPATH
    "Toolchain to use for building this project")
set(LIBRARY_OUTPUT_PATH lib)
set(H3_SOVERSION 1)

project(h3-java LANGUAGES C)

include_directories(${H3_BUILD_ROOT}/src/h3lib/include)

if(USE_NATIVE_JNI)
    find_package(JNI REQUIRED)

    include_directories(${JNI_INCLUDE_DIRS})
else()
    include_directories(/java/include)
    # TODO Provide correct jni_md.h
    include_directories(/java/include/linux)
    include_directories(/java/include/darwin)
endif()

set(JNI_SOURCE_FILES
    ${PROJECT_SOURCE_DIR}/src/jniapi.c
    ${PROJECT_SOURCE_DIR}/src/com_uber_h3core_NativeMethods.h)

add_library(h3-java SHARED ${JNI_SOURCE_FILES})

# This is cached so the Windows build can indicate a different location
# for the native core library.
set(H3_CORE_LIBRARY_PATH "/lib/libh3" CACHE STRING "Path to the built core library, without suffix")
mark_as_advanced(H3_CORE_LIBRARY_PATH)

target_link_libraries(h3-java "${H3_BUILD_ROOT}/${H3_CORE_LIBRARY_PATH}${CMAKE_STATIC_LIBRARY_SUFFIX}")

find_library(M_LIB m)
if(M_LIB)
    target_link_libraries(h3-java ${M_LIB})
endif()

find_program(CLANG_FORMAT_PATH clang-format)
cmake_dependent_option(
    ENABLE_FORMAT "Enable running clang-format before compiling" ON
    "CLANG_FORMAT_PATH" OFF)
if (ENABLE_FORMAT)
    add_custom_target(
            formatjni
            COMMAND ${CLANG_FORMAT_PATH}
            -style=file
            -i
            ${JNI_SOURCE_FILES}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            COMMENT "Formatting JNI sources"
    )
    add_dependencies(h3-java formatjni)
elseif(NOT CLANG_FORMAT_PATH)
    message(WARNING "clang-format was not detected, "
                    "so automatic source code reformatting is disabled.")
endif()
