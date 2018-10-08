/*
 * Copyright 2018 Uber Technologies, Inc.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestCoordIJ {
    @Test
    public void test() {
        CoordIJ ij1 = new CoordIJ(0, 0);
        CoordIJ ij2 = new CoordIJ(1, 10);
        CoordIJ ij3 = new CoordIJ(0, 0);

        assertEquals(0, ij1.i);
        assertEquals(0, ij1.j);
        assertEquals(1, ij2.i);
        assertEquals(10, ij2.j);
        assertEquals(0, ij3.i);
        assertEquals(0, ij3.j);

        assertNotEquals(ij1, ij2);
        assertNotEquals(ij3, ij2);
        assertEquals(ij1, ij3);
        assertEquals(ij1, ij1);
        assertNotEquals(ij1, null);

        assertEquals(ij1.hashCode(), ij3.hashCode());
        // Not strictly needed, but likely
        assertNotEquals(ij1.hashCode(), ij2.hashCode());
    }

    @Test
    public void testToString() {
        CoordIJ ij = new CoordIJ(123, -456);

        String toString = ij.toString();
        assertTrue(toString.contains("i=123"));
        assertTrue(toString.contains("j=-456"));
    }
}
