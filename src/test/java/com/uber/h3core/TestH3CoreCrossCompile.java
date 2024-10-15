/*
 * Copyright 2022 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.h3core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test that particular resource names exist in the built artifact when cross compiling. Although we
 * cannot test that those resources run correctly (since they can't be loaded), we can at least test
 * that the cross compiler put resources in the right locations. This test is only run if the system
 * property <code>h3.use.docker</code> has the value <code>true</code>.
 */
class TestH3CoreCrossCompile {
  @BeforeAll
  static void assumptions() {
    assumeTrue(
        "true".equals(System.getProperty("h3.use.docker")), "Docker cross compilation enabled");
  }

  @Test
  void resourcesExist() throws IOException {
    List<String> resources =
        ImmutableList.of(
            "/linux-x64/libh3-java.so",
            "/linux-x86/libh3-java.so",
            "/linux-arm64/libh3-java.so",
            "/linux-armv5/libh3-java.so",
            "/linux-armv7/libh3-java.so",
            "/linux-ppc64le/libh3-java.so",
            "/linux-s390x/libh3-java.so",
            "/windows-x64/libh3-java.dll",
            "/windows-x86/libh3-java.dll",
            "/darwin-x64/libh3-java.dylib",
            "/darwin-arm/libh3-java.dylib",
            "/freebsd-x64/libh3-java.so",
            "/android-arm/libh3-java.so",
            "/android-arm64/libh3-java.so");
    for (String name : resources) {
      assertNotNull(H3CoreLoader.class.getResource(name), name + " is an included resource");
    }
  }
}
