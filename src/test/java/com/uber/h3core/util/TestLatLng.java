/*
 * Copyright 2017-2018, 2022 Uber Technologies, Inc.
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
package com.uber.h3core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.BaseTestH3Core;
import org.junit.jupiter.api.Test;

/** */
class TestLatLng {
  @Test
  void test() {
    LatLng v1 = new LatLng(0, 1);
    LatLng v2 = new LatLng(1, 0);
    LatLng v3 = new LatLng(0, 1);

    assertEquals(0, v1.lat, BaseTestH3Core.EPSILON);
    assertEquals(1, v1.lng, BaseTestH3Core.EPSILON);
    assertEquals(1, v2.lat, BaseTestH3Core.EPSILON);
    assertEquals(0, v2.lng, BaseTestH3Core.EPSILON);
    assertEquals(0, v3.lat, BaseTestH3Core.EPSILON);
    assertEquals(1, v3.lng, BaseTestH3Core.EPSILON);

    assertNotEquals(v1, v2);
    assertNotEquals(v3, v2);
    assertEquals(v1, v3);
    assertEquals(v1, v1);
    assertNotEquals(null, v1);

    assertEquals(v1.hashCode(), v3.hashCode());
    // Not strictly needed, but likely
    assertNotEquals(v1.hashCode(), v2.hashCode());
  }

  @Test
  void testToString() {
    LatLng v = new LatLng(123.456, 456.789);

    String toString = v.toString();
    assertTrue(toString.contains("lat=123.456"));
    assertTrue(toString.contains("lng=456.789"));
  }
}
