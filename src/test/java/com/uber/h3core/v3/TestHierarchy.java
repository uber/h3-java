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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.uber.h3core.exceptions.H3Exception;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for grid hierarchy functions (compact, uncompact, children, parent) */
class TestHierarchy extends BaseTestH3CoreV3 {
  @Test
  void h3ToParent() {
    assertEquals(0x801dfffffffffffL, h3.h3ToParent(0x811d7ffffffffffL, 0));
    assertEquals(0x801dfffffffffffL, h3.h3ToParent(0x801dfffffffffffL, 0));
    assertEquals(0x8828308281fffffL, h3.h3ToParent(0x8928308280fffffL, 8));
    assertEquals(0x872830828ffffffL, h3.h3ToParent(0x8928308280fffffL, 7));
    assertEquals("872830828ffffff", h3.h3ToParentAddress("8928308280fffff", 7));
  }

  @Test
  void h3ToParentInvalidRes() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToParent(0, 5));
  }

  @Test
  void h3ToParentInvalid() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToParent(0x8928308280fffffL, -1));
  }

  @Test
  void h3ToParentInvalid2() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToParent(0x8928308280fffffL, 17));
  }

  @Test
  void h3ToParentInvalid3() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToParent(0, 17));
  }

  @Test
  void h3ToChildren() {
    List<String> sfChildren = h3.h3ToChildren("88283082803ffff", 9);

    assertEquals(7, sfChildren.size());
    assertTrue(sfChildren.contains("8928308280fffff"));
    assertTrue(sfChildren.contains("8928308280bffff"));
    assertTrue(sfChildren.contains("8928308281bffff"));
    assertTrue(sfChildren.contains("89283082813ffff"));
    assertTrue(sfChildren.contains("89283082817ffff"));
    assertTrue(sfChildren.contains("89283082807ffff"));
    assertTrue(sfChildren.contains("89283082803ffff"));

    List<Long> pentagonChildren = h3.h3ToChildren(0x801dfffffffffffL, 2);

    // res 0 pentagon has 5 hexagon children and 1 pentagon child at res 1.
    // Total output will be:
    //   5 * 7 children of res 1 hexagons
    //   6 children of res 1 pentagon
    assertEquals(5 * 7 + 6, pentagonChildren.size());

    // Don't crash
    h3.h3ToChildren(0, 2);
    try {
      h3.h3ToChildren("88283082803ffff", -1);
      assertTrue(false);
    } catch (IllegalArgumentException ex) {
      // expected
    }
    try {
      h3.h3ToChildren("88283082803ffff", 17);
      assertTrue(false);
    } catch (IllegalArgumentException ex) {
      // expected
    }
  }

  @Test
  void compact() {
    // Some random location
    String starting = h3.geoToH3Address(30, 20, 6);

    Collection<String> expanded = h3.kRing(starting, 8);

    Collection<String> compacted = h3.compactAddress(expanded);

    // Visually inspected the results to determine this was OK.
    assertEquals(61, compacted.size());

    Collection<String> uncompacted = h3.uncompactAddress(compacted, 6);

    assertEquals(expanded.size(), uncompacted.size());

    // Assert contents are the same
    assertEquals(new HashSet<>(expanded), new HashSet<>(uncompacted));
  }

  @Test
  void compactLong() {
    // Some random location
    long starting = h3.geoToH3(30, 20, 6);

    Collection<Long> expanded = h3.kRing(starting, 8);

    Collection<Long> compacted = h3.compact(expanded);

    // Visually inspected the results to determine this was OK.
    assertEquals(61, compacted.size());

    Collection<Long> uncompacted = h3.uncompact(compacted, 6);

    assertEquals(expanded.size(), uncompacted.size());

    // Assert contents are the same
    assertEquals(new HashSet<>(expanded), new HashSet<>(uncompacted));
  }

  @Test
  void compactInvalid() {
    String starting = h3.geoToH3Address(30, 20, 6);
    List<String> expanded = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      expanded.add(starting);
    }
    assertThrows(RuntimeException.class, () -> h3.compactAddress(expanded));
  }

  @Test
  void uncompactPentagon() {
    List<String> addresses = h3.uncompactAddress(ImmutableList.of("821c07fffffffff"), 3);
    assertEquals(6, addresses.size());
    addresses.stream().forEach(h -> assertEquals(3, h3.h3GetResolution(h)));
  }

  @Test
  void uncompactZero() {
    assertEquals(0, h3.uncompactAddress(ImmutableList.of("0"), 3).size());
  }

  @Test
  void uncompactInvalid() {
    assertThrows(
        RuntimeException.class, () -> h3.uncompactAddress(ImmutableList.of("85283473fffffff"), 4));
  }

  @Test
  void h3ToCenterChild() {
    assertEquals(
        "8928308280fffff",
        h3.h3ToCenterChild("8928308280fffff", 9),
        "Same resolution as parent results in same index");
    assertEquals(
        0x8928308280fffffL,
        h3.h3ToCenterChild(0x8928308280fffffL, 9),
        "Same resolution as parent results in same index");

    assertEquals(
        "8a28308280c7fff",
        h3.h3ToCenterChild("8928308280fffff", 10),
        "Direct center child is correct");
    assertEquals(
        0x8a28308280c7fffL,
        h3.h3ToCenterChild(0x8928308280fffffL, 10),
        "Direct center child is correct");

    assertEquals(
        "8b28308280c0fff",
        h3.h3ToCenterChild("8928308280fffff", 11),
        "Center child skipping a resolution is correct");
    assertEquals(
        0x8b28308280c0fffL,
        h3.h3ToCenterChild(0x8928308280fffffL, 11),
        "Center child skipping a resolution is correct");
  }

  @Test
  void h3ToCenterChildParent() {
    assertThrows(H3Exception.class, () -> h3.h3ToCenterChild("8928308280fffff", 8));
  }

  @Test
  void h3ToCenterChildNegative() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToCenterChild("8928308280fffff", -1));
  }

  @Test
  void h3ToCenterChildOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> h3.h3ToCenterChild("8928308280fffff", 16));
  }
}
