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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.CoordIJ;
import java.util.List;
import org.junit.Test;

/** Tests for grid traversal functions (k-ring, distance and line, and local IJ coordinates). */
public class TestTraversal extends BaseTestH3CoreV3 {
  @Test
  public void testKring() {
    List<String> hexagons = h3.kRing("8928308280fffff", 1);

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
  public void testKring2() {
    List<String> hexagons = h3.kRing("8928308280fffff", 2);

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
  public void testKrings() {
    String origin = "8928308280fffff";
    List<List<String>> hexagons = h3.kRings(origin, 2);
    assertEquals(h3.kRing(origin, 0), hexagons.get(0));
    assertEquals(h3.kRing(origin, 1), hexagons.get(1));
    assertEquals(h3.kRing(origin, 2), hexagons.get(2));
    assertEquals(3, hexagons.size());
  }

  @Test
  public void testKringLarge() {
    int k = 50;
    List<String> hexagons = h3.kRing("8928308280fffff", k);

    int expectedCount = 1;
    for (int i = 1; i <= k; i++) {
      expectedCount += i * 6;
    }

    assertEquals(expectedCount, hexagons.size());
  }

  @Test
  public void testKringPentagon() {
    List<String> hexagons = h3.kRing("821c07fffffffff", 1);

    assertEquals(1 + 5, hexagons.size());

    assertTrue(hexagons.contains("821c2ffffffffff"));
    assertTrue(hexagons.contains("821c27fffffffff"));
    assertTrue(hexagons.contains("821c07fffffffff"));
    assertTrue(hexagons.contains("821c17fffffffff"));
    assertTrue(hexagons.contains("821c1ffffffffff"));
    assertTrue(hexagons.contains("821c37fffffffff"));
  }

  @Test
  public void testHexRange() {
    List<List<String>> hexagons = h3.hexRange("8928308280fffff", 1);

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
  public void testHexRangeLong() {
    List<List<Long>> hexagons = h3.hexRange(0x8928308280fffffL, 1);

    assertEquals(2, hexagons.size());
    assertEquals(1, hexagons.get(0).size());
    assertEquals(6, hexagons.get(1).size());

    assertTrue(hexagons.get(0).contains(0x8928308280fffffL));
    assertTrue(hexagons.get(1).contains(0x8928308280bffffL));
    assertTrue(hexagons.get(1).contains(0x89283082807ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082877ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082803ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082873ffffL));
    assertTrue(hexagons.get(1).contains(0x8928308283bffffL));
  }

  @Test
  public void testKRingDistances() {
    List<List<String>> hexagons = h3.kRingDistances("8928308280fffff", 1);

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
  public void testKRingDistancesLong() {
    List<List<Long>> hexagons = h3.kRingDistances(0x8928308280fffffL, 1);

    assertEquals(2, hexagons.size());
    assertEquals(1, hexagons.get(0).size());
    assertEquals(6, hexagons.get(1).size());

    assertTrue(hexagons.get(0).contains(0x8928308280fffffL));
    assertTrue(hexagons.get(1).contains(0x8928308280bffffL));
    assertTrue(hexagons.get(1).contains(0x89283082807ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082877ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082803ffffL));
    assertTrue(hexagons.get(1).contains(0x89283082873ffffL));
    assertTrue(hexagons.get(1).contains(0x8928308283bffffL));
  }

  @Test
  public void testHexRange2() {
    List<List<String>> hexagons = h3.kRingDistances("8928308280fffff", 2);

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
  public void testKRingDistancesPentagon() {
    h3.kRingDistances("821c07fffffffff", 1);
    // No exception should happen
  }

  @Test(expected = H3Exception.class)
  public void testHexRangePentagon() {
    h3.hexRange("821c07fffffffff", 1);
  }

  @Test
  public void testHexRing() {
    List<String> hexagons = h3.hexRing("8928308280fffff", 1);

    assertEquals(6, hexagons.size());

    assertTrue(hexagons.contains("8928308280bffff"));
    assertTrue(hexagons.contains("89283082807ffff"));
    assertTrue(hexagons.contains("89283082877ffff"));
    assertTrue(hexagons.contains("89283082803ffff"));
    assertTrue(hexagons.contains("89283082873ffff"));
    assertTrue(hexagons.contains("8928308283bffff"));
  }

  @Test
  public void testHexRingLong() {
    List<Long> hexagons = h3.hexRing(0x8928308280fffffL, 1);

    assertEquals(6, hexagons.size());

    assertTrue(hexagons.contains(0x8928308280bffffL));
    assertTrue(hexagons.contains(0x89283082807ffffL));
    assertTrue(hexagons.contains(0x89283082877ffffL));
    assertTrue(hexagons.contains(0x89283082803ffffL));
    assertTrue(hexagons.contains(0x89283082873ffffL));
    assertTrue(hexagons.contains(0x8928308283bffffL));
  }

  @Test
  public void testHexRing2() {
    List<String> hexagons = h3.hexRing("8928308280fffff", 2);

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
  public void testHexRingLarge() {
    int k = 50;
    List<String> hexagons = h3.hexRing("8928308280fffff", k);

    int expectedCount = 50 * 6;

    assertEquals(expectedCount, hexagons.size());
  }

  @Test(expected = H3Exception.class)
  public void testHexRingPentagon() {
    h3.hexRing("821c07fffffffff", 1);
  }

  @Test(expected = H3Exception.class)
  public void testHexRingAroundPentagon() {
    h3.hexRing("821c37fffffffff", 2);
  }

  @Test
  public void testHexRingSingle() {
    String origin = "8928308280fffff";
    List<String> hexagons = h3.hexRing(origin, 0);

    assertEquals(1, hexagons.size());
    assertEquals("8928308280fffff", hexagons.get(0));
  }

  @Test(expected = H3Exception.class)
  public void testH3DistanceFailedDistance() {
    // This fails because of a limitation in the H3 core library.
    // It cannot find distances when spanning more than one base cell.
    // Expected correct result is 2.
    h3.h3Distance("8029fffffffffff", "8079fffffffffff");
  }

  @Test(expected = H3Exception.class)
  public void testH3DistanceFailedResolution() {
    // Cannot find distances when the indexes are not comparable (different resolutions)
    h3.h3Distance("81283ffffffffff", "8029fffffffffff");
  }

  @Test(expected = H3Exception.class)
  public void testH3DistanceFailedPentagonDistortion() {
    // This fails because of a limitation in the H3 core library.
    // It cannot find distances from opposite sides of a pentagon.
    // Expected correct result is 9.
    h3.h3Distance("821c37fffffffff", "822837fffffffff");
  }

  @Test
  public void testH3Distance() {
    // Resolution 0 to some neighbors
    assertEquals(0, h3.h3Distance(0x8029fffffffffffL, 0x8029fffffffffffL));
    assertEquals(1, h3.h3Distance("8029fffffffffff", "801dfffffffffff"));
    assertEquals(1, h3.h3Distance("8029fffffffffff", "8037fffffffffff"));

    // Resolution 1 from a base cell onto a pentagon
    assertEquals(2, h3.h3Distance("81283ffffffffff", "811d7ffffffffff"));
    assertEquals(2, h3.h3Distance("81283ffffffffff", "811cfffffffffff"));
    assertEquals(3, h3.h3Distance("81283ffffffffff", "811c3ffffffffff"));

    // Resolution 5 within the same base cell
    assertEquals(0, h3.h3Distance("85283083fffffff", "85283083fffffff"));
    assertEquals(1, h3.h3Distance("85283083fffffff", "85283093fffffff"));
    assertEquals(2, h3.h3Distance("85283083fffffff", "8528342bfffffff"));
    assertEquals(3, h3.h3Distance("85283083fffffff", "85283477fffffff"));
    assertEquals(4, h3.h3Distance("85283083fffffff", "85283473fffffff"));
    assertEquals(5, h3.h3Distance(0x85283083fffffffL, 0x85283447fffffffL));
  }

  @Test(expected = H3Exception.class)
  public void testDistanceAcrossPentagon() {
    // Opposite sides of a pentagon.
    h3.h3Distance("81283ffffffffff", "811dbffffffffff");
  }

  @Test(expected = H3Exception.class)
  public void testCellToLocalIjNoncomparable() {
    h3.experimentalH3ToLocalIj(0x832830fffffffffL, 0x822837fffffffffL);
  }

  @Test(expected = H3Exception.class)
  public void testCellToLocalIjTooFar() {
    h3.experimentalH3ToLocalIj("822a17fffffffff", "822837fffffffff");
  }

  @Test(expected = H3Exception.class)
  public void testCellToLocalIjPentagonDistortion() {
    h3.experimentalH3ToLocalIj("81283ffffffffff", "811cbffffffffff");
  }

  @Test
  public void testCellToLocalIjPentagon() {
    final String origin = "811c3ffffffffff";
    assertEquals(new CoordIJ(0, 0), h3.experimentalH3ToLocalIj(origin, origin));
    assertEquals(new CoordIJ(1, 0), h3.experimentalH3ToLocalIj(origin, "811d3ffffffffff"));
    assertEquals(new CoordIJ(-1, 0), h3.experimentalH3ToLocalIj(origin, "811cfffffffffff"));
  }

  @Test
  public void testCellToLocalIjHexagons() {
    final String origin = "8828308281fffff";
    assertEquals(new CoordIJ(392, 336), h3.experimentalH3ToLocalIj(origin, origin));
    assertEquals(new CoordIJ(387, 336), h3.experimentalH3ToLocalIj(origin, "88283080c3fffff"));
    assertEquals(new CoordIJ(392, -14), h3.experimentalH3ToLocalIj(origin, "8828209581fffff"));
  }

  @Test
  public void testLocalIjToCellPentagon() {
    final String origin = "811c3ffffffffff";
    assertEquals(origin, h3.experimentalLocalIjToH3(origin, new CoordIJ(0, 0)));
    assertEquals("811d3ffffffffff", h3.experimentalLocalIjToH3(origin, new CoordIJ(1, 0)));
    assertEquals("811cfffffffffff", h3.experimentalLocalIjToH3(origin, new CoordIJ(-1, 0)));
  }

  @Test
  public void testCellToLocalIjHexagonsLong() {
    final long origin = 0x8828308281fffffL;
    assertEquals(new CoordIJ(392, 336), h3.experimentalH3ToLocalIj(origin, origin));
    assertEquals(new CoordIJ(387, 336), h3.experimentalH3ToLocalIj(origin, 0x88283080c3fffffL));
    assertEquals(new CoordIJ(392, -14), h3.experimentalH3ToLocalIj(origin, 0x8828209581fffffL));
  }

  @Test
  public void testLocalIjToCellPentagonLong() {
    final long origin = 0x811c3ffffffffffL;
    assertEquals(origin, h3.experimentalLocalIjToH3(origin, new CoordIJ(0, 0)));
    assertEquals(0x811d3ffffffffffL, h3.experimentalLocalIjToH3(origin, new CoordIJ(1, 0)));
    assertEquals(0x811cfffffffffffL, h3.experimentalLocalIjToH3(origin, new CoordIJ(-1, 0)));
  }

  @Test(expected = H3Exception.class)
  public void testLocalIjToCellTooFar() {
    h3.experimentalLocalIjToH3("8049fffffffffff", new CoordIJ(2, 0));
  }

  @Test(expected = H3Exception.class)
  public void testLocalIjToCellTooFarLong() {
    h3.experimentalLocalIjToH3(0x8049fffffffffffL, new CoordIJ(2, 0));
  }

  @Test
  public void testH3Line() {
    for (int res = 0; res < 12; res++) {
      String origin = h3.geoToH3Address(37.5, -122, res);
      String destination = h3.geoToH3Address(25, -120, res);

      List<String> line = h3.h3Line(origin, destination);
      long distance = h3.h3Distance(origin, destination);

      // Need to add 1 to account for the origin as well
      assertEquals("Distance matches expected", distance + 1, line.size());

      for (int i = 1; i < line.size(); i++) {
        assertTrue(
            "Every index in the line is a neighbor of the previous",
            h3.h3IndexesAreNeighbors(line.get(i - 1), line.get(i)));
      }

      assertTrue("Line contains start", line.contains(origin));
      assertTrue("Line contains destination", line.contains(destination));
    }
  }

  @Test
  public void testH3LineLong() {
    for (int res = 0; res < 12; res++) {
      long origin = h3.geoToH3(37.5, -122, res);
      long destination = h3.geoToH3(25, -120, res);

      List<Long> line = h3.h3Line(origin, destination);
      long distance = h3.h3Distance(origin, destination);

      // Need to add 1 to account for the origin as well
      assertEquals("Distance matches expected", distance + 1, line.size());

      for (int i = 1; i < line.size(); i++) {
        assertTrue(
            "Every index in the line is a neighbor of the previous",
            h3.h3IndexesAreNeighbors(line.get(i - 1), line.get(i)));
      }

      assertTrue("Line contains start", line.contains(origin));
      assertTrue("Line contains destination", line.contains(destination));
    }
  }

  @Test(expected = H3Exception.class)
  public void testH3LineFailed() {
    long origin = h3.geoToH3(37.5, -122, 9);
    long destination = h3.geoToH3(37.5, -122, 10);

    h3.h3Line(origin, destination);
  }
}
