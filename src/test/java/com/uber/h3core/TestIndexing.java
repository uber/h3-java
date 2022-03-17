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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** Tests for indexing functions (geoToH3, h3ToGeo, h3ToGeoBoundary) */
public class TestIndexing extends BaseTestH3Core {
  @Test
  public void testGeoToH3() {
    assertEquals(h3.latLngToCell(67.194013596, 191.598258018, 5), 22758474429497343L | (1L << 59L));
  }

  @Test
  public void testH3ToGeo() {
    LatLng coords = h3.cellToLatLng(22758474429497343L | (1L << 59L));
    assertEquals(coords.lat, 67.15092686397713, EPSILON);
    assertEquals(coords.lng, 191.6091114190303 - 360.0, EPSILON);

    LatLng coords2 = h3.cellToLatLng(Long.toHexString(22758474429497343L | (1L << 59L)));
    assertEquals(coords, coords2);
  }

  @Test
  public void testH3ToGeoBoundary() {
    List<LatLng> boundary = h3.cellToBoundary(22758474429497343L | (1L << 59L));
    List<LatLng> actualBoundary = new ArrayList<>();
    actualBoundary.add(new LatLng(67.224749856, 191.476993415 - 360.0));
    actualBoundary.add(new LatLng(67.140938355, 191.373085667 - 360.0));
    actualBoundary.add(new LatLng(67.067252558, 191.505086715 - 360.0));
    actualBoundary.add(new LatLng(67.077062918, 191.740304069 - 360.0));
    actualBoundary.add(new LatLng(67.160561948, 191.845198829 - 360.0));
    actualBoundary.add(new LatLng(67.234563187, 191.713897218 - 360.0));

    for (int i = 0; i < 6; i++) {
      assertEquals(boundary.get(i).lat, actualBoundary.get(i).lat, EPSILON);
      assertEquals(boundary.get(i).lng, actualBoundary.get(i).lng, EPSILON);
    }

    List<LatLng> boundary2 = h3.cellToBoundary(Long.toHexString(22758474429497343L | (1L << 59L)));
    assertEquals(boundary, boundary2);
  }

  @Test
  public void testHostileInput() {
    assertNotEquals(0, h3.latLngToCell(-987654321, 987654321, 5));
    assertNotEquals(0, h3.latLngToCell(987654321, -987654321, 5));
  }

  @Test(expected = H3Exception.class)
  public void testHostileGeoToH3NaN() {
    h3.latLngToCell(Double.NaN, Double.NaN, 5);
  }

  @Test(expected = H3Exception.class)
  public void testHostileGeoToH3PositiveInfinity() {
    h3.latLngToCell(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 5);
  }

  @Test(expected = H3Exception.class)
  public void testHostileGeoToH3NegativeInfinity() {
    h3.latLngToCell(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHostileInputNegativeRes() {
    h3.latLngToCell(0, 0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHostileInputLargeRes() {
    h3.latLngToCell(0, 0, 1000);
  }

  @Test
  public void testHostileInputLatLng() {
    // Should not crash
    h3.latLngToCell(1e45, 1e45, 0);
  }

  @Test
  public void testHostileInputMaximum() {
    h3.latLngToCell(Double.MAX_VALUE, Double.MAX_VALUE, 0);
  }
}
