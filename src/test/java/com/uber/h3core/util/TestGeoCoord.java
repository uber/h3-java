/*
 * Copyright 2017-2018 Uber Technologies, Inc.
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

import com.uber.h3core.TestH3Core;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestGeoCoord {
    @Test
    public void test() {
        GeoCoord v1 = new GeoCoord(0, 1);
        GeoCoord v2 = new GeoCoord(1, 0);
        GeoCoord v3 = new GeoCoord(0, 1);

        assertEquals(0, v1.lat, TestH3Core.EPSILON);
        assertEquals(1, v1.lng, TestH3Core.EPSILON);
        assertEquals(1, v2.lat, TestH3Core.EPSILON);
        assertEquals(0, v2.lng, TestH3Core.EPSILON);
        assertEquals(0, v3.lat, TestH3Core.EPSILON);
        assertEquals(1, v3.lng, TestH3Core.EPSILON);

        assertNotEquals(v1, v2);
        assertNotEquals(v3, v2);
        assertEquals(v1, v3);
        assertEquals(v1, v1);
        assertNotEquals(v1, null);

        assertEquals(v1.hashCode(), v3.hashCode());
        // Not strictly needed, but likely
        assertNotEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testToString() {
        GeoCoord v = new GeoCoord(123.456, 456.789);

        String toString = v.toString();
        assertTrue(toString.contains("lat=123.456"));
        assertTrue(toString.contains("lng=456.789"));
    }
}
