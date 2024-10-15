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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for indexing functions (geoToH3, h3ToGeo, h3ToGeoBoundary) */
class TestIndexing extends BaseTestH3CoreV3 {
  @Test
  void geoToH3() {
    assertEquals(h3.geoToH3(67.194013596, 191.598258018, 5), 22758474429497343L | (1L << 59L));
  }

  @Test
  void h3ToGeo() {
    LatLng coords = h3.h3ToGeo(22758474429497343L | (1L << 59L));
    assertEquals(67.15092686397713, coords.lat, EPSILON);
    assertEquals(coords.lng, 191.6091114190303 - 360.0, EPSILON);

    LatLng coords2 = h3.h3ToGeo(Long.toHexString(22758474429497343L | (1L << 59L)));
    assertEquals(coords, coords2);
  }

  @Test
  void h3ToGeoBoundary() {
    List<LatLng> boundary = h3.h3ToGeoBoundary(22758474429497343L | (1L << 59L));
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

    List<LatLng> boundary2 = h3.h3ToGeoBoundary(Long.toHexString(22758474429497343L | (1L << 59L)));
    assertEquals(boundary, boundary2);
  }

  @Test
  void hostileInput() {
    assertNotEquals(0, h3.geoToH3(-987654321, 987654321, 5));
    assertNotEquals(0, h3.geoToH3(987654321, -987654321, 5));
  }

  @Test
  void hostileGeoToH3NaN() {
    assertThrows(H3Exception.class, () -> h3.geoToH3(Double.NaN, Double.NaN, 5));
  }

  @Test
  void hostileGeoToH3PositiveInfinity() {
    assertThrows(
        H3Exception.class, () -> h3.geoToH3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 5));
  }

  @Test
  void hostileGeoToH3NegativeInfinity() {
    assertThrows(
        H3Exception.class, () -> h3.geoToH3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 5));
  }

  @Test
  void hostileInputNegativeRes() {
    assertThrows(IllegalArgumentException.class, () -> h3.geoToH3(0, 0, -1));
  }

  @Test
  void hostileInputLargeRes() {
    assertThrows(IllegalArgumentException.class, () -> h3.geoToH3(0, 0, 1000));
  }

  @Test
  void hostileInputLatLng() {
    // Should not crash
    h3.geoToH3(1e45, 1e45, 0);
  }

  @Test
  void hostileInputMaximum() {
    h3.geoToH3(Double.MAX_VALUE, Double.MAX_VALUE, 0);
  }
}
