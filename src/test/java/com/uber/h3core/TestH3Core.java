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

import com.uber.h3core.util.GeoCoord;

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
        final H3CoreLoader.OperatingSystem os = H3CoreLoader.detectOs(System.getProperty("java.vendor"),
                System.getProperty("os.name"));
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

    @Test
    public void testGetPentagonIndexes() {
        for (int res = 0; res < 16; res++) {
            Collection<String> indexesAddresses = h3.getPentagonIndexesAddresses(res);
            Collection<Long> indexes = h3.getPentagonIndexes(res);

            assertEquals("Both signatures return the same results (size)", indexes.size(), indexesAddresses.size());
            assertEquals("There are 12 pentagons per resolution", 12, indexes.size());

            for (Long index : indexes) {
                assertEquals("Index is unique", 1, indexes.stream().filter(i -> i.equals(index)).count());
                assertTrue("Index is valid", h3.h3IsValid(index));
                assertEquals(String.format("Index is res %d", res), res, h3.h3GetResolution(index));
                assertTrue("Both signatures return the same results", indexesAddresses.contains(h3.h3ToString(index)));
                assertTrue("Index is a pentagon", h3.h3IsPentagon(index));
            }
        }
    }

    @Test
    public void testCellArea() {
        double areasKm2[] = { 2.562182162955496e+06, 4.476842018179411e+05, 6.596162242711056e+04,
                9.228872919002590e+03, 1.318694490797110e+03, 1.879593512281298e+02, 2.687164354763186e+01,
                3.840848847060638e+00, 5.486939641329893e-01, 7.838600808637444e-02, 1.119834221989390e-02,
                1.599777169186614e-03, 2.285390931423380e-04, 3.264850232091780e-05, 4.664070326136774e-06,
                6.662957615868888e-07 };

        for (int res = 0; res <= 15; res++) {
            String cellAddress = h3.geoToH3Address(0, 0, res);
            long cell = h3.geoToH3(0, 0, res);

            double areaAddressM2 = h3.cellArea(cellAddress, AreaUnit.m2);
            double areaAddressKm2 = h3.cellArea(cellAddress, AreaUnit.km2);
            double areaAddressRads2 = h3.cellArea(cellAddress, AreaUnit.rads2);
            double areaM2 = h3.cellArea(cell, AreaUnit.m2);
            double areaKm2 = h3.cellArea(cell, AreaUnit.km2);
            double areaRads2 = h3.cellArea(cell, AreaUnit.rads2);

            assertEquals("cell area should match expectation", areasKm2[res], areaAddressKm2, EPSILON);
            assertEquals("rads2 cell area should agree", areaAddressRads2, areaRads2, EPSILON);
            assertEquals("km2 cell area should agree", areaAddressKm2, areaKm2, EPSILON);
            assertEquals("m2 cell area should agree", areaAddressM2, areaM2, EPSILON);
            assertTrue("m2 area greater than km2 area", areaM2 > areaKm2);
            assertTrue("km2 area greater than rads2 area", areaKm2 > areaRads2);
        }
    }

    @Test
    public void testCellAreaInvalid() {
        // Passing in a zero should not cause a crash
        h3.cellArea(0, AreaUnit.rads2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCellAreaInvalidUnit() {
        long cell = h3.geoToH3(0, 0, 0);
        h3.cellArea(cell, null);
    }

    @Test
    public void testExactEdgeLength() {
        for (int res = 0; res <= 15; res++) {
            long cell = h3.geoToH3(0, 0, res);

            for (long edge : h3.getH3UnidirectionalEdgesFromHexagon(cell)) {
                String edgeAddress = h3.h3ToString(edge);

                double areaAddressM = h3.exactEdgeLength(edgeAddress, LengthUnit.m);
                double areaAddressKm = h3.exactEdgeLength(edgeAddress, LengthUnit.km);
                double areaAddressRads = h3.exactEdgeLength(edgeAddress, LengthUnit.rads);
                double areaM = h3.exactEdgeLength(edge, LengthUnit.m);
                double areaKm = h3.exactEdgeLength(edge, LengthUnit.km);
                double areaRads = h3.exactEdgeLength(edge, LengthUnit.rads);

                // Only asserts some properties of the functions that the edge lengths
                // should have certain relationships to each other, test isn't specific
                // to a cell's actual values.
                assertTrue("edge length should match expectation", areaAddressRads > 0);
                assertEquals("rads edge length should agree", areaAddressRads, areaRads, EPSILON);
                assertEquals("km edge length should agree", areaAddressKm, areaKm, EPSILON);
                assertEquals("m edge length should agree", areaAddressM, areaM, EPSILON);
                assertTrue("m length greater than km length", areaM > areaKm);
                assertTrue("km length greater than rads length", areaKm > areaRads);
            }
        }
    }

    @Test
    public void testExactEdgeLengthInvalid() {
        // Passing in a zero should not cause a crash
        h3.exactEdgeLength(0, LengthUnit.rads);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExactEdgeLengthInvalidUnit() {
        long cell = h3.geoToH3(0, 0, 0);
        long edge = h3.getH3UnidirectionalEdgesFromHexagon(cell).get(0);
        h3.exactEdgeLength(edge, null);
    }

    @Test
    public void testPointDist() {
        GeoCoord[] testA = { new GeoCoord(10, 10), new GeoCoord(0, 0), new GeoCoord(23, 23) };
        GeoCoord[] testB = { new GeoCoord(10, -10), new GeoCoord(-10, 0),new GeoCoord(23, 23) };
        double[] testDistanceDegrees = { 20, 10, 0 };

        for (int i = 0; i < testA.length; i++) {
            GeoCoord a = testA[i];
            GeoCoord b = testB[i];
            double expectedRads = Math.toRadians(testDistanceDegrees[i]);

            double distRads = h3.pointDist(a, b, LengthUnit.rads);
            double distKm = h3.pointDist(a, b, LengthUnit.km);
            double distM = h3.pointDist(a, b, LengthUnit.m);

            // TODO: Epsilon is unusually large in the core H3 tests
            assertEquals("radians distance is as expected", expectedRads, distRads, EPSILON * 10000);
            if (expectedRads == 0) {
                assertEquals("m distance is zero", 0, distM, EPSILON);
                assertEquals("km distance is zero", 0, distKm, EPSILON);
            } else {
                assertTrue("m distance greater than km distance", distM > distKm);
                assertTrue("km distance greater than rads distance", distKm > distRads);
            }
        }
    }

    @Test
    public void testPointDistNaN() {
        GeoCoord zero = new GeoCoord(0, 0);
        GeoCoord nan = new GeoCoord(Double.NaN, Double.NaN);
        double dist1 = h3.pointDist(nan, zero, LengthUnit.rads);
        double dist2 = h3.pointDist(zero, nan, LengthUnit.km);
        double dist3 = h3.pointDist(nan, nan, LengthUnit.m);
        assertTrue("nan distance results in nan", Double.isNaN(dist1));
        assertTrue("nan distance results in nan", Double.isNaN(dist2));
        assertTrue("nan distance results in nan", Double.isNaN(dist3));
    }

    @Test
    public void testPointDistPositiveInfinity() {
        GeoCoord posInf = new GeoCoord(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        GeoCoord zero = new GeoCoord(0, 0);
        double dist = h3.pointDist(posInf, zero, LengthUnit.m);
        assertTrue("+Infinity distance results in NaN", Double.isNaN(dist));
    }

    @Test
    public void testPointDistNegativeInfinity() {
        GeoCoord negInf = new GeoCoord(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        GeoCoord zero = new GeoCoord(0, 0);
        double dist = h3.pointDist(negInf, zero, LengthUnit.m);
        assertTrue("-Infinity distance results in NaN", Double.isNaN(dist));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPointDistInvalid() {
        GeoCoord a = new GeoCoord(0, 0);
        GeoCoord b = new GeoCoord(0, 0);
        h3.pointDist(a, b, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPentagonIndexesNegativeRes() {
        h3.getPentagonIndexesAddresses(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPentagonIndexesOutOfRangeRes() {
        h3.getPentagonIndexesAddresses(20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstantsInvalid() {
        h3.hexArea(-1, AreaUnit.km2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstantsInvalidUnit() {
        h3.hexArea(-1, AreaUnit.rads2);
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
