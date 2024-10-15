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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.Collection;
import org.junit.jupiter.api.Test;

/** Tests for {@link H3Core} miscellaneous functions. */
class TestMiscellaneous extends BaseTestH3Core {
  @Test
  void constants() {
    double lastAreaKm2 = 0;
    double lastAreaM2 = 0;
    double lastEdgeLengthKm = 0;
    double lastEdgeLengthM = 0;
    long lastNumHexagons = Long.MAX_VALUE;
    for (int i = 15; i >= 0; i--) {
      double areaKm2 = h3.getHexagonAreaAvg(i, AreaUnit.km2);
      double areaM2 = h3.getHexagonAreaAvg(i, AreaUnit.m2);
      double edgeKm = h3.getHexagonEdgeLengthAvg(i, LengthUnit.km);
      double edgeM = h3.getHexagonEdgeLengthAvg(i, LengthUnit.m);
      long numHexagons = h3.getNumCells(i);

      assertTrue(areaKm2 > lastAreaKm2);
      assertTrue(areaM2 > lastAreaM2);
      assertTrue(areaM2 > areaKm2);
      assertTrue(edgeKm > lastEdgeLengthKm);
      assertTrue(edgeM > lastEdgeLengthM);
      assertTrue(edgeM > edgeKm);
      assertTrue(numHexagons < lastNumHexagons);
      assertTrue(numHexagons > 0);

      lastAreaKm2 = areaKm2;
      lastAreaM2 = areaM2;
      lastEdgeLengthKm = edgeKm;
      lastEdgeLengthM = edgeM;
      lastNumHexagons = numHexagons;
    }
  }

  @Test
  void getRes0Indexes() {
    Collection<String> indexesAddresses = h3.getRes0CellAddresses();
    Collection<Long> indexes = h3.getRes0Cells();

    assertEquals(
        indexes.size(), indexesAddresses.size(), "Both signatures return the same results (size)");

    for (Long index : indexes) {
      assertEquals(1, indexes.stream().filter(i -> i.equals(index)).count(), "Index is unique");
      assertTrue(h3.isValidCell(index), "Index is valid");
      assertEquals(0, h3.getResolution(index), "Index is res 0");
      assertTrue(
          indexesAddresses.contains(h3.h3ToString(index)),
          "Both signatures return the same results");
    }
  }

  @Test
  void getPentagonIndexes() {
    for (int res = 0; res < 16; res++) {
      Collection<String> indexesAddresses = h3.getPentagonAddresses(res);
      Collection<Long> indexes = h3.getPentagons(res);

      assertEquals(
          indexes.size(),
          indexesAddresses.size(),
          "Both signatures return the same results (size)");
      assertEquals(12, indexes.size(), "There are 12 pentagons per resolution");

      for (Long index : indexes) {
        assertEquals(1, indexes.stream().filter(i -> i.equals(index)).count(), "Index is unique");
        assertTrue(h3.isValidCell(index), "Index is valid");
        assertEquals(res, h3.getResolution(index), String.format("Index is res %d", res));
        assertTrue(
            indexesAddresses.contains(h3.h3ToString(index)),
            "Both signatures return the same results");
        assertTrue(h3.isPentagon(index), "Index is a pentagon");
      }
    }
  }

  @Test
  void cellArea() {
    double areasKm2[] = {
      2.562182162955496e+06,
      447684.20172018633,
      6.596162242711056e+04,
      9.228872919002590e+03,
      1.318694490797110e+03,
      1.879593512281298e+02,
      2.687164354763186e+01,
      3.840848847060638e+00,
      5.486939641329893e-01,
      7.838600808637444e-02,
      1.119834221989390e-02,
      1.599777169186614e-03,
      2.285390931423380e-04,
      3.264850232091780e-05,
      4.664070326136774e-06,
      6.662957615868888e-07
    };

    for (int res = 0; res <= 15; res++) {
      String cellAddress = h3.latLngToCellAddress(0, 0, res);
      long cell = h3.latLngToCell(0, 0, res);

      double areaAddressM2 = h3.cellArea(cellAddress, AreaUnit.m2);
      double areaAddressKm2 = h3.cellArea(cellAddress, AreaUnit.km2);
      double areaAddressRads2 = h3.cellArea(cellAddress, AreaUnit.rads2);
      double areaM2 = h3.cellArea(cell, AreaUnit.m2);
      double areaKm2 = h3.cellArea(cell, AreaUnit.km2);
      double areaRads2 = h3.cellArea(cell, AreaUnit.rads2);

      assertEquals(areasKm2[res], areaAddressKm2, EPSILON, "cell area should match expectation");
      assertEquals(areaAddressRads2, areaRads2, EPSILON, "rads2 cell area should agree");
      assertEquals(areaAddressKm2, areaKm2, EPSILON, "km2 cell area should agree");
      assertEquals(areaAddressM2, areaM2, EPSILON, "m2 cell area should agree");
      assertTrue(areaM2 > areaKm2, "m2 area greater than km2 area");
      assertTrue(areaKm2 > areaRads2, "km2 area greater than rads2 area");
    }
  }

  @Test
  void cellAreaInvalid() {
    // Passing in a zero should not cause a crash
    h3.cellArea(0, AreaUnit.rads2);
  }

  @Test
  void cellAreaInvalidUnit() {
    long cell = h3.latLngToCell(0, 0, 0);
    assertThrows(IllegalArgumentException.class, () -> h3.cellArea(cell, null));
  }

  @Test
  void edgeLength() {
    for (int res = 0; res <= 15; res++) {
      long cell = h3.latLngToCell(0, 0, res);

      for (long edge : h3.originToDirectedEdges(cell)) {
        String edgeAddress = h3.h3ToString(edge);

        double areaAddressM = h3.edgeLength(edgeAddress, LengthUnit.m);
        double areaAddressKm = h3.edgeLength(edgeAddress, LengthUnit.km);
        double areaAddressRads = h3.edgeLength(edgeAddress, LengthUnit.rads);
        double areaM = h3.edgeLength(edge, LengthUnit.m);
        double areaKm = h3.edgeLength(edge, LengthUnit.km);
        double areaRads = h3.edgeLength(edge, LengthUnit.rads);

        // Only asserts some properties of the functions that the edge lengths
        // should have certain relationships to each other, test isn't specific
        // to a cell's actual values.
        assertTrue(areaAddressRads > 0, "edge length should match expectation");
        assertEquals(areaAddressRads, areaRads, EPSILON, "rads edge length should agree");
        assertEquals(areaAddressKm, areaKm, EPSILON, "km edge length should agree");
        assertEquals(areaAddressM, areaM, EPSILON, "m edge length should agree");
        assertTrue(areaM > areaKm, "m length greater than km length");
        assertTrue(areaKm > areaRads, "km length greater than rads length");
      }
    }
  }

  @Test
  void edgeLengthInvalid() {
    assertThrows(
        H3Exception.class,
        () ->
            // Passing in a non-edge should not cause a crash
            h3.edgeLength(h3.latLngToCell(0, 0, 0), LengthUnit.km));
  }

  @Test
  void edgeLengthInvalidEdge() {
    assertThrows(H3Exception.class, () -> h3.edgeLength(0, LengthUnit.rads));
  }

  @Test
  void edgeLengthInvalidUnit() {
    long cell = h3.latLngToCell(0, 0, 0);
    long edge = h3.originToDirectedEdges(cell).get(0);
    assertThrows(IllegalArgumentException.class, () -> h3.edgeLength(edge, null));
  }

  @Test
  void pointDist() {
    LatLng[] testA = {new LatLng(10, 10), new LatLng(0, 0), new LatLng(23, 23)};
    LatLng[] testB = {new LatLng(10, -10), new LatLng(-10, 0), new LatLng(23, 23)};
    double[] testDistanceDegrees = {20, 10, 0};

    for (int i = 0; i < testA.length; i++) {
      LatLng a = testA[i];
      LatLng b = testB[i];
      double expectedRads = Math.toRadians(testDistanceDegrees[i]);

      double distRads = h3.greatCircleDistance(a, b, LengthUnit.rads);
      double distKm = h3.greatCircleDistance(a, b, LengthUnit.km);
      double distM = h3.greatCircleDistance(a, b, LengthUnit.m);

      // TODO: Epsilon is unusually large in the core H3 tests
      assertEquals(expectedRads, distRads, EPSILON * 10000, "radians distance is as expected");
      if (expectedRads == 0) {
        assertEquals(0, distM, EPSILON, "m distance is zero");
        assertEquals(0, distKm, EPSILON, "km distance is zero");
      } else {
        assertTrue(distM > distKm, "m distance greater than km distance");
        assertTrue(distKm > distRads, "km distance greater than rads distance");
      }
    }
  }

  @Test
  void pointDistNaN() {
    LatLng zero = new LatLng(0, 0);
    LatLng nan = new LatLng(Double.NaN, Double.NaN);
    double dist1 = h3.greatCircleDistance(nan, zero, LengthUnit.rads);
    double dist2 = h3.greatCircleDistance(zero, nan, LengthUnit.km);
    double dist3 = h3.greatCircleDistance(nan, nan, LengthUnit.m);
    assertTrue(Double.isNaN(dist1), "nan distance results in nan");
    assertTrue(Double.isNaN(dist2), "nan distance results in nan");
    assertTrue(Double.isNaN(dist3), "nan distance results in nan");
  }

  @Test
  void pointDistPositiveInfinity() {
    LatLng posInf = new LatLng(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    LatLng zero = new LatLng(0, 0);
    double dist = h3.greatCircleDistance(posInf, zero, LengthUnit.m);
    assertTrue(Double.isNaN(dist), "+Infinity distance results in NaN");
  }

  @Test
  void pointDistNegativeInfinity() {
    LatLng negInf = new LatLng(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    LatLng zero = new LatLng(0, 0);
    double dist = h3.greatCircleDistance(negInf, zero, LengthUnit.m);
    assertTrue(Double.isNaN(dist), "-Infinity distance results in NaN");
  }

  @Test
  void pointDistInvalid() {
    LatLng a = new LatLng(0, 0);
    LatLng b = new LatLng(0, 0);
    assertThrows(IllegalArgumentException.class, () -> h3.greatCircleDistance(a, b, null));
  }

  @Test
  void getPentagonIndexesNegativeRes() {
    assertThrows(IllegalArgumentException.class, () -> h3.getPentagonAddresses(-1));
  }

  @Test
  void getPentagonIndexesOutOfRangeRes() {
    assertThrows(IllegalArgumentException.class, () -> h3.getPentagonAddresses(20));
  }

  @Test
  void constantsInvalid() {
    assertThrows(IllegalArgumentException.class, () -> h3.getHexagonAreaAvg(-1, AreaUnit.km2));
  }

  @Test
  void constantsInvalidUnit() {
    assertThrows(IllegalArgumentException.class, () -> h3.getHexagonAreaAvg(-1, AreaUnit.rads2));
  }

  @Test
  void constantsInvalid2() {
    assertThrows(IllegalArgumentException.class, () -> h3.getHexagonAreaAvg(0, null));
  }

  @Test
  void constantsInvalid3() {
    assertThrows(IllegalArgumentException.class, () -> h3.getHexagonEdgeLengthAvg(0, null));
  }
}
