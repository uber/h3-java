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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uber.h3core.exceptions.DistanceUndefinedException;
import com.uber.h3core.exceptions.LineUndefinedException;
import com.uber.h3core.exceptions.LocalIjUndefinedException;
import com.uber.h3core.exceptions.PentagonEncounteredException;
import com.uber.h3core.util.CoordIJ;
import com.uber.h3core.util.GeoCoord;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestH3Core {
    public static final double EPSILON = 1e-6;

    private static H3Core h3;

    @BeforeClass
    public static void setup() throws IOException {
        h3 = H3Core.newInstance();
    }

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
    public void testH3IsValid() {
        assertTrue(h3.h3IsValid(22758474429497343L | (1L << 59L)));
        assertFalse(h3.h3IsValid(-1L));
        assertTrue(h3.h3IsValid("8f28308280f18f2"));
    }

    @Test
    public void testGeoToH3() {
        assertEquals(h3.geoToH3(67.194013596, 191.598258018, 5), 22758474429497343L | (1L << 59L));
    }

    @Test
    public void testH3ToGeo() {
        GeoCoord coords = h3.h3ToGeo(22758474429497343L | (1L << 59L));
        assertEquals(coords.lat, 67.15092686397713, EPSILON);
        assertEquals(coords.lng, 191.6091114190303 - 360.0, EPSILON);

        GeoCoord coords2 = h3.h3ToGeo(Long.toHexString(22758474429497343L | (1L << 59L)));
        assertEquals(coords, coords2);
    }

    @Test
    public void testH3ToGeoBoundary() {
        List<GeoCoord> boundary = h3.h3ToGeoBoundary(22758474429497343L | (1L << 59L));
        List<GeoCoord> actualBoundary = new ArrayList<>();
        actualBoundary.add(new GeoCoord(67.224749856, 191.476993415 - 360.0));
        actualBoundary.add(new GeoCoord(67.140938355, 191.373085667 - 360.0));
        actualBoundary.add(new GeoCoord(67.067252558, 191.505086715 - 360.0));
        actualBoundary.add(new GeoCoord(67.077062918, 191.740304069 - 360.0));
        actualBoundary.add(new GeoCoord(67.160561948, 191.845198829 - 360.0));
        actualBoundary.add(new GeoCoord(67.234563187, 191.713897218 - 360.0));

        for (int i = 0; i < 6; i++) {
            assertEquals(boundary.get(i).lat, actualBoundary.get(i).lat, EPSILON);
            assertEquals(boundary.get(i).lng, actualBoundary.get(i).lng, EPSILON);
        }

        List<GeoCoord> boundary2 = h3.h3ToGeoBoundary(Long.toHexString(22758474429497343L | (1L << 59L)));
        assertEquals(boundary, boundary2);
    }

    @Test
    public void testKring() {
        List<String> hexagons = h3.kRing("8928308280fffff", 1);

        assertEquals(1 + 6, hexagons.size());

        assertTrue(hexagons.contains("8928308280fffff"));
        assertTrue(hexagons.contains("8928308280bffff"));
        assertTrue(hexagons.contains("89283082807ffff"));
        assertTrue(hexagons.contains("89283082877ffff"));
        assertTrue(hexagons.contains("89283082803ffff"));
        assertTrue(hexagons.contains("89283082873ffff"));
        assertTrue(hexagons.contains("8928308283bffff"));
    }

    @Test
    public void testKring2() {
        List<String> hexagons = h3.kRing("8928308280fffff", 2);

        assertEquals(1 + 6 + 12, hexagons.size());

        assertTrue(hexagons.contains("89283082813ffff"));
        assertTrue(hexagons.contains("89283082817ffff"));
        assertTrue(hexagons.contains("8928308281bffff"));
        assertTrue(hexagons.contains("89283082863ffff"));
        assertTrue(hexagons.contains("89283082823ffff"));
        assertTrue(hexagons.contains("89283082873ffff"));
        assertTrue(hexagons.contains("89283082877ffff"));
        assertTrue(hexagons.contains("8928308287bffff"));
        assertTrue(hexagons.contains("89283082833ffff"));
        assertTrue(hexagons.contains("8928308282bffff"));
        assertTrue(hexagons.contains("8928308283bffff"));
        assertTrue(hexagons.contains("89283082857ffff"));
        assertTrue(hexagons.contains("892830828abffff"));
        assertTrue(hexagons.contains("89283082847ffff"));
        assertTrue(hexagons.contains("89283082867ffff"));
        assertTrue(hexagons.contains("89283082803ffff"));
        assertTrue(hexagons.contains("89283082807ffff"));
        assertTrue(hexagons.contains("8928308280bffff"));
        assertTrue(hexagons.contains("8928308280fffff"));
    }

    @Test
    public void testKring1And2() {
        List<List<String>> kRings = h3.kRings("8928308280fffff", 2);

        List<String> hexagons;
        hexagons = kRings.get(1);

        assertEquals(1 + 6, hexagons.size());

        assertTrue(hexagons.contains("8928308280fffff"));
        assertTrue(hexagons.contains("8928308280bffff"));
        assertTrue(hexagons.contains("89283082807ffff"));
        assertTrue(hexagons.contains("89283082877ffff"));
        assertTrue(hexagons.contains("89283082803ffff"));
        assertTrue(hexagons.contains("89283082873ffff"));
        assertTrue(hexagons.contains("8928308283bffff"));

        hexagons = kRings.get(2);
        assertEquals(1 + 6 + 12, hexagons.size());

        assertTrue(hexagons.contains("89283082813ffff"));
        assertTrue(hexagons.contains("89283082817ffff"));
        assertTrue(hexagons.contains("8928308281bffff"));
        assertTrue(hexagons.contains("89283082863ffff"));
        assertTrue(hexagons.contains("89283082823ffff"));
        assertTrue(hexagons.contains("89283082873ffff"));
        assertTrue(hexagons.contains("89283082877ffff"));
        assertTrue(hexagons.contains("8928308287bffff"));
        assertTrue(hexagons.contains("89283082833ffff"));
        assertTrue(hexagons.contains("8928308282bffff"));
        assertTrue(hexagons.contains("8928308283bffff"));
        assertTrue(hexagons.contains("89283082857ffff"));
        assertTrue(hexagons.contains("892830828abffff"));
        assertTrue(hexagons.contains("89283082847ffff"));
        assertTrue(hexagons.contains("89283082867ffff"));
        assertTrue(hexagons.contains("89283082803ffff"));
        assertTrue(hexagons.contains("89283082807ffff"));
        assertTrue(hexagons.contains("8928308280bffff"));
        assertTrue(hexagons.contains("8928308280fffff"));
    }

    @Test
    public void testKringLarge() {
        int k = 50;
        List<String> hexagons = h3.kRing("8928308280fffff", k);

        int expectedCount = 1;
        for (int i = 1; i <= k; i++) {
            expectedCount += i * 6;
        }

        assertEquals(expectedCount, hexagons.size());
    }

    @Test
    public void testKringPentagon() {
        List<String> hexagons = h3.kRing("821c07fffffffff", 1);

        assertEquals(1 + 5, hexagons.size());

        assertTrue(hexagons.contains("821c2ffffffffff"));
        assertTrue(hexagons.contains("821c27fffffffff"));
        assertTrue(hexagons.contains("821c07fffffffff"));
        assertTrue(hexagons.contains("821c17fffffffff"));
        assertTrue(hexagons.contains("821c1ffffffffff"));
        assertTrue(hexagons.contains("821c37fffffffff"));
    }

    @Test
    public void testHexRange() throws PentagonEncounteredException {
        List<List<String>> hexagons = h3.hexRange("8928308280fffff", 1);

        assertEquals(2, hexagons.size());
        assertEquals(1, hexagons.get(0).size());
        assertEquals(6, hexagons.get(1).size());

        assertTrue(hexagons.get(0).contains("8928308280fffff"));
        assertTrue(hexagons.get(1).contains("8928308280bffff"));
        assertTrue(hexagons.get(1).contains("89283082807ffff"));
        assertTrue(hexagons.get(1).contains("89283082877ffff"));
        assertTrue(hexagons.get(1).contains("89283082803ffff"));
        assertTrue(hexagons.get(1).contains("89283082873ffff"));
        assertTrue(hexagons.get(1).contains("8928308283bffff"));
    }

    @Test
    public void testKRingDistances() {
        List<List<String>> hexagons = h3.kRingDistances("8928308280fffff", 1);

        assertEquals(2, hexagons.size());
        assertEquals(1, hexagons.get(0).size());
        assertEquals(6, hexagons.get(1).size());

        assertTrue(hexagons.get(0).contains("8928308280fffff"));
        assertTrue(hexagons.get(1).contains("8928308280bffff"));
        assertTrue(hexagons.get(1).contains("89283082807ffff"));
        assertTrue(hexagons.get(1).contains("89283082877ffff"));
        assertTrue(hexagons.get(1).contains("89283082803ffff"));
        assertTrue(hexagons.get(1).contains("89283082873ffff"));
        assertTrue(hexagons.get(1).contains("8928308283bffff"));
    }

    @Test
    public void testHexRange2() {
        List<List<String>> hexagons = h3.kRingDistances("8928308280fffff", 2);

        assertEquals(3, hexagons.size());
        assertEquals(1, hexagons.get(0).size());
        assertEquals(6, hexagons.get(1).size());
        assertEquals(12, hexagons.get(2).size());

        assertTrue(hexagons.get(0).contains("8928308280fffff"));
        assertTrue(hexagons.get(1).contains("8928308280bffff"));
        assertTrue(hexagons.get(1).contains("89283082873ffff"));
        assertTrue(hexagons.get(1).contains("89283082877ffff"));
        assertTrue(hexagons.get(1).contains("8928308283bffff"));
        assertTrue(hexagons.get(1).contains("89283082807ffff"));
        assertTrue(hexagons.get(1).contains("89283082803ffff"));
        assertTrue(hexagons.get(2).contains("8928308281bffff"));
        assertTrue(hexagons.get(2).contains("89283082857ffff"));
        assertTrue(hexagons.get(2).contains("89283082847ffff"));
        assertTrue(hexagons.get(2).contains("8928308287bffff"));
        assertTrue(hexagons.get(2).contains("89283082863ffff"));
        assertTrue(hexagons.get(2).contains("89283082867ffff"));
        assertTrue(hexagons.get(2).contains("8928308282bffff"));
        assertTrue(hexagons.get(2).contains("89283082823ffff"));
        assertTrue(hexagons.get(2).contains("89283082833ffff"));
        assertTrue(hexagons.get(2).contains("892830828abffff"));
        assertTrue(hexagons.get(2).contains("89283082817ffff"));
        assertTrue(hexagons.get(2).contains("89283082813ffff"));
    }

    @Test
    public void testKRingDistancesPentagon() {
        h3.kRingDistances("821c07fffffffff", 1);
        // No exception should happen
    }

    @Test(expected = PentagonEncounteredException.class)
    public void testHexRangePentagon() throws PentagonEncounteredException {
        h3.hexRange("821c07fffffffff", 1);
    }

    @Test
    public void testPolyfill() {
        List<Long> hexagons = h3.polyfill(
                ImmutableList.of(
                        new GeoCoord(37.813318999983238, -122.4089866999972145),
                        new GeoCoord(37.7866302000007224, -122.3805436999997056),
                        new GeoCoord(37.7198061999978478, -122.3544736999993603),
                        new GeoCoord(37.7076131999975672, -122.5123436999983966),
                        new GeoCoord(37.7835871999971715, -122.5247187000021967),
                        new GeoCoord(37.8151571999998453, -122.4798767000009008)
                ), null, 9
        );

        assertTrue(hexagons.size() > 1000);
    }

    @Test
    public void testPolyfillAddresses() {
        List<String> hexagons = h3.polyfillAddress(
                ImmutableList.<GeoCoord>of(
                        new GeoCoord(37.813318999983238, -122.4089866999972145),
                        new GeoCoord(37.7866302000007224, -122.3805436999997056),
                        new GeoCoord(37.7198061999978478, -122.3544736999993603),
                        new GeoCoord(37.7076131999975672, -122.5123436999983966),
                        new GeoCoord(37.7835871999971715, -122.5247187000021967),
                        new GeoCoord(37.8151571999998453, -122.4798767000009008)
                ), null, 9
        );

        assertTrue(hexagons.size() > 1000);
    }

    @Test
    public void testPolyfillWithHole() {
        List<Long> hexagons = h3.polyfill(
                ImmutableList.<GeoCoord>of(
                        new GeoCoord(37.813318999983238, -122.4089866999972145),
                        new GeoCoord(37.7866302000007224, -122.3805436999997056),
                        new GeoCoord(37.7198061999978478, -122.3544736999993603),
                        new GeoCoord(37.7076131999975672, -122.5123436999983966),
                        new GeoCoord(37.7835871999971715, -122.5247187000021967),
                        new GeoCoord(37.8151571999998453, -122.4798767000009008)
                ),
                ImmutableList.<List<GeoCoord>>of(
                        ImmutableList.<GeoCoord>of(
                                new GeoCoord(37.7869802, -122.4471197),
                                new GeoCoord(37.7664102, -122.4590777),
                                new GeoCoord(37.7710682, -122.4137097)
                        )
                ),
                9
        );

        assertTrue(hexagons.size() > 1000);
    }

    @Test
    public void testPolyfillWithTwoHoles() {
        List<Long> hexagons = h3.polyfill(
                ImmutableList.<GeoCoord>of(
                        new GeoCoord(37.813318999983238, -122.4089866999972145),
                        new GeoCoord(37.7866302000007224, -122.3805436999997056),
                        new GeoCoord(37.7198061999978478, -122.3544736999993603),
                        new GeoCoord(37.7076131999975672, -122.5123436999983966),
                        new GeoCoord(37.7835871999971715, -122.5247187000021967),
                        new GeoCoord(37.8151571999998453, -122.4798767000009008)
                ),
                ImmutableList.<List<GeoCoord>>of(
                        ImmutableList.<GeoCoord>of(
                                new GeoCoord(37.7869802, -122.4471197),
                                new GeoCoord(37.7664102, -122.4590777),
                                new GeoCoord(37.7710682, -122.4137097)
                        ),
                        ImmutableList.<GeoCoord>of(
                                new GeoCoord(37.747976, -122.490025),
                                new GeoCoord(37.731550, -122.503758),
                                new GeoCoord(37.725440, -122.452603)
                        )
                ),
                9
        );

        assertTrue(hexagons.size() > 1000);
    }

    @Test
    public void testPolyfillKnownHoles() {
        List<Long> inputHexagons = h3.kRing(0x85283083fffffffL, 2);
        inputHexagons.remove(0x8528308ffffffffL);
        inputHexagons.remove(0x85283097fffffffL);
        inputHexagons.remove(0x8528309bfffffffL);

        List<List<GeoCoord>> geo = h3.h3SetToMultiPolygon(inputHexagons, true).get(0);

        List<GeoCoord> outline = geo.remove(0); // geo is now holes

        List<Long> outputHexagons = h3.polyfill(outline, geo, 5);

        assertEquals(ImmutableSet.copyOf(inputHexagons), ImmutableSet.copyOf(outputHexagons));
    }

    @Test
    public void testH3SetToMultiPolygonEmpty() {
        assertEquals(0, h3.h3SetToMultiPolygon(new ArrayList<Long>(), false).size());
    }

    @Test
    public void testH3SetToMultiPolygonSingle() {
        long testIndex = 0x89283082837ffffL;

        List<GeoCoord> actualBounds = h3.h3ToGeoBoundary(testIndex);
        List<List<List<GeoCoord>>> multiBounds = h3.h3SetToMultiPolygon(ImmutableList.of(testIndex), true);

        // This is tricky, because output in an order starting from any vertex
        // would also be correct, but that's difficult to assert and there's
        // value in being specific here

        assertEquals(1, multiBounds.size());
        assertEquals(1, multiBounds.get(0).size());
        assertEquals(actualBounds.size() + 1, multiBounds.get(0).get(0).size());

        int[] expectedIndices = {0, 1, 2, 3, 4, 5, 0};

        for (int i = 0; i < actualBounds.size(); i++) {
            assertEquals(actualBounds.get(expectedIndices[i]).lat, multiBounds.get(0).get(0).get(i).lat, EPSILON);
            assertEquals(actualBounds.get(expectedIndices[i]).lng, multiBounds.get(0).get(0).get(i).lng, EPSILON);
        }
    }

    @Test
    public void testH3SetToMultiPolygonSingleNonGeoJson() {
        long testIndex = 0x89283082837ffffL;

        List<GeoCoord> actualBounds = h3.h3ToGeoBoundary(testIndex);
        List<List<List<GeoCoord>>> multiBounds = h3.h3SetToMultiPolygon(ImmutableList.of(testIndex), false);

        // This is tricky, because output in an order starting from any vertex
        // would also be correct, but that's difficult to assert and there's
        // value in being specific here

        assertEquals(1, multiBounds.size());
        assertEquals(1, multiBounds.get(0).size());
        assertEquals(actualBounds.size(), multiBounds.get(0).get(0).size());

        int[] expectedIndices = {0, 1, 2, 3, 4, 5};

        for (int i = 0; i < actualBounds.size(); i++) {
            assertEquals(actualBounds.get(expectedIndices[i]).lat, multiBounds.get(0).get(0).get(i).lat, EPSILON);
            assertEquals(actualBounds.get(expectedIndices[i]).lng, multiBounds.get(0).get(0).get(i).lng, EPSILON);
        }
    }

    @Test
    public void testH3SetToMultiPolygonContiguous2() {
        long testIndex = 0x89283082837ffffL;
        long testIndex2 = 0x89283082833ffffL;

        List<GeoCoord> actualBounds = h3.h3ToGeoBoundary(testIndex);
        List<GeoCoord> actualBounds2 = h3.h3ToGeoBoundary(testIndex2);

        // Note this is different than the h3core-js bindings, in that it uses GeoJSON (possible bug)
        List<List<List<GeoCoord>>> multiBounds = h3.h3SetToMultiPolygon(ImmutableList.of(testIndex, testIndex2), false);

        assertEquals(1, multiBounds.size());
        assertEquals(1, multiBounds.get(0).size());
        assertEquals(10, multiBounds.get(0).get(0).size());

        assertEquals(actualBounds.get(1).lat, multiBounds.get(0).get(0).get(0).lat, EPSILON);
        assertEquals(actualBounds.get(2).lat, multiBounds.get(0).get(0).get(1).lat, EPSILON);
        assertEquals(actualBounds.get(3).lat, multiBounds.get(0).get(0).get(2).lat, EPSILON);
        assertEquals(actualBounds.get(4).lat, multiBounds.get(0).get(0).get(3).lat, EPSILON);
        assertEquals(actualBounds.get(5).lat, multiBounds.get(0).get(0).get(4).lat, EPSILON);
        assertEquals(actualBounds2.get(4).lat, multiBounds.get(0).get(0).get(5).lat, EPSILON);
        assertEquals(actualBounds2.get(5).lat, multiBounds.get(0).get(0).get(6).lat, EPSILON);
        assertEquals(actualBounds2.get(0).lat, multiBounds.get(0).get(0).get(7).lat, EPSILON);
        assertEquals(actualBounds2.get(1).lat, multiBounds.get(0).get(0).get(8).lat, EPSILON);
        assertEquals(actualBounds2.get(2).lat, multiBounds.get(0).get(0).get(9).lat, EPSILON);
        assertEquals(actualBounds.get(1).lng, multiBounds.get(0).get(0).get(0).lng, EPSILON);
        assertEquals(actualBounds.get(2).lng, multiBounds.get(0).get(0).get(1).lng, EPSILON);
        assertEquals(actualBounds.get(3).lng, multiBounds.get(0).get(0).get(2).lng, EPSILON);
        assertEquals(actualBounds.get(4).lng, multiBounds.get(0).get(0).get(3).lng, EPSILON);
        assertEquals(actualBounds.get(5).lng, multiBounds.get(0).get(0).get(4).lng, EPSILON);
        assertEquals(actualBounds2.get(4).lng, multiBounds.get(0).get(0).get(5).lng, EPSILON);
        assertEquals(actualBounds2.get(5).lng, multiBounds.get(0).get(0).get(6).lng, EPSILON);
        assertEquals(actualBounds2.get(0).lng, multiBounds.get(0).get(0).get(7).lng, EPSILON);
        assertEquals(actualBounds2.get(1).lng, multiBounds.get(0).get(0).get(8).lng, EPSILON);
        assertEquals(actualBounds2.get(2).lng, multiBounds.get(0).get(0).get(9).lng, EPSILON);
    }

    @Test
    public void testH3SetToMultiPolygonNonContiguous2() {
        long testIndex = 0x89283082837ffffL;
        long testIndex2 = 0x8928308280fffffL;

        List<List<List<GeoCoord>>> multiBounds = h3.h3SetToMultiPolygon(ImmutableList.of(testIndex, testIndex2), false);

        assertEquals(2, multiBounds.size());
        assertEquals(1, multiBounds.get(0).size());
        assertEquals(6, multiBounds.get(0).get(0).size());
        assertEquals(1, multiBounds.get(1).size());
        assertEquals(6, multiBounds.get(1).get(0).size());
    }

    @Test
    public void testH3SetToMultiPolygonHole() {
        // Six hexagons in a ring around a hole
        List<List<List<GeoCoord>>> multiBounds = h3.h3AddressSetToMultiPolygon(ImmutableList.of(
                "892830828c7ffff", "892830828d7ffff", "8928308289bffff",
                "89283082813ffff", "8928308288fffff", "89283082883ffff"
        ), false);

        assertEquals(1, multiBounds.size());
        assertEquals(2, multiBounds.get(0).size());
        assertEquals(6 * 3, multiBounds.get(0).get(0).size());
        assertEquals(6, multiBounds.get(0).get(1).size());
    }

    @Test
    public void testH3SetToMultiPolygonLarge() {
        int numHexes = 20000;

        List<String> addresses = new ArrayList<>(numHexes);
        for (int i = 0; i < numHexes; i++) {
            addresses.add(h3.geoToH3Address(0, i * 0.01, 15));
        }

        List<List<List<GeoCoord>>> multiBounds = h3.h3AddressSetToMultiPolygon(addresses, false);

        assertEquals(numHexes, multiBounds.size());
        for (int i = 0; i < multiBounds.size(); i++) {
            assertEquals(1, multiBounds.get(i).size());
            assertEquals(6, multiBounds.get(i).get(0).size());
        }
    }

    @Test
    public void testHexRing() throws PentagonEncounteredException {
        List<String> hexagons = h3.hexRing("8928308280fffff", 1);

        assertEquals(6, hexagons.size());

        assertTrue(hexagons.contains("8928308280bffff"));
        assertTrue(hexagons.contains("89283082807ffff"));
        assertTrue(hexagons.contains("89283082877ffff"));
        assertTrue(hexagons.contains("89283082803ffff"));
        assertTrue(hexagons.contains("89283082873ffff"));
        assertTrue(hexagons.contains("8928308283bffff"));
    }

    @Test
    public void testHexRing2() throws PentagonEncounteredException {
        List<String> hexagons = h3.hexRing("8928308280fffff", 2);

        assertEquals(12, hexagons.size());

        assertTrue(hexagons.contains("89283082813ffff"));
        assertTrue(hexagons.contains("89283082817ffff"));
        assertTrue(hexagons.contains("8928308281bffff"));
        assertTrue(hexagons.contains("89283082863ffff"));
        assertTrue(hexagons.contains("89283082823ffff"));
        assertTrue(hexagons.contains("8928308287bffff"));
        assertTrue(hexagons.contains("89283082833ffff"));
        assertTrue(hexagons.contains("8928308282bffff"));
        assertTrue(hexagons.contains("89283082857ffff"));
        assertTrue(hexagons.contains("892830828abffff"));
        assertTrue(hexagons.contains("89283082847ffff"));
        assertTrue(hexagons.contains("89283082867ffff"));
    }

    @Test
    public void testHexRingLarge() throws PentagonEncounteredException {
        int k = 50;
        List<String> hexagons = h3.hexRing("8928308280fffff", k);

        int expectedCount = 50 * 6;

        assertEquals(expectedCount, hexagons.size());
    }

    @Test(expected = PentagonEncounteredException.class)
    public void testHexRingPentagon() throws PentagonEncounteredException {
        h3.hexRing("821c07fffffffff", 1);
    }

    @Test(expected = PentagonEncounteredException.class)
    public void testHexRingAroundPentagon() throws PentagonEncounteredException {
        h3.hexRing("821c37fffffffff", 2);
    }

    @Test
    public void testHexRingSingle() throws PentagonEncounteredException {
        String origin = "8928308280fffff";
        List<String> hexagons = h3.hexRing(origin, 0);

        assertEquals(1, hexagons.size());
        assertEquals("8928308280fffff", hexagons.get(0));
    }

    @Test
    public void testHostileInput() {
        assertNotEquals(0, h3.geoToH3(-987654321, 987654321, 5));
        assertNotEquals(0, h3.geoToH3(987654321, -987654321, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostileGeoToH3NaN() {
        h3.geoToH3(Double.NaN, Double.NaN, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostileGeoToH3PositiveInfinity() {
        h3.geoToH3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostileGeoToH3NegativeInfinity() {
        h3.geoToH3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostileInputNegativeRes() {
        h3.geoToH3(0, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostileInputLargeRes() {
        h3.geoToH3(0, 0, 1000);
    }

    @Test
    public void testHostileInputLatLng() {
        try {
            // Don't crash
            h3.geoToH3(1e45, 1e45, 0);
        } catch (IllegalArgumentException e) {
            // Acceptable result
        }
    }

    @Test
    public void testHostileInputMaximum() {
        try {
            // Don't crash
            h3.geoToH3(Double.MAX_VALUE, Double.MAX_VALUE, 0);
        } catch (IllegalArgumentException e) {
            // Acceptable result
        }
    }

    @Test
    public void testH3GetResolution() {
        assertEquals(0, h3.h3GetResolution(0x8029fffffffffffL));
        assertEquals(15, h3.h3GetResolution(0x8f28308280f18f2L));
        assertEquals(14, h3.h3GetResolution(0x8e28308280f18f7L));
        assertEquals(9, h3.h3GetResolution("8928308280fffff"));

        // These are invalid, we're checking for not crashing.
        assertEquals(0, h3.h3GetResolution(0));
        assertEquals(15, h3.h3GetResolution(0xffffffffffffffffL));
    }

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
    public void testH3IsResClassIII() {
        String r0 = h3.geoToH3Address(0, 0, 0);
        String r1 = h3.geoToH3Address(10, 0, 1);
        String r2 = h3.geoToH3Address(0, 10, 2);
        String r3 = h3.geoToH3Address(10, 10, 3);

        assertFalse(h3.h3IsResClassIII(r0));
        assertTrue(h3.h3IsResClassIII(r1));
        assertFalse(h3.h3IsResClassIII(r2));
        assertTrue(h3.h3IsResClassIII(r3));
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

    @Test
    public void testH3GetBaseCell() {
        assertEquals(20, h3.h3GetBaseCell("8f28308280f18f2"));
        assertEquals(20, h3.h3GetBaseCell(0x8f28308280f18f2L));
        assertEquals(14, h3.h3GetBaseCell("821c07fffffffff"));
        assertEquals(14, h3.h3GetBaseCell(0x821c07fffffffffL));
    }

    @Test
    public void testH3IsPentagon() {
        assertFalse(h3.h3IsPentagon("8f28308280f18f2"));
        assertFalse(h3.h3IsPentagon(0x8f28308280f18f2L));
        assertTrue(h3.h3IsPentagon("821c07fffffffff"));
        assertTrue(h3.h3IsPentagon(0x821c07fffffffffL));
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
        for (int i = 0; i < 9; i++) {
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

    @Test(expected = DistanceUndefinedException.class)
    public void testH3DistanceFailedDistance() throws DistanceUndefinedException {
        // This fails because of a limitation in the H3 core library.
        // It cannot find distances when spanning more than one base cell.
        // Expected correct result is 2.
        h3.h3Distance("8029fffffffffff", "8079fffffffffff");
    }

    @Test(expected = DistanceUndefinedException.class)
    public void testH3DistanceFailedResolution() throws DistanceUndefinedException {
        // Cannot find distances when the indexes are not comparable (different resolutions)
        h3.h3Distance("81283ffffffffff", "8029fffffffffff");
    }

    @Test(expected = DistanceUndefinedException.class)
    public void testH3DistanceFailedPentagonDistortion() throws DistanceUndefinedException {
        // This fails because of a limitation in the H3 core library.
        // It cannot find distances from opposite sides of a pentagon.
        // Expected correct result is 9.
        h3.h3Distance("821c37fffffffff", "822837fffffffff");
    }

    @Test
    public void testH3Distance() throws DistanceUndefinedException {
        // Resolution 0 to some neighbors
        assertEquals(0, h3.h3Distance("8029fffffffffff", "8029fffffffffff"));
        assertEquals(1, h3.h3Distance("8029fffffffffff", "801dfffffffffff"));
        assertEquals(1, h3.h3Distance("8029fffffffffff", "8037fffffffffff"));

        // Resolution 1 from a base cell onto a pentagon
        assertEquals(2, h3.h3Distance("81283ffffffffff", "811d7ffffffffff"));
        assertEquals(2, h3.h3Distance("81283ffffffffff", "811cfffffffffff"));
        assertEquals(3, h3.h3Distance("81283ffffffffff", "811c3ffffffffff"));
        // Opposite sides of a pentagon
        assertEquals(4, h3.h3Distance("81283ffffffffff", "811dbffffffffff"));

        // Resolution 5 within the same base cell
        assertEquals(0, h3.h3Distance("85283083fffffff", "85283083fffffff"));
        assertEquals(1, h3.h3Distance("85283083fffffff", "85283093fffffff"));
        assertEquals(2, h3.h3Distance("85283083fffffff", "8528342bfffffff"));
        assertEquals(3, h3.h3Distance("85283083fffffff", "85283477fffffff"));
        assertEquals(4, h3.h3Distance("85283083fffffff", "85283473fffffff"));
        assertEquals(5, h3.h3Distance("85283083fffffff", "85283447fffffff"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExperimentalH3ToLocalIjNoncomparable() throws PentagonEncounteredException, LocalIjUndefinedException {
        h3.experimentalH3ToLocalIj("832830fffffffff", "822837fffffffff");
    }

    @Test(expected = LocalIjUndefinedException.class)
    public void testExperimentalH3ToLocalIjTooFar() throws PentagonEncounteredException, LocalIjUndefinedException {
        h3.experimentalH3ToLocalIj("822a17fffffffff", "822837fffffffff");
    }

    @Test(expected = PentagonEncounteredException.class)
    public void testExperimentalH3ToLocalIjPentagonDistortion() throws PentagonEncounteredException, LocalIjUndefinedException {
        h3.experimentalH3ToLocalIj("81283ffffffffff", "811cbffffffffff");
    }

    @Test
    public void testExperimentalH3ToLocalIjPentagon() throws PentagonEncounteredException, LocalIjUndefinedException {
        final String origin = "811c3ffffffffff";
        assertEquals(new CoordIJ(0, 0), h3.experimentalH3ToLocalIj(origin, origin));
        assertEquals(new CoordIJ(1, 0), h3.experimentalH3ToLocalIj(origin, "811d3ffffffffff"));
        assertEquals(new CoordIJ(-1, 0), h3.experimentalH3ToLocalIj(origin, "811cfffffffffff"));
    }

    @Test
    public void testExperimentalH3ToLocalIjHexagons() throws PentagonEncounteredException, LocalIjUndefinedException {
        final String origin = "8828308281fffff";
        assertEquals(new CoordIJ(392, 336), h3.experimentalH3ToLocalIj(origin, origin));
        assertEquals(new CoordIJ(387, 336), h3.experimentalH3ToLocalIj(origin, "88283080c3fffff"));
        assertEquals(new CoordIJ(392, -14), h3.experimentalH3ToLocalIj(origin, "8828209581fffff"));
    }

    @Test
    public void testExperimentalLocalIjToH3Pentagon() throws LocalIjUndefinedException {
        final String origin = "811c3ffffffffff";
        assertEquals(origin, h3.experimentalLocalIjToH3(origin, new CoordIJ(0, 0)));
        assertEquals("811d3ffffffffff", h3.experimentalLocalIjToH3(origin, new CoordIJ(1, 0)));
        assertEquals("811cfffffffffff", h3.experimentalLocalIjToH3(origin, new CoordIJ(-1, 0)));
    }

    @Test(expected = LocalIjUndefinedException.class)
    public void testExperimentalLocalIjToH3TooFar() throws LocalIjUndefinedException {
        h3.experimentalLocalIjToH3("8049fffffffffff", new CoordIJ(2, 0));
    }

    @Test
    public void testH3Line() throws LineUndefinedException, DistanceUndefinedException {
        for (int res = 0; res < 12; res++) {
            String origin = h3.geoToH3Address(37.5, -122, res);
            String destination = h3.geoToH3Address(25, -120, res);

            List<String> line = h3.h3Line(origin, destination);
            int distance = h3.h3Distance(origin, destination);

            // Need to add 1 to account for the origin as well
            assertEquals("Distance matches expected", distance + 1, line.size());

            for (int i = 1; i < line.size(); i++) {
                assertTrue("Every index in the line is a neighbor of the previous", h3.h3IndexesAreNeighbors(line.get(i - 1), line.get(i)));
            }

            assertTrue("Line contains start", line.contains(origin));
            assertTrue("Line contains destination", line.contains(destination));
        }
    }

    @Test(expected = LineUndefinedException.class)
    public void testH3LineFailed() throws LineUndefinedException {
        long origin = h3.geoToH3(37.5, -122, 9);
        long destination = h3.geoToH3(37.5, -122, 10);

        h3.h3Line(origin, destination);
    }
}
