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

# Needed due to CMP0042
set(CMAKE_MACOSX_RPATH 1)
set(CMAKE_TOOLCHAIN_FILE "${CMAKE_CURRENT_SOURCE_DIR}/cmake/toolchain.cmake"
    CACHE FILEPATH
    "Toolchain to use for building this project")
set(LIBRARY_OUTPUT_PATH lib)
set(H3_SOVERSION 1)

project(h3-java)

find_package(JNI)

include_directories(${H3_ROOT}/src/h3lib/include)
include_directories(${JNI_INCLUDE_DIRS})

# For building H3 itself
add_subdirectory(${H3_ROOT} build)

set(JNI_SOURCE_FILES
    ${PROJECT_SOURCE_DIR}/src/jniapi.c
    ${PROJECT_SOURCE_DIR}/src/com_uber_h3core_NativeMethods.h)

add_library(h3-java SHARED ${JNI_SOURCE_FILES})
if(BUILD_SHARED_LIBS)
    set_target_properties(h3 PROPERTIES SOVERSION ${H3_SOVERSION})
endif()
target_link_libraries(h3-java h3)

# ENABLE_FORMAT is from ${H3_ROOT}/CMakeLists.txt
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

add_dependencies(h3-java h3)
