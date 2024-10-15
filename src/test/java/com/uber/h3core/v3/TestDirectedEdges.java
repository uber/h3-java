/*
 * Copyright 2019 Uber Technologies, Inc.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for unidirectional edge functions. */
class TestDirectedEdges extends BaseTestH3CoreV3 {
  @Test
  void unidirectionalEdges() {
    String start = "891ea6d6533ffff";
    String adjacent = "891ea6d65afffff";
    String notAdjacent = "891ea6992dbffff";

    assertTrue(h3.h3IndexesAreNeighbors(start, adjacent));
    assertFalse(h3.h3IndexesAreNeighbors(start, notAdjacent));
    // Indexes are not considered to neighbor themselves
    assertFalse(h3.h3IndexesAreNeighbors(start, start));

    String edge = h3.getH3UnidirectionalEdge(start, adjacent);

    assertTrue(h3.h3UnidirectionalEdgeIsValid(edge));
    assertFalse(h3.h3UnidirectionalEdgeIsValid(start));

    assertEquals(start, h3.getOriginH3IndexFromUnidirectionalEdge(edge));
    assertEquals(adjacent, h3.getDestinationH3IndexFromUnidirectionalEdge(edge));

    List<String> components = h3.getH3IndexesFromUnidirectionalEdge(edge);
    assertEquals(2, components.size());
    assertEquals(start, components.get(0));
    assertEquals(adjacent, components.get(1));

    Collection<String> edges = h3.getH3UnidirectionalEdgesFromHexagon(start);
    assertEquals(6, edges.size());
    assertTrue(edges.contains(edge));

    List<LatLng> boundary = h3.getH3UnidirectionalEdgeBoundary(edge);
    assertEquals(2, boundary.size());
  }

  @Test
  void unidirectionalEdgesLong() {
    long start = 0x891ea6d6533ffffL;
    long adjacent = 0x891ea6d65afffffL;
    long notAdjacent = 0x891ea6992dbffffL;

    assertTrue(h3.h3IndexesAreNeighbors(start, adjacent));
    assertFalse(h3.h3IndexesAreNeighbors(start, notAdjacent));
    // Indexes are not considered to neighbor themselves
    assertFalse(h3.h3IndexesAreNeighbors(start, start));

    long edge = h3.getH3UnidirectionalEdge(start, adjacent);

    assertTrue(h3.h3UnidirectionalEdgeIsValid(edge));
    assertFalse(h3.h3UnidirectionalEdgeIsValid(start));

    assertEquals(start, h3.getOriginH3IndexFromUnidirectionalEdge(edge));
    assertEquals(adjacent, h3.getDestinationH3IndexFromUnidirectionalEdge(edge));

    List<Long> components = h3.getH3IndexesFromUnidirectionalEdge(edge);
    assertEquals(2, components.size());
    assertEquals(start, (long) components.get(0));
    assertEquals(adjacent, (long) components.get(1));

    Collection<Long> edges = h3.getH3UnidirectionalEdgesFromHexagon(start);
    assertEquals(6, edges.size());
    assertTrue(edges.contains(edge));

    List<LatLng> boundary = h3.getH3UnidirectionalEdgeBoundary(edge);
    assertEquals(2, boundary.size());
  }

  @Test
  void unidirectionalEdgesNotNeighbors() {
    assertThrows(
        H3Exception.class, () -> h3.getH3UnidirectionalEdge("891ea6d6533ffff", "891ea6992dbffff"));
  }
}
