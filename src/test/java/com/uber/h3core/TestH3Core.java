/*
 * Copyright 2017-2019 Uber Technologies, Inc.
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

import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link H3Core} instantiation and miscellaneous functions.
 */
public class TestH3Core extends BaseTestH3Core {
    @Test
    public void testConstructAnother() throws IOException {
        assertNotNull(h3);

        H3Core another = H3Core.newInstance();
        assertNotNull(another);

        // Doesn't override equals.
        assertNotEquals(h3, another);
    }

    @Test
    public void testConstructSpecific() throws IOException {
        // This uses the same logic as H3CoreLoader for detecting
        // the OS and architecture, to avoid issues with CI.
        final H3CoreLoader.OperatingSystem os = H3CoreLoader.detectOs(System.getProperty("java.vendor"), System.getProperty("os.name"));
        final String arch = H3CoreLoader.detectArch(System.getProperty("os.arch"));

        H3Core another = H3Core.newInstance(os, arch);

        assertNotNull(another);
    }

    @Test
    public void testConstants() {
        double lastAreaKm2 = 0;
        double lastAreaM2 = 0;
        double lastEdgeLengthKm = 0;
        double lastEdgeLengthM = 0;
        long lastNumHexagons = Long.MAX_VALUE;
        for (int i = 15; i >= 0; i--) {
            double areaKm2 = h3.hexArea(i, AreaUnit.km2);
            double areaM2 = h3.hexArea(i, AreaUnit.m2);
            double edgeKm = h3.edgeLength(i, LengthUnit.km);
            double edgeM = h3.edgeLength(i, LengthUnit.m);
            long numHexagons = h3.numHexagons(i);

            assertTrue(areaKm2 > lastAreaKm2);
            assertTrue(areaM2 > lastAreaM2);
            assertTrue(areaM2 > areaKm2);
            assertTrue(edgeKm > lastEdgeLengthKm);
            assertTrue(edgeM > lastEdgeLengthM);
            assertTrue(edgeM > edgeKm);
            assertTrue(numHexagons < lastNumHexagons);
            assertTrue(numHexagons > 0);

            lastAreaKm2 = areaKm2;
            lastAreaM2 = areaM2;
            lastEdgeLengthKm = edgeKm;
            lastEdgeLengthM = edgeM;
            lastNumHexagons = numHexagons;
        }
    }

    @Test
    public void testGetRes0Indexes() {
        Collection<String> indexesAddresses = h3.getRes0IndexesAddresses();
        Collection<Long> indexes = h3.getRes0Indexes();

        assertEquals("Both signatures return the same results (size)", indexes.size(), indexesAddresses.size());

        for (Long index : indexes) {
            assertEquals("Index is unique", 1, indexes.stream().filter(i -> i.equals(index)).count());
            assertTrue("Index is valid", h3.h3IsValid(index));
            assertEquals("Index is res 0", 0, h3.h3GetResolution(index));
            assertTrue("Both signatures return the same results", indexesAddresses.contains(h3.h3ToString(index)));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstantsInvalid() {
        h3.hexArea(-1, AreaUnit.km2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstantsInvalid2() {
        h3.hexArea(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstantsInvalid3() {
        h3.edgeLength(0, null);
    }
}
