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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for grid hierarchy functions (compact, uncompact, children, parent)
 */
public class TestHierarchy extends BaseTestH3Core {
    @Test
    public void testH3ToParent() {
        assertEquals(0x801dfffffffffffL, h3.h3ToParent(0x811d7ffffffffffL, 0));
        assertEquals(0x801dfffffffffffL, h3.h3ToParent(0x801dfffffffffffL, 0));
        assertEquals(0x8828308281fffffL, h3.h3ToParent(0x8928308280fffffL, 8));
        assertEquals(0x872830828ffffffL, h3.h3ToParent(0x8928308280fffffL, 7));
        assertEquals("872830828ffffff", h3.h3ToParentAddress("8928308280fffff", 7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToParentInvalidRes() {
        h3.h3ToParent(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToParentInvalid() {
        h3.h3ToParent(0x8928308280fffffL, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToParentInvalid2() {
        h3.h3ToParent(0x8928308280fffffL, 17);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToParentInvalid3() {
        h3.h3ToParent(0, 17);
    }

    @Test
    public void testH3ToChildren() {
        List<String> sfChildren = h3.h3ToChildren("88283082803ffff", 9);

        assertEquals(7, sfChildren.size());
        assertTrue(sfChildren.contains("8928308280fffff"));
        assertTrue(sfChildren.contains("8928308280bffff"));
        assertTrue(sfChildren.contains("8928308281bffff"));
        assertTrue(sfChildren.contains("89283082813ffff"));
        assertTrue(sfChildren.contains("89283082817ffff"));
        assertTrue(sfChildren.contains("89283082807ffff"));
        assertTrue(sfChildren.contains("89283082803ffff"));

        List<Long> pentagonChildren = h3.h3ToChildren(0x801dfffffffffffL, 2);

        // res 0 pentagon has 5 hexagon children and 1 pentagon child at res 1.
        // Total output will be:
        //   5 * 7 children of res 1 hexagons
        //   6 children of res 1 pentagon
        assertEquals(5 * 7 + 6, pentagonChildren.size());

        // Don't crash
        h3.h3ToChildren(0, 2);
        try {
            h3.h3ToChildren("88283082803ffff", -1);
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            h3.h3ToChildren("88283082803ffff", 17);
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testCompact() {
        // Some random location
        String starting = h3.geoToH3Address(30, 20, 6);

        Collection<String> expanded = h3.kRing(starting, 8);

        Collection<String> compacted = h3.compactAddress(expanded);

        // Visually inspected the results to determine this was OK.
        assertEquals(61, compacted.size());

        Collection<String> uncompacted = h3.uncompactAddress(compacted, 6);

        assertEquals(expanded.size(), uncompacted.size());

        // Assert contents are the same
        assertEquals(new HashSet<>(expanded), new HashSet<>(uncompacted));
    }

    @Test(expected = RuntimeException.class)
    public void testCompactInvalid() {
        // Some random location
        String starting = h3.geoToH3Address(30, 20, 6);

        List<String> expanded = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            expanded.add(starting);
        }

        h3.compactAddress(expanded);
    }

    @Test
    public void testUncompactPentagon() {
        List<String> addresses = h3.uncompactAddress(ImmutableList.of("821c07fffffffff"), 3);
        assertEquals(6, addresses.size());
        addresses.stream()
                .forEach(h -> assertEquals(3, h3.h3GetResolution(h)));
    }

    @Test
    public void testUncompactZero() {
        assertEquals(0, h3.uncompactAddress(ImmutableList.of("0"), 3).size());
    }

    @Test(expected = RuntimeException.class)
    public void testUncompactInvalid() {
        h3.uncompactAddress(ImmutableList.of("85283473fffffff"), 4);
    }

    @Test
    public void testH3ToCenterChild() {
        assertEquals("Same resolution as parent results in same index", "8928308280fffff", h3.h3ToCenterChild("8928308280fffff", 9));
        assertEquals("Same resolution as parent results in same index", 0x8928308280fffffL, h3.h3ToCenterChild(0x8928308280fffffL, 9));

        assertEquals("Direct center child is correct", "8a28308280c7fff", h3.h3ToCenterChild("8928308280fffff", 10));
        assertEquals("Direct center child is correct", 0x8a28308280c7fffL, h3.h3ToCenterChild(0x8928308280fffffL, 10));

        assertEquals("Center child skipping a resolution is correct", "8b28308280c0fff", h3.h3ToCenterChild("8928308280fffff", 11));
        assertEquals("Center child skipping a resolution is correct", 0x8b28308280c0fffL, h3.h3ToCenterChild(0x8928308280fffffL, 11));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToCenterChildParent() {
        h3.h3ToCenterChild("8928308280fffff", 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToCenterChildNegative() {
        h3.h3ToCenterChild("8928308280fffff", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testH3ToCenterChildOutOfRange() {
        h3.h3ToCenterChild("8928308280fffff", 16);
    }
}
