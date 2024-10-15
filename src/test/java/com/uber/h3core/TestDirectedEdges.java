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
package com.uber.h3core;

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
class TestDirectedEdges extends BaseTestH3Core {
  @Test
  void unidirectionalEdges() {
    String start = "891ea6d6533ffff";
    String adjacent = "891ea6d65afffff";
    String notAdjacent = "891ea6992dbffff";

    assertTrue(h3.areNeighborCells(start, adjacent));
    assertFalse(h3.areNeighborCells(start, notAdjacent));
    // Indexes are not considered to neighbor themselves
    assertFalse(h3.areNeighborCells(start, start));

    String edge = h3.cellsToDirectedEdge(start, adjacent);

    assertTrue(h3.isValidDirectedEdge(edge));
    assertFalse(h3.isValidDirectedEdge(start));

    assertEquals(start, h3.getDirectedEdgeOrigin(edge));
    assertEquals(adjacent, h3.getDirectedEdgeDestination(edge));

    List<String> components = h3.directedEdgeToCells(edge);
    assertEquals(2, components.size());
    assertEquals(start, components.get(0));
    assertEquals(adjacent, components.get(1));

    Collection<String> edges = h3.originToDirectedEdges(start);
    assertEquals(6, edges.size());
    assertTrue(edges.contains(edge));

    List<LatLng> boundary = h3.directedEdgeToBoundary(edge);
    assertEquals(2, boundary.size());
  }

  @Test
  void unidirectionalEdgesNotNeighbors() {
    assertThrows(
        H3Exception.class, () -> h3.cellsToDirectedEdge("891ea6d6533ffff", "891ea6992dbffff"));
  }

  @Test
  void directedEdgeInvalid() {
    assertThrows(H3Exception.class, () -> h3.getDirectedEdgeOrigin(0));
  }
}
