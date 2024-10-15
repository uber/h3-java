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
package com.uber.h3core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests for JNI code without going through {@link H3Core}. */
class TestNativeMethods {
  protected static NativeMethods nativeMethods;

  @BeforeAll
  static void setup() throws IOException {
    nativeMethods = H3CoreLoader.loadNatives();
  }

  /** Test that h3SetToLinkedGeo properly propagates an exception */
  @Test
  void h3SetToLinkedGeoException() {
    final AtomicInteger counter = new AtomicInteger(0);

    try {
      nativeMethods.cellsToLinkedMultiPolygon(
          new long[] {0x8928308280fffffL},
          new ArrayList<List<List<LatLng>>>() {
            @Override
            public boolean add(List<List<LatLng>> lists) {
              throw new RuntimeException("crashed#" + counter.getAndIncrement());
            }
          });
      assertTrue(false);
    } catch (RuntimeException ex) {
      assertEquals("crashed#0", ex.getMessage());
    }
    assertEquals(1, counter.get());
  }

  @Test
  void getRes0IndexesTooSmall() {
    assertThrows(OutOfMemoryError.class, () -> nativeMethods.getRes0Cells(new long[1]));
  }

  @Test
  void getPentagonIndexes() {
    assertThrows(OutOfMemoryError.class, () -> nativeMethods.getPentagons(1, new long[1]));
  }

  @Test
  void invalidMode() {
    assertThrows(
        H3Exception.class,
        () ->
            nativeMethods.polygonToCells(
                new double[] {}, new int[] {}, new double[] {}, 0, 1, new long[] {}));
  }
}
