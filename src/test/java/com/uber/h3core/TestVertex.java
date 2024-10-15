/*
 * Copyright 2022 Uber Technologies, Inc.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for vertex functions */
class TestVertex extends BaseTestH3Core {
  @Test
  void cellToVertexNegative() {
    assertThrows(H3Exception.class, () -> h3.cellToVertex(0x823d6ffffffffffL, -1));
  }

  @Test
  void cellToVertexTooHigh() {
    assertThrows(H3Exception.class, () -> h3.cellToVertex(0x823d6ffffffffffL, 6));
  }

  @Test
  void cellToVertexPentagonInvalid() {
    assertThrows(H3Exception.class, () -> h3.cellToVertex(0x823007fffffffffL, 5));
  }

  @Test
  void cellToVertexInvalid() {
    assertThrows(H3Exception.class, () -> h3.cellToVertex("ffffffffffffffff", 5));
  }

  @Test
  void isValidVertex() {
    assertFalse(h3.isValidVertex(0xFFFFFFFFFFFFFFFFL));
    assertFalse(h3.isValidVertex(0));
    assertFalse(h3.isValidVertex(0x823d6ffffffffffL));
    assertTrue(h3.isValidVertex(0x2222597fffffffffL));
    assertFalse(h3.isValidVertex("823d6ffffffffff"));
    assertTrue(h3.isValidVertex("2222597fffffffff"));
  }

  @Test
  void cellToVertexes() {
    String origin = "823d6ffffffffff";
    Collection<String> verts = h3.cellToVertexes(origin);
    assertEquals(6, verts.size());
    for (int i = 0; i < 6; i++) {
      String vert = h3.cellToVertex(origin, i);
      assertTrue(verts.contains(vert));
      assertTrue(h3.isValidVertex(vert));
    }
  }

  @Test
  void cellToVertexesPentagon() {
    long origin = 0x823007fffffffffL;
    Collection<Long> verts = h3.cellToVertexes(origin);
    assertEquals(5, verts.size());
    for (int i = 0; i < 5; i++) {
      long vert = h3.cellToVertex(origin, i);
      assertTrue(verts.contains(vert));
      assertTrue(h3.isValidVertex(vert));
    }
  }

  @Test
  void cellToVertex() {
    long origin = 0x823d6ffffffffffL;
    String originAddress = h3.h3ToString(origin);
    Set<String> verts = new HashSet<>();
    for (int i = 0; i < 6; i++) {
      long vert = h3.cellToVertex(origin, i);
      String vertAddress = h3.cellToVertex(originAddress, i);
      assertEquals(h3.h3ToString(vert), vertAddress);
      assertTrue(h3.isValidVertex(vert));
      verts.add(vertAddress);
    }
    assertEquals(6, verts.size(), "Vertexes are unique");
  }

  @Test
  void vertexToLatLng() {
    String origin = "823d6ffffffffff";
    List<LatLng> bounds = h3.cellToBoundary(origin);
    for (int i = 0; i < 6; i++) {
      String vert = h3.cellToVertex(origin, i);
      LatLng latLng = h3.vertexToLatLng(vert);
      assertTrue(bounds.contains(latLng), "vertex found in boundary");
    }
  }

  @Test
  void vertexToLatLngInvalid() {
    assertThrows(H3Exception.class, () -> h3.vertexToLatLng("ffffffffffffffff"));
  }
}
