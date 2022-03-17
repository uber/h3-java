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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.uber.h3core.util.GeoCoord;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

/** Tests for unidirectional edge functions. */
public class TestUnidirectionalEdges extends BaseTestH3Core {
  @Test
  public void testUnidirectionalEdges() {
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

    List<GeoCoord> boundary = h3.getH3UnidirectionalEdgeBoundary(edge);
    assertEquals(2, boundary.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnidirectionalEdgesNotNeighbors() {
    h3.getH3UnidirectionalEdge("891ea6d6533ffff", "891ea6992dbffff");
  }
}
