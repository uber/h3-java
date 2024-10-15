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
package com.uber.h3core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.CoordIJ;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for grid traversal functions (k-ring, distance and line, and local IJ coordinates). */
class TestTraversal extends BaseTestH3Core {
  @Test
  void kring() {
    List<String> hexagons = h3.gridDisk("8928308280fffff", 1);

    assertEquals(1 + 6, hexagons.size());

    assertTrue(hexagons.contains("8928308280fffff"));
    assertTrue(hexagons.contains("8928308280bffff"));
    assertTrue(hexagons.contains("89283082807ffff"));
    assertTrue(hexagons.contains("89283082877ffff"));
    assertTrue(hexagons.contains("89283082803ffff"));
    assertTrue(hexagons.contains("89283082873ffff"));
    assertTrue(hexagons.contains("8928308283bffff"));
  }

  @Test
  void kring2() {
    List<String> hexagons = h3.gridDisk("8928308280fffff", 2);

    assertEquals(1 + 6 + 12, hexagons.size());

    assertTrue(hexagons.contains("89283082813ffff"));
    assertTrue(hexagons.contains("89283082817ffff"));
    assertTrue(hexagons.contains("8928308281bffff"));
    assertTrue(hexagons.contains("89283082863ffff"));
    assertTrue(hexagons.contains("89283082823ffff"));
    assertTrue(hexagons.contains("89283082873ffff"));
    assertTrue(hexagons.contains("89283082877ffff"));
    assertTrue(hexagons.contains("8928308287bffff"));
    assertTrue(hexagons.contains("89283082833ffff"));
    assertTrue(hexagons.contains("8928308282bffff"));
    assertTrue(hexagons.contains("8928308283bffff"));
    assertTrue(hexagons.contains("89283082857ffff"));
    assertTrue(hexagons.contains("892830828abffff"));
    assertTrue(hexagons.contains("89283082847ffff"));
    assertTrue(hexagons.contains("89283082867ffff"));
    assertTrue(hexagons.contains("89283082803ffff"));
    assertTrue(hexagons.contains("89283082807ffff"));
    assertTrue(hexagons.contains("8928308280bffff"));
    assertTrue(hexagons.contains("8928308280fffff"));
  }

  @Test
  void kringLarge() {
    int k = 50;
    List<String> hexagons = h3.gridDisk("8928308280fffff", k);

    int expectedCount = 1;
    for (int i = 1; i <= k; i++) {
      expectedCount += i * 6;
    }

    assertEquals(expectedCount, hexagons.size());
  }

  @Test
  void kringTooLarge() {
    int k = 13780510;
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // Cannot be materialized into Java because the maximum array size is INT32_MAX
            h3.gridDisk(h3.latLngToCell(0, 0, 15), k));
  }

  @Test
  void kringPentagon() {
    List<String> hexagons = h3.gridDisk("821c07fffffffff", 1);

    assertEquals(1 + 5, hexagons.size());

    assertTrue(hexagons.contains("821c2ffffffffff"));
    assertTrue(hexagons.contains("821c27fffffffff"));
    assertTrue(hexagons.contains("821c07fffffffff"));
    assertTrue(hexagons.contains("821c17fffffffff"));
    assertTrue(hexagons.contains("821c1ffffffffff"));
    assertTrue(hexagons.contains("821c37fffffffff"));
  }

  @Test
  void hexRange() {
    List<List<String>> hexagons = h3.gridDiskUnsafe("8928308280fffff", 1);

    assertEquals(2, hexagons.size());
    assertEquals(1, hexagons.get(0).size());
    assertEquals(6, hexagons.get(1).size());

    assertTrue(hexagons.get(0).contains("8928308280fffff"));
    assertTrue(hexagons.get(1).contains("8928308280bffff"));
    assertTrue(hexagons.get(1).contains("89283082807ffff"));
    assertTrue(hexagons.get(1).contains("89283082877ffff"));
    assertTrue(hexagons.get(1).contains("89283082803ffff"));
    assertTrue(hexagons.get(1).contains("89283082873ffff"));
    assertTrue(hexagons.get(1).contains("8928308283bffff"));
  }

  @Test
  void kRingDistances() {
    List<List<String>> hexagons = h3.gridDiskDistances("8928308280fffff", 1);

    assertEquals(2, hexagons.size());
    assertEquals(1, hexagons.get(0).size());
    assertEquals(6, hexagons.get(1).size());

    assertTrue(hexagons.get(0).contains("8928308280fffff"));
    assertTrue(hexagons.get(1).contains("8928308280bffff"));
    assertTrue(hexagons.get(1).contains("89283082807ffff"));
    assertTrue(hexagons.get(1).contains("89283082877ffff"));
    assertTrue(hexagons.get(1).contains("89283082803ffff"));
    assertTrue(hexagons.get(1).contains("89283082873ffff"));
    assertTrue(hexagons.get(1).contains("8928308283bffff"));
  }

  @Test
  void hexRange2() {
    List<List<String>> hexagons = h3.gridDiskDistances("8928308280fffff", 2);

    assertEquals(3, hexagons.size());
    assertEquals(1, hexagons.get(0).size());
    assertEquals(6, hexagons.get(1).size());
    assertEquals(12, hexagons.get(2).size());

    assertTrue(hexagons.get(0).contains("8928308280fffff"));
    assertTrue(hexagons.get(1).contains("8928308280bffff"));
    assertTrue(hexagons.get(1).contains("89283082873ffff"));
    assertTrue(hexagons.get(1).contains("89283082877ffff"));
    assertTrue(hexagons.get(1).contains("8928308283bffff"));
    assertTrue(hexagons.get(1).contains("89283082807ffff"));
    assertTrue(hexagons.get(1).contains("89283082803ffff"));
    assertTrue(hexagons.get(2).contains("8928308281bffff"));
    assertTrue(hexagons.get(2).contains("89283082857ffff"));
    assertTrue(hexagons.get(2).contains("89283082847ffff"));
    assertTrue(hexagons.get(2).contains("8928308287bffff"));
    assertTrue(hexagons.get(2).contains("89283082863ffff"));
    assertTrue(hexagons.get(2).contains("89283082867ffff"));
    assertTrue(hexagons.get(2).contains("8928308282bffff"));
    assertTrue(hexagons.get(2).contains("89283082823ffff"));
    assertTrue(hexagons.get(2).contains("89283082833ffff"));
    assertTrue(hexagons.get(2).contains("892830828abffff"));
    assertTrue(hexagons.get(2).contains("89283082817ffff"));
    assertTrue(hexagons.get(2).contains("89283082813ffff"));
  }

  @Test
  void kRingDistancesPentagon() {
    h3.gridDiskDistances("821c07fffffffff", 1);
    // No exception should happen
  }

  @Test
  void hexRangePentagon() {
    assertThrows(H3Exception.class, () -> h3.gridDiskUnsafe("821c07fffffffff", 1));
  }

  @Test
  void hexRing() {
    List<String> hexagons = h3.gridRingUnsafe("8928308280fffff", 1);

    assertEquals(6, hexagons.size());

    assertTrue(hexagons.contains("8928308280bffff"));
    assertTrue(hexagons.contains("89283082807ffff"));
    assertTrue(hexagons.contains("89283082877ffff"));
    assertTrue(hexagons.contains("89283082803ffff"));
    assertTrue(hexagons.contains("89283082873ffff"));
    assertTrue(hexagons.contains("8928308283bffff"));
  }

  @Test
  void hexRing2() {
    List<String> hexagons = h3.gridRingUnsafe("8928308280fffff", 2);

    assertEquals(12, hexagons.size());

    assertTrue(hexagons.contains("89283082813ffff"));
    assertTrue(hexagons.contains("89283082817ffff"));
    assertTrue(hexagons.contains("8928308281bffff"));
    assertTrue(hexagons.contains("89283082863ffff"));
    assertTrue(hexagons.contains("89283082823ffff"));
    assertTrue(hexagons.contains("8928308287bffff"));
    assertTrue(hexagons.contains("89283082833ffff"));
    assertTrue(hexagons.contains("8928308282bffff"));
    assertTrue(hexagons.contains("89283082857ffff"));
    assertTrue(hexagons.contains("892830828abffff"));
    assertTrue(hexagons.contains("89283082847ffff"));
    assertTrue(hexagons.contains("89283082867ffff"));
  }

  @Test
  void hexRingLarge() {
    int k = 50;
    List<String> hexagons = h3.gridRingUnsafe("8928308280fffff", k);

    int expectedCount = 50 * 6;

    assertEquals(expectedCount, hexagons.size());
  }

  @Test
  void hexRingPentagon() {
    assertThrows(H3Exception.class, () -> h3.gridRingUnsafe("821c07fffffffff", 1));
  }

  @Test
  void hexRingAroundPentagon() {
    assertThrows(H3Exception.class, () -> h3.gridRingUnsafe("821c37fffffffff", 2));
  }

  @Test
  void hexRingSingle() {
    String origin = "8928308280fffff";
    List<String> hexagons = h3.gridRingUnsafe(origin, 0);

    assertEquals(1, hexagons.size());
    assertEquals("8928308280fffff", hexagons.get(0));
  }

  @Test
  void h3DistanceFailedDistance() {
    assertThrows(
        H3Exception.class,
        () ->
            // This fails because of a limitation in the H3 core library.
            // It cannot find distances when spanning more than one base cell.
            // Expected correct result is 2.
            h3.gridDistance("8029fffffffffff", "8079fffffffffff"));
  }

  @Test
  void h3DistanceFailedResolution() {
    assertThrows(
        H3Exception.class,
        () ->
            // Cannot find distances when the indexes are not comparable (different resolutions)
            h3.gridDistance("81283ffffffffff", "8029fffffffffff"));
  }

  @Test
  void h3DistanceFailedPentagonDistortion() {
    assertThrows(
        H3Exception.class,
        () ->
            // This fails because of a limitation in the H3 core library.
            // It cannot find distances from opposite sides of a pentagon.
            // Expected correct result is 9.
            h3.gridDistance("821c37fffffffff", "822837fffffffff"));
  }

  @Test
  void h3Distance() {
    // Resolution 0 to some neighbors
    assertEquals(0, h3.gridDistance("8029fffffffffff", "8029fffffffffff"));
    assertEquals(1, h3.gridDistance("8029fffffffffff", "801dfffffffffff"));
    assertEquals(1, h3.gridDistance("8029fffffffffff", "8037fffffffffff"));

    // Resolution 1 from a base cell onto a pentagon
    assertEquals(2, h3.gridDistance("81283ffffffffff", "811d7ffffffffff"));
    assertEquals(2, h3.gridDistance("81283ffffffffff", "811cfffffffffff"));
    assertEquals(3, h3.gridDistance("81283ffffffffff", "811c3ffffffffff"));

    // Resolution 5 within the same base cell
    assertEquals(0, h3.gridDistance("85283083fffffff", "85283083fffffff"));
    assertEquals(1, h3.gridDistance("85283083fffffff", "85283093fffffff"));
    assertEquals(2, h3.gridDistance("85283083fffffff", "8528342bfffffff"));
    assertEquals(3, h3.gridDistance("85283083fffffff", "85283477fffffff"));
    assertEquals(4, h3.gridDistance("85283083fffffff", "85283473fffffff"));
    assertEquals(5, h3.gridDistance("85283083fffffff", "85283447fffffff"));
  }

  @Test
  void distanceAcrossPentagon() {
    assertThrows(
        H3Exception.class,
        () ->
            // Opposite sides of a pentagon.
            h3.gridDistance("81283ffffffffff", "811dbffffffffff"));
  }

  @Test
  void cellToLocalIjNoncomparable() {
    assertThrows(H3Exception.class, () -> h3.cellToLocalIj("832830fffffffff", "822837fffffffff"));
  }

  @Test
  void cellToLocalIjTooFar() {
    assertThrows(H3Exception.class, () -> h3.cellToLocalIj("822a17fffffffff", "822837fffffffff"));
  }

  @Test
  void cellToLocalIjPentagonDistortion() {
    assertThrows(H3Exception.class, () -> h3.cellToLocalIj("81283ffffffffff", "811cbffffffffff"));
  }

  @Test
  void cellToLocalIjPentagon() {
    final String origin = "811c3ffffffffff";
    assertEquals(new CoordIJ(0, 0), h3.cellToLocalIj(origin, origin));
    assertEquals(new CoordIJ(1, 0), h3.cellToLocalIj(origin, "811d3ffffffffff"));
    assertEquals(new CoordIJ(-1, 0), h3.cellToLocalIj(origin, "811cfffffffffff"));
  }

  @Test
  void cellToLocalIjHexagons() {
    final String origin = "8828308281fffff";
    assertEquals(new CoordIJ(392, 336), h3.cellToLocalIj(origin, origin));
    assertEquals(new CoordIJ(387, 336), h3.cellToLocalIj(origin, "88283080c3fffff"));
    assertEquals(new CoordIJ(392, -14), h3.cellToLocalIj(origin, "8828209581fffff"));
  }

  @Test
  void localIjToCellPentagon() {
    final String origin = "811c3ffffffffff";
    assertEquals(origin, h3.localIjToCell(origin, new CoordIJ(0, 0)));
    assertEquals("811d3ffffffffff", h3.localIjToCell(origin, new CoordIJ(1, 0)));
    assertEquals("811cfffffffffff", h3.localIjToCell(origin, new CoordIJ(-1, 0)));
  }

  @Test
  void localIjToCellTooFar() {
    assertThrows(H3Exception.class, () -> h3.localIjToCell("8049fffffffffff", new CoordIJ(2, 0)));
  }

  @Test
  void h3Line() {
    for (int res = 0; res < 12; res++) {
      String origin = h3.latLngToCellAddress(37.5, -122, res);
      String destination = h3.latLngToCellAddress(25, -120, res);

      List<String> line = h3.gridPathCells(origin, destination);
      long distance = h3.gridDistance(origin, destination);

      // Need to add 1 to account for the origin as well
      assertEquals(distance + 1, line.size(), "Distance matches expected");

      for (int i = 1; i < line.size(); i++) {
        assertTrue(
            h3.areNeighborCells(line.get(i - 1), line.get(i)),
            "Every index in the line is a neighbor of the previous");
      }

      assertTrue(line.contains(origin), "Line contains start");
      assertTrue(line.contains(destination), "Line contains destination");
    }
  }

  @Test
  void h3LineFailed() {
    long origin = h3.latLngToCell(37.5, -122, 9);
    long destination = h3.latLngToCell(37.5, -122, 10);
    assertThrows(H3Exception.class, () -> h3.gridPathCells(origin, destination));
  }
}
