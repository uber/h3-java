
/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
