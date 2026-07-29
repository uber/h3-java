/*
 * Copyright 2026 Uber Technologies, Inc.
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
package com.uber.h3core.inputfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.BaseTestH3Core;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/** Test input files from core library. */
class TestCellToLatLng extends BaseTestH3Core {
  private static void testFile(Path path) {
    try {
      try (Scanner in = new Scanner(path, "UTF-8")) {
        while (in.hasNext()) {
          String cell = in.next();
          double lat = in.nextDouble();
          double lng = in.nextDouble();

          LatLng actual = h3.cellToLatLng(cell);

          double epsilon = 0.000001 * Math.PI / 180.0;

          assertTrue(latLngEquals(actual, new LatLng(lat, lng), epsilon));

          int res = h3.getResolution(cell);
          String cell2 = h3.latLngToCellAddress(actual.lat, actual.lng, res);
          assertEquals(cell, cell2);
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Test
  void test() throws IOException {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/res*ic.txt");
    assertEquals(
        5,
        Files.walk(Path.of("target/h3/tests/inputfiles"), 1)
            .filter(
                (path) -> {
                  if (matcher.matches(path)) {
                    testFile(path);

                    return true;
                  } else {
                    return false;
                  }
                })
            .count());
  }
}
