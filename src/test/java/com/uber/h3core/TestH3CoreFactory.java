/*
 * Copyright 2017-2019, 2022 Uber Technologies, Inc.
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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.junit.Test;

/** Tests for {@link H3Core} instantiation. */
public class TestH3CoreFactory extends BaseTestH3Core {
  @Test
  public void testConstructAnother() throws IOException {
    assertNotNull(h3);

    H3Core another = H3Core.newInstance();
    assertNotNull(another);

    // Doesn't override equals.
    assertNotEquals(h3, another);
  }

  @Test
  public void testConstructSpecific() throws IOException {
    // This uses the same logic as H3CoreLoader for detecting
    // the OS and architecture, to avoid issues with CI.
    final H3CoreLoader.OperatingSystem os =
        H3CoreLoader.detectOs(System.getProperty("java.vendor"), System.getProperty("os.name"));
    final String arch = H3CoreLoader.detectArch(System.getProperty("os.arch"));

    H3Core another = H3Core.newInstance(os, arch);

    assertNotNull(another);
  }

  // H3CoreV3 must be tested here due to method accessibility
  @Test
  public void testConstructAnotherV3() throws IOException {
    assertNotNull(h3);

    H3CoreV3 another = H3CoreV3.newInstance();
    assertNotNull(another);

    // Doesn't override equals.
    assertNotEquals(h3, another);
  }

  @Test
  public void testConstructSpecificV3() throws IOException {
    // This uses the same logic as H3CoreLoader for detecting
    // the OS and architecture, to avoid issues with CI.
    final H3CoreLoader.OperatingSystem os =
        H3CoreLoader.detectOs(System.getProperty("java.vendor"), System.getProperty("os.name"));
    final String arch = H3CoreLoader.detectArch(System.getProperty("os.arch"));

    H3CoreV3 another = H3CoreV3.newInstance(os, arch);

    assertNotNull(another);
  }
}
