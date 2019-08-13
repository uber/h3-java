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
package com.uber.h3core;

import com.uber.h3core.util.GeoCoord;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for JNI code without going through {@link H3Core}.
 */
public class TestNativeMethods {
    protected static NativeMethods nativeMethods;

    @BeforeClass
    public static void setup() throws IOException {
        nativeMethods = H3CoreLoader.loadNatives();
    }

    /**
     * Test that h3SetToLinkedGeo properly propagates an exception
     */
    @Test
    public void testH3SetToLinkedGeoException() {
        final AtomicInteger counter = new AtomicInteger(0);

        try {
            nativeMethods.h3SetToLinkedGeo(new long[]{0x8928308280fffffL}, new ArrayList<List<List<GeoCoord>>>() {
                @Override
                public boolean add(List<List<GeoCoord>> lists) {
                    throw new RuntimeException("crashed#" + counter.getAndIncrement());
                }
            });
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertEquals("crashed#0", ex.getMessage());
        }
        assertEquals(1, counter.get());
    }

    @Test(expected = OutOfMemoryError.class)
    public void getRes0IndexesTooSmall() {
        nativeMethods.getRes0Indexes(new long[1]);
    }

    @Test(expected = OutOfMemoryError.class)
    public void getPentagonIndexes() {
        nativeMethods.getPentagonIndexes(1, new long[1]);
    }
}
