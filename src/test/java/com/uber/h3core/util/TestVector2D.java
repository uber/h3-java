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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestVector2D {
    @Test
    public void test() {
        Vector2D v1 = new Vector2D(0, 1);
        Vector2D v2 = new Vector2D(1, 0);
        Vector2D v3 = new Vector2D(0, 1);

        assertNotEquals(null, v1);
        assertNotEquals(0, v1);
        assertNotEquals(v1, v2);
        assertNotEquals(v3, v2);
        assertEquals(v1, v3);

        assertEquals(v1.hashCode(), v3.hashCode());
        // Not strictly needed, but likely
        assertNotEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testToString() {
        Vector2D v = new Vector2D(123.456, 456.789);

        String toString = v.toString();
        assertTrue(toString.contains("x=123.456"));
        assertTrue(toString.contains("y=456.789"));
    }
}
