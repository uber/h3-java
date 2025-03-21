/*
 * Copyright 2019, 2022 Uber Technologies, Inc.
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
package com.uber.h3core.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for index inspection and description functions. */
class TestInspection extends BaseTestH3CoreV3 {
  @Test
  void h3IsValid() {
    assertTrue(h3.h3IsValid(22758474429497343L | (1L << 59L)));
    assertFalse(h3.h3IsValid(-1L));
    assertTrue(h3.h3IsValid("8f28308280f18f2"));
    assertTrue(h3.h3IsValid("8F28308280F18F2"));
    assertTrue(h3.h3IsValid("08f28308280f18f2"));

    assertFalse(h3.h3IsValid(0x8f28308280f18f2L | (1L << 63L)));
    assertFalse(h3.h3IsValid(0x8f28308280f18f2L | (1L << 58L)));
  }

  @Test
  void h3GetResolution() {
    assertEquals(0, h3.h3GetResolution(0x8029fffffffffffL));
    assertEquals(15, h3.h3GetResolution(0x8f28308280f18f2L));
    assertEquals(14, h3.h3GetResolution(0x8e28308280f18f7L));
    assertEquals(9, h3.h3GetResolution("8928308280fffff"));

    // These are invalid, we're checking for not crashing.
    assertEquals(0, h3.h3GetResolution(0));
    assertEquals(15, h3.h3GetResolution(0xffffffffffffffffL));
  }

  @Test
  void h3IsResClassIII() {
    String r0 = h3.geoToH3Address(0, 0, 0);
    String r1 = h3.geoToH3Address(10, 0, 1);
    long r2 = h3.geoToH3(0, 10, 2);
    long r3 = h3.geoToH3(10, 10, 3);

    assertFalse(h3.h3IsResClassIII(r0));
    assertTrue(h3.h3IsResClassIII(r1));
    assertFalse(h3.h3IsResClassIII(r2));
    assertTrue(h3.h3IsResClassIII(r3));
  }

  @Test
  void h3GetBaseCell() {
    assertEquals(20, h3.h3GetBaseCell("8f28308280f18f2"));
    assertEquals(20, h3.h3GetBaseCell(0x8f28308280f18f2L));
    assertEquals(14, h3.h3GetBaseCell("821c07fffffffff"));
    assertEquals(14, h3.h3GetBaseCell(0x821c07fffffffffL));
  }

  @Test
  void h3IsPentagon() {
    assertFalse(h3.h3IsPentagon("8f28308280f18f2"));
    assertFalse(h3.h3IsPentagon(0x8f28308280f18f2L));
    assertTrue(h3.h3IsPentagon("821c07fffffffff"));
    assertTrue(h3.h3IsPentagon(0x821c07fffffffffL));
  }

  @Test
  void h3GetFaces() {
    assertH3Faces(1, h3.h3GetFaces(0x85283473fffffffL));
    assertH3Faces(1, h3.h3GetFaces("85283473fffffff"));

    assertH3Faces(2, h3.h3GetFaces(0x8167bffffffffffL));

    assertH3Faces(5, h3.h3GetFaces(0x804dfffffffffffL));
  }

  @Test
  void h3GetFacesInvalid() {
    // Don't crash
    h3.h3GetFaces(0);
  }

  private static void assertH3Faces(int expectedNumFaces, Collection<Integer> faces) {
    assertEquals(expectedNumFaces, faces.size());

    for (int i : faces) {
      assertTrue(i >= 0 && i < 20);
    }

    final Set<Integer> deduplicatedFaces = new HashSet<>(faces);
    assertEquals(deduplicatedFaces.size(), faces.size(), "All faces are unique");
  }
}
