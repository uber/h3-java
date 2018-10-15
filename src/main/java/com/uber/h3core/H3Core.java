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

import com.uber.h3core.exceptions.DistanceUndefinedException;
import com.uber.h3core.exceptions.LocalIjUndefinedException;
import com.uber.h3core.exceptions.PentagonEncounteredException;
import com.uber.h3core.util.CoordIJ;
import com.uber.h3core.util.GeoCoord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * H3Core provides all functions of the H3 API.
 *
 * <p>This class is thread safe and can be used as a singleton.</p>
 */
public class H3Core {
    // These constants are from h3api.h and h3Index.h
    /**
     * Maximum number of vertices for an H3 index
     */
    private static final int MAX_CELL_BNDRY_VERTS = 10;

    // Constants for the resolution bits in an H3 index.
    private static final long H3_RES_OFFSET = 52L;
    private static final long H3_RES_MASK = 0xfL << H3_RES_OFFSET;
    private static final long H3_RES_MASK_NEGATIVE = ~H3_RES_MASK;
    /**
     * Mask for the indexing digits in an H3 index.
     *
     * <p>The digits are offset by 0, so no shift is needed in the constant.
     */
    private static final long H3_DIGIT_MASK = 0x1fffffffffffL;

    /**
     * Native implementation of the H3 library.
     */
    private final NativeMethods h3Api;

    /**
     * Create by unpacking the H3 native library to disk and loading it.
     * The library will attempt to detect the correct operating system
     * and architecture of native library to unpack.
     *
     * @throws SecurityException Loading the library was not allowed by the
     *                           SecurityManager.
     * @throws UnsatisfiedLinkError The library could not be loaded
     * @throws IOException The library could not be extracted to disk.
     */
    public static H3Core newInstance() throws IOException {
        NativeMethods h3Api = H3CoreLoader.loadNatives();
        return new H3Core(h3Api);
    }

    /**
     * Create by unpacking the H3 native library to disk and loading it.
     * The library will attempt to extract the native library matching
     * the given arguments to disk.
     *
     * @throws SecurityException Loading the library was not allowed by the
     *                           SecurityManager.
     * @throws UnsatisfiedLinkError The library could not be loaded
     * @throws IOException The library could not be extracted to disk.
     */
    public static H3Core newInstance(H3CoreLoader.OperatingSystem os, String arch) throws IOException {
        NativeMethods h3Api = H3CoreLoader.loadNatives(os, arch);
        return new H3Core(h3Api);
    }

    /**
     * Create by using the H3 native library already installed on the system.
     *
     * @throws SecurityException The library could not be loaded
     * @throws UnsatisfiedLinkError The library could not be loaded
     */
    public static H3Core newSystemInstance() {
        NativeMethods h3Api = H3CoreLoader.loadSystemNatives();
        return new H3Core(h3Api);
    }

    /**
     * Construct with the given NativeMethods, from {@link H3CoreLoader}.
     */
    private H3Core(NativeMethods h3Api) {
        this.h3Api = h3Api;
    }

    /**
     * Returns true if this is a valid H3 index.
     */
    public boolean h3IsValid(long h3) {
        return h3Api.h3IsValid(h3);
    }

    /**
     * Returns true if this is a valid H3 index.
     */
    public boolean h3IsValid(String h3Address) {
        return h3IsValid(stringToH3(h3Address));
    }

    /**
     * Returns the base cell number for this index.
     */
    public int h3GetBaseCell(long h3) {
        return h3Api.h3GetBaseCell(h3);
    }

    /**
     * Returns the base cell number for this index.
     */
    public int h3GetBaseCell(String h3Address) {
        return h3GetBaseCell(stringToH3(h3Address));
    }

    /**
     * Returns <code>true</code> if this index is one of twelve pentagons per resolution.
     */
    public boolean h3IsPentagon(long h3) {
        return h3Api.h3IsPentagon(h3);
    }

    /**
     * Returns <code>true</code> if this index is one of twelve pentagons per resolution.
     */
    public boolean h3IsPentagon(String h3Address) {
        return h3IsPentagon(stringToH3(h3Address));
    }

    /**
     * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
     *
     * @param lat Latitude in degrees.
     * @param lng Longitude in degrees.
     * @param res Resolution, 0 &lt;= res &lt;= 15
     * @return The H3 index.
     * @throws IllegalArgumentException latitude, longitude, or resolution are out of range.
     */
    public long geoToH3(double lat, double lng, int res) {
        checkResolution(res);
        long result = h3Api.geoToH3(toRadians(lat), toRadians(lng), res);
        if (result == 0) {
            // Must be latitude or longitude that's wrong, since we already
            // check the resolution before calling geoToH3.
            throw new IllegalArgumentException("Latitude or longitude were invalid.");
        }
        return result;
    }

    /**
     * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
     *
     * @param lat Latitude in degrees.
     * @param lng Longitude in degrees.
     * @param res Resolution, 0 &lt;= res &lt;= 15
     * @return The H3 index.
     * @throws IllegalArgumentException Latitude, longitude, or resolution is out of range.
     */
    public String geoToH3Address(double lat, double lng, int res) {
        return h3ToString(geoToH3(lat, lng, res));
    }

    /**
     * Find the latitude, longitude (both in degrees) center point of the cell.
     */
    public GeoCoord h3ToGeo(long h3) {
        double[] coords = new double[2];
        h3Api.h3ToGeo(h3, coords);
        GeoCoord out = new GeoCoord(
                toDegrees(coords[0]),
                toDegrees(coords[1])
        );
        return out;
    }

    /**
     * Find the latitude, longitude (degrees) center point of the cell.
     */
    public GeoCoord h3ToGeo(String h3Address) {
        return h3ToGeo(stringToH3(h3Address));
    }

    /**
     * Find the cell boundary in latitude, longitude (degrees) coordinates for the cell
     */
    public List<GeoCoord> h3ToGeoBoundary(long h3) {
        double[] verts = new double[MAX_CELL_BNDRY_VERTS * 2];
        int numVerts = h3Api.h3ToGeoBoundary(h3, verts);
        List<GeoCoord> out = new ArrayList<>(numVerts);
        for (int i = 0; i < numVerts; i++) {
            GeoCoord coord = new GeoCoord(
                    toDegrees(verts[i * 2]),
                    toDegrees(verts[(i * 2) + 1])
            );
            out.add(coord);
        }
        return out;
    }

    /**
     * Find the cell boundary in latitude, longitude (degrees) coordinates for the cell
     */
    public List<GeoCoord> h3ToGeoBoundary(String h3Address) {
        return h3ToGeoBoundary(stringToH3(h3Address));
    }

    /**
     * Neighboring indexes in all directions.
     *
     * @param h3Address Origin index
     * @param k         Number of rings around the origin
     */
    public List<String> kRing(String h3Address, int k) {
        return h3ToStringList(kRing(stringToH3(h3Address), k));
    }

    /**
     * Neighboring indexes in all directions.
     *
     * @param h3Address Origin index
     * @param k         Number of rings around the origin
     * @return List of {@link #kRing(String, int)} results.
     */
    public List<List<String>> kRings(String h3Address, int k) {
        List<List<String>> result = new ArrayList<>(k + 1);
        result.add(Collections.singletonList(h3Address));
        for (int i = 1; i <= k; ++i) {
            result.add(kRing(h3Address, i));
        }
        return result;
    }

    /**
     * Neighboring indexes in all directions.
     *
     * @param h3 Origin index
     * @param k  Number of rings around the origin
     */
    public List<Long> kRing(long h3, int k) {
        int sz = h3Api.maxKringSize(k);

        long[] out = new long[sz];

        h3Api.kRing(h3, k, out);

        return nonZeroLongArrayToList(out);
    }

    /**
     * Neighboring indexes in all directions, ordered by distance from the origin index.
     *
     * @param h3Address Origin index
     * @param k         Number of rings around the origin
     * @return A list of rings, each of which is a list of addresses. The rings are in order
     *         from closest to origin to farthest.
     */
    public List<List<String>> kRingDistances(String h3Address, int k) {
        List<List<Long>> rings = kRingDistances(stringToH3(h3Address), k);

        return rings.stream()
                .map(this::h3ToStringList)
                .collect(Collectors.toList());
    }

    /**
     * Neighboring indexes in all directions, ordered by distance from the origin index.
     *
     * @param h3 Origin index
     * @param k  Number of rings around the origin
     * @return A list of rings, each of which is a list of addresses. The rings are in order
     *         from closest to origin to farthest.
     */
    public List<List<Long>> kRingDistances(long h3, int k) {
        int sz = h3Api.maxKringSize(k);

        long[] out = new long[sz];
        int[] distances = new int[sz];

        h3Api.kRingDistances(h3, k, out, distances);

        List<List<Long>> ret = new ArrayList<>(k + 1);

        for (int i = 0; i <= k; i++) {
            ret.add(new ArrayList<>());
        }

        for (int i = 0; i < sz; i++) {
            long nextH3 = out[i];
            if (nextH3 != 0) {
                ret.get(distances[i])
                        .add(nextH3);
            }
        }

        return ret;
    }

    /**
     * Returns in order neighbor traversal.
     *
     * @param h3Address Origin hexagon index
     * @param k         Number of rings around the origin
     * @return A list of rings, each of which is a list of addresses. The rings are in order
     *         from closest to origin to farthest.
     * @throws PentagonEncounteredException A pentagon was encountered while iterating the rings
     */
    public List<List<String>> hexRange(String h3Address, int k) throws PentagonEncounteredException {
        List<List<Long>> rings = hexRange(stringToH3(h3Address), k);

        return rings.stream()
                .map(this::h3ToStringList)
                .collect(Collectors.toList());
    }

    /**
     * Returns in order neighbor traversal.
     *
     * @param h3 Origin hexagon index
     * @param k  Number of rings around the origin
     * @return A list of rings, each of which is a list of addresses. The rings are in order
     *         from closest to origin to farthest.
     * @throws PentagonEncounteredException A pentagon was encountered while iterating the rings
     */
    public List<List<Long>> hexRange(long h3, int k) throws PentagonEncounteredException {
        int sz = h3Api.maxKringSize(k);

        long[] out = new long[sz];

        if (h3Api.hexRange(h3, k, out) != 0) {
            throw new PentagonEncounteredException("A pentagon was encountered while computing hexRange.");
        }

        List<List<Long>> ret = new ArrayList<>(k + 1);

        List<Long> ring = null;
        int currentK = 0;
        int nextRing = 0;

        for (int i = 0; i < sz; i++) {
            // Check if we've reached the index of the next ring.
            if (i == nextRing) {
                ring = new ArrayList<>();
                ret.add(ring);

                // Determine the start index of the next ring.
                // k=0 is a special case of size 1.
                if (currentK == 0) {
                    nextRing = 1;
                } else {
                    nextRing += (6 * currentK);
                }
                currentK++;
            }

            long h = out[i];
            ring.add(h);
        }

        return ret;
    }

    /**
     * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
     *
     * @param h3Address Origin index
     * @param k         Number of rings around the origin
     * @return All indexes <code>k</code> away from the origin
     * @throws PentagonEncounteredException A pentagon or pentagonal distortion was encountered.
     */
    public List<String> hexRing(String h3Address, int k) throws PentagonEncounteredException {
        return h3ToStringList(hexRing(stringToH3(h3Address), k));
    }

    /**
     * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
     *
     * @param h3 Origin index
     * @param k  Number of rings around the origin
     * @return All indexes <code>k</code> away from the origin
     * @throws PentagonEncounteredException A pentagon or pentagonal distortion was encountered.
     */
    public List<Long> hexRing(long h3, int k) throws PentagonEncounteredException {
        int sz = k == 0 ? 1 : 6 * k;

        long[] out = new long[sz];

        if (h3Api.hexRing(h3, k, out) != 0) {
            throw new PentagonEncounteredException("A pentagon was encountered while computing hexRing.");
        }

        return nonZeroLongArrayToList(out);
    }

    /**
     * Returns the distance between <code>a</code> and <code>b</code>.
     * This is the grid distance, or distance expressed in number of H3 cells.
     *
     * <p>In some cases H3 cannot compute the distance between two indexes.
     * This can happen because:
     * <ul>
     *     <li>The indexes are not comparable (difference resolutions, etc)</li>
     *     <li>The distance is greater than the H3 core library supports</li>
     *     <li>The H3 library does not support finding the distance between
     *     the two cells, because of pentagonal distortion.</li>
     * </ul>
     *
     * @param a An H3 index
     * @param b Another H3 index
     * @return Distance between the two in grid cells
     * @throws DistanceUndefinedException H3 cannot compute the distance.
     */
    public int h3Distance(String a, String b) throws DistanceUndefinedException {
        return h3Distance(stringToH3(a), stringToH3(b));
    }

    /**
     * Returns the distance between <code>a</code> and <code>b</code>.
     * This is the grid distance, or distance expressed in number of H3 cells.
     *
     * <p>In some cases H3 cannot compute the distance between two indexes.
     * This can happen because:
     * <ul>
     *     <li>The indexes are not comparable (difference resolutions, etc)</li>
     *     <li>The distance is greater than the H3 core library supports</li>
     *     <li>The H3 library does not support finding the distance between
     *     the two cells, because of pentagonal distortion.</li>
     * </ul>
     *
     * @param a An H3 index
     * @param b Another H3 index
     * @return Distance between the two in grid cells
     * @throws DistanceUndefinedException H3 cannot compute the distance.
     */
    public int h3Distance(long a, long b) throws DistanceUndefinedException {
        final int distance = h3Api.h3Distance(a, b);

        if (distance < 0) {
            throw new DistanceUndefinedException("Distance not defined between the two indexes.");
        }

        return distance;
    }

    /**
     * Converts <code>h3</code> to IJ coordinates in a local coordinate space defined by
     * <code>origin</code>.
     *
     * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
     * distortion. IJ coordinates are only comparable if they came from the same origin.
     *
     * <p>This function is experimental, and its output is not guaranteed
     * to be compatible across different versions of H3.
     *
     * @param origin Anchoring index for the local coordinate space.
     * @param h3 Index to find the coordinates of.
     * @return Coordinates for <code>h3</code> in the local coordinate space.
     * @throws IllegalArgumentException The two indexes are not comparable.
     * @throws PentagonEncounteredException The two indexes are separated by pentagonal distortion.
     * @throws LocalIjUndefinedException The two indexes are too far apart.
     */
    public CoordIJ experimentalH3ToLocalIj(long origin, long h3) throws PentagonEncounteredException, LocalIjUndefinedException {
        final int[] coords = new int[2];
        final int result = h3Api.experimentalH3ToLocalIj(origin, h3, coords);
        // The definition of these cases is in experimentalH3ToLocalIj in localij.c in the C library.
        // 0 is success, anything else is a failure of some kind.
        switch (result) {
            case 0:
                return new CoordIJ(coords[0], coords[1]);
            case 1:
                throw new IllegalArgumentException("Incompatible origin and index.");
            default:
            case 2:
                throw new LocalIjUndefinedException("Local IJ coordinates undefined for this origin and index pair. The index may be too far from the origin.");
            case 3:
            case 4:
            case 5:
                throw new PentagonEncounteredException("Encountered possible pentagon distortion");
        }
    }

    /**
     * Converts <code>h3Address</code> to IJ coordinates in a local coordinate space defined by
     * <code>originAddress</code>.
     *
     * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
     * distortion. IJ coordinates are only comparable if they came from the same origin.
     *
     * <p>This function is experimental, and its output is not guaranteed
     * to be compatible across different versions of H3.
     *
     * @param originAddress Anchoring index for the local coordinate space.
     * @param h3Address Index to find the coordinates of.
     * @return Coordinates for <code>h3</code> in the local coordinate space.
     * @throws IllegalArgumentException The two indexes are not comparable.
     * @throws PentagonEncounteredException The two indexes are separated by pentagonal distortion.
     * @throws LocalIjUndefinedException The two indexes are too far apart.
     */
    public CoordIJ experimentalH3ToLocalIj(String originAddress, String h3Address) throws PentagonEncounteredException, LocalIjUndefinedException {
        return experimentalH3ToLocalIj(stringToH3(originAddress), stringToH3(h3Address));
    }

    /**
     * Converts the IJ coordinates to an index, using a local IJ coordinate space anchored by
     * <code>origin</code>.
     *
     * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
     * distortion. IJ coordinates are only comparable if they came from the same origin.
     *
     * <p>This function is experimental, and its output is not guaranteed
     * to be compatible across different versions of H3.
     *
     * @param origin Anchoring index for the local coordinate space.
     * @param ij Coordinates in the local IJ coordinate space.
     * @return Index represented by <code>ij</code>
     * @throws LocalIjUndefinedException No index is defined at the given location, for example
     * because the coordinates are too far away from the origin, or pentagon distortion is encountered.
     */
    public long experimentalLocalIjToH3(long origin, CoordIJ ij) throws LocalIjUndefinedException {
        final long result = h3Api.experimentalLocalIjToH3(origin, ij.i, ij.j);
        if (result == 0) {
            throw new LocalIjUndefinedException("Index not defined for this origin and IJ coordinates pair. IJ coordinates may be too far from origin, or pentagon distortion was encountered.");
        }
        return result;
    }

    /**
     * Converts the IJ coordinates to an index, using a local IJ coordinate space anchored by
     * <code>origin</code>.
     *
     * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
     * distortion. IJ coordinates are only comparable if they came from the same origin.
     *
     * <p>This function is experimental, and its output is not guaranteed
     * to be compatible across different versions of H3.
     *
     * @param originAddress Anchoring index for the local coordinate space.
     * @param ij Coordinates in the local IJ coordinate space.
     * @return Index represented by <code>ij</code>
     * @throws LocalIjUndefinedException No index is defined at the given location, for example
     * because the coordinates are too far away from the origin, or pentagon distortion is encountered.
     */
    public String experimentalLocalIjToH3(String originAddress, CoordIJ ij) throws LocalIjUndefinedException {
        return h3ToString(experimentalLocalIjToH3(stringToH3(originAddress), ij));
    }

    /**
     * Finds indexes within the given geofence.
     *
     * @param points Outline geofence
     * @param holes Geofences of any internal holes
     * @param res Resolution of the desired indexes
     */
    public List<String> polyfillAddress(List<GeoCoord> points, List<List<GeoCoord>> holes, int res) {
        return h3ToStringList(polyfill(points, holes, res));
    }

    /**
     * Finds indexes within the given geofence.
     *
     * @param points Outline geofence
     * @param holes Geofences of any internal holes
     * @param res Resolution of the desired indexes
     * @throws IllegalArgumentException Invalid resolution
     */
    public List<Long> polyfill(List<GeoCoord> points, List<List<GeoCoord>> holes, int res) {
        checkResolution(res);

        // pack the data for use by the polyfill JNI call
        double[] verts = new double[points.size() * 2];
        packGeofenceVertices(verts, points, 0);
        int[] holeSizes = new int[0];
        double[] holeVerts = new double[0];
        if (holes != null) {
            holeSizes = new int[holes.size()];
            int totalSize = 0;
            for (int i = 0; i < holes.size(); i++) {
                totalSize += holes.get(i).size() * 2;
                // Note we are storing the number of doubles
                holeSizes[i] = holes.get(i).size() * 2;
            }
            holeVerts = new double[totalSize];
            int offset = 0;
            for (int i = 0; i < holes.size(); i++) {
                offset = packGeofenceVertices(holeVerts, holes.get(i), offset);
            }
        }

        int sz = h3Api.maxPolyfillSize(verts, holeSizes, holeVerts, res);

        long[] results = new long[sz];

        h3Api.polyfill(verts, holeSizes, holeVerts, res, results);

        return nonZeroLongArrayToList(results);
    }

    /**
     * Interleave the pairs in the given double array.
     *
     * @return Next offset to begin filling from
     */
    private static int packGeofenceVertices(double[] arr, List<GeoCoord> original, int offset) {
        assert arr.length >= (original.size() * 2) + offset;

        for (int i = 0; i < original.size(); i++) {
            GeoCoord coord = original.get(i);

            arr[(i * 2) + offset] = toRadians(coord.lat);
            arr[(i * 2) + 1 + offset] = toRadians(coord.lng);
        }

        return (original.size() * 2) + offset;
    }

    /**
     * Create polygons from a set of contiguous indexes
     */
    public List<List<List<GeoCoord>>> h3AddressSetToMultiPolygon(Collection<String> h3Addresses, boolean geoJson) {
        List<Long> indices = stringToH3List(h3Addresses);

        return h3SetToMultiPolygon(indices, geoJson);
    }

    /**
     * Create polygons from a set of contiguous indexes
     */
    public List<List<List<GeoCoord>>> h3SetToMultiPolygon(Collection<Long> h3, boolean geoJson) {
        long[] h3AsArray = collectionToLongArray(h3);

        ArrayList<List<List<GeoCoord>>> result = new ArrayList<>();

        h3Api.h3SetToLinkedGeo(h3AsArray, result);

        // For each polygon
        for (List<List<GeoCoord>> loops : result) {
            // For each loop within the polygon (first being the outline,
            // further loops being "holes" or exclusions in the polygon.)
            for (List<GeoCoord> loop : loops) {
                // For each coordinate in the loop, we need to convert to degrees,
                // and ensure the correct ordering (whether geoJson or not.)
                for (int vectorInLoop = 0; vectorInLoop < loop.size(); vectorInLoop++) {
                    final GeoCoord v = loop.get(vectorInLoop);
                    final double origLat = toDegrees(v.lat);
                    final double origLng = toDegrees(v.lng);

                    final GeoCoord replacement = new GeoCoord(origLat, origLng);

                    loop.set(vectorInLoop, replacement);
                }

                if (geoJson && loop.size() > 0) {
                    // geoJson requires closing the loop
                    loop.add(loop.get(0));
                }
            }
        }

        return result;
    }

    /**
     * Returns the resolution of the provided index
     */
    public int h3GetResolution(String h3Address) {
        return h3GetResolution(stringToH3(h3Address));
    }

    /**
     * Returns the resolution of the provided index
     */
    public int h3GetResolution(long h3) {
        return (int) ((h3 & H3_RES_MASK) >> H3_RES_OFFSET);
    }

    /**
     * Returns the parent of the index at the given resolution.
     *
     * @param h3 H3 index.
     * @param res Resolution of the parent, <code>0 &lt;= res &lt;= h3GetResolution(h3)</code>
     * @throws IllegalArgumentException <code>res</code> is not between 0 and the resolution of <code>h3</code>, inclusive.
     */
    public long h3ToParent(long h3, int res) {
        checkResolution(res);
        // This is a ported version of h3ToParent from h3core.

        int childRes = (int) ((h3 & H3_RES_MASK) >> H3_RES_OFFSET);
        if (res < 0 || res > childRes) {
            throw new IllegalArgumentException(String.format("res (%d) must be between 0 and %d, inclusive", res, childRes));
        } else if (res == childRes) {
            return h3;
        }

        // newRes is the bits that need to be set to set the given resolution.
        long newRes = (long) res << H3_RES_OFFSET;
        long digitMaskForRes = H3_DIGIT_MASK;
        for (int i = 0; i < res; i++) {
            digitMaskForRes >>= 3L;
        }

        return (h3 & H3_RES_MASK_NEGATIVE) | newRes | digitMaskForRes;
    }

    /**
     * Returns the parent of the index at the given resolution.
     *
     * @param h3Address H3 index.
     * @param res Resolution of the parent, <code>0 &lt;= res &lt;= h3GetResolution(h3)</code>
     */
    public String h3ToParentAddress(String h3Address, int res) {
        long parent = h3ToParent(stringToH3(h3Address), res);
        return h3ToString(parent);
    }

    /**
     * Provides the children of the index at the given resolution.
     *
     * @param childRes Resolution of the children
     */
    public List<String> h3ToChildren(String h3Address, int childRes) {
        return h3ToStringList(h3ToChildren(stringToH3(h3Address), childRes));
    }

    /**
     * Provides the children of the index at the given resolution.
     *
     * @param h3 H3 index.
     * @param childRes Resolution of the children
     * @throws IllegalArgumentException Invalid resolution
     */
    public List<Long> h3ToChildren(long h3, int childRes) {
        checkResolution(childRes);

        int sz = h3Api.maxH3ToChildrenSize(h3, childRes);

        long[] out = new long[sz];

        h3Api.h3ToChildren(h3, childRes, out);

        return nonZeroLongArrayToList(out);
    }

    /**
     * Determines if an index is Class III or Class II.
     *
     * @return <code>true</code> if the index is Class III
     */
    public boolean h3IsResClassIII(String h3Address) {
        return h3IsResClassIII(stringToH3(h3Address));
    }

    /**
     * Determines if an index is Class III or Class II.
     *
     * @param h3 H3 index.
     * @return <code>true</code> if the index is Class III
     */
    public boolean h3IsResClassIII(long h3) {
        return h3GetResolution(h3) % 2 != 0;
    }

    /**
     * Returns a compacted set of indexes, at possibly coarser resolutions.
     */
    public List<String> compactAddress(Collection<String> h3Addresses) {
        List<Long> h3 = stringToH3List(h3Addresses);
        List<Long> compacted = compact(h3);
        return h3ToStringList(compacted);
    }

    /**
     * Returns a compacted set of indexes, at possibly coarser resolutions.
     *
     * @throws IllegalArgumentException Invalid input, such as duplicated indexes.
     */
    public List<Long> compact(Collection<Long> h3) {
        int sz = h3.size();

        long[] h3AsArray = collectionToLongArray(h3);

        long[] out = new long[sz];

        int success = h3Api.compact(h3AsArray, out);

        if (success != 0) {
            throw new IllegalArgumentException("Bad input to compact");
        }

        return nonZeroLongArrayToList(out);
    }

    /**
     * Uncompacts all the given indexes to resolution <code>res</code>.
     */
    public List<String> uncompactAddress(Collection<String> h3Addresses, int res) {
        List<Long> h3 = stringToH3List(h3Addresses);
        List<Long> uncompacted = uncompact(h3, res);
        return h3ToStringList(uncompacted);
    }

    /**
     * Uncompacts all the given indexes to resolution <code>res</code>.
     *
     * @throws IllegalArgumentException Invalid input, such as indexes finer than <code>res</code>.
     */
    public List<Long> uncompact(Collection<Long> h3, int res) {
        checkResolution(res);

        long[] h3AsArray = collectionToLongArray(h3);

        int sz = h3Api.maxUncompactSize(h3AsArray, res);

        long[] out = new long[sz];

        int success = h3Api.uncompact(h3AsArray, res, out);

        if (success != 0) {
            throw new IllegalArgumentException("Bad input to uncompact");
        }

        return nonZeroLongArrayToList(out);
    }

    /**
     * Converts from <code>long</code> representation of an index to <code>String</code> representation.
     */
    public String h3ToString(long h3) {
        return Long.toHexString(h3);
    }

    /**
     * Converts from <code>String</code> representation of an index to <code>long</code> representation.
     */
    public long stringToH3(String h3Address) {
        return Long.parseUnsignedLong(h3Address, 16);
    }

    /**
     * Returns the average area in <code>unit</code> for indexes at resolution <code>res</code>.
     *
     * @throws IllegalArgumentException Invalid parameter value
     */
    public double hexArea(int res, AreaUnit unit) {
        checkResolution(res);
        if (unit == AreaUnit.km2)
            return h3Api.hexAreaKm2(res);
        else if (unit == AreaUnit.m2)
            return h3Api.hexAreaM2(res);
        else
            throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
    }

    /**
     * Returns the average edge length in <code>unit</code> for indexes at resolution <code>res</code>.
     *
     * @throws IllegalArgumentException Invalid parameter value
     */
    public double edgeLength(int res, LengthUnit unit) {
        checkResolution(res);
        if (unit == LengthUnit.km)
            return h3Api.edgeLengthKm(res);
        else if (unit == LengthUnit.m)
            return h3Api.edgeLengthM(res);
        else
            throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
    }

    /**
     * Returns the number of unique H3 indexes at resolution <code>res</code>.
     *
     * @throws IllegalArgumentException Invalid resolution
     */
    public long numHexagons(int res) {
        checkResolution(res);
        return h3Api.numHexagons(res);
    }

    /**
     * Returns <code>true</code> if the two indexes are neighbors.
     */
    public boolean h3IndexesAreNeighbors(long a, long b) {
        return h3Api.h3IndexesAreNeighbors(a, b);
    }

    /**
     * Returns <code>true</code> if the two indexes are neighbors.
     */
    public boolean h3IndexesAreNeighbors(String a, String b) {
        return h3IndexesAreNeighbors(stringToH3(a), stringToH3(b));
    }

    /**
     * Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>.
     *
     * @throws IllegalArgumentException The indexes are not neighbors.
     */
    public long getH3UnidirectionalEdge(long a, long b) {
        long index = h3Api.getH3UnidirectionalEdge(a, b);

        if (index == 0) {
            throw new IllegalArgumentException("Given indexes are not neighbors.");
        }

        return index;
    }

    /**
     * Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>.
     *
     * @throws IllegalArgumentException The indexes are not neighbors.
     */
    public String getH3UnidirectionalEdge(String a, String b) {
        return h3ToString(getH3UnidirectionalEdge(stringToH3(a), stringToH3(b)));
    }

    /**
     * Returns <code>true</code> if the given index is a valid unidirectional edge.
     */
    public boolean h3UnidirectionalEdgeIsValid(long h3) {
        return h3Api.h3UnidirectionalEdgeIsValid(h3);
    }

    /**
     * Returns <code>true</code> if the given index is a valid unidirectional edge.
     */
    public boolean h3UnidirectionalEdgeIsValid(String h3) {
        return h3UnidirectionalEdgeIsValid(stringToH3(h3));
    }

    /**
     * Returns the origin index of the given unidirectional edge.
     */
    public long getOriginH3IndexFromUnidirectionalEdge(long h3) {
        return h3Api.getOriginH3IndexFromUnidirectionalEdge(h3);
    }

    /**
     * Returns the origin index of the given unidirectional edge.
     */
    public String getOriginH3IndexFromUnidirectionalEdge(String h3) {
        return h3ToString(getOriginH3IndexFromUnidirectionalEdge(stringToH3(h3)));
    }

    /**
     * Returns the destination index of the given unidirectional edge.
     */
    public long getDestinationH3IndexFromUnidirectionalEdge(long h3) {
        return h3Api.getDestinationH3IndexFromUnidirectionalEdge(h3);
    }

    /**
     * Returns the destination index of the given unidirectional edge.
     */
    public String getDestinationH3IndexFromUnidirectionalEdge(String h3) {
        return h3ToString(getDestinationH3IndexFromUnidirectionalEdge(stringToH3(h3)));
    }

    /**
     * Returns the origin and destination indexes (in that order) of the given
     * unidirectional edge.
     */
    public List<Long> getH3IndexesFromUnidirectionalEdge(long h3) {
        long[] results = new long[2];

        // TODO: could be a pair type
        h3Api.getH3IndexesFromUnidirectionalEdge(h3, results);

        return nonZeroLongArrayToList(results);
    }

    /**
     * Returns the origin and destination indexes (in that order) of the given
     * unidirectional edge.
     */
    public List<String> getH3IndexesFromUnidirectionalEdge(String h3) {
        return h3ToStringList(getH3IndexesFromUnidirectionalEdge(stringToH3(h3)));
    }

    /**
     * Returns all unidirectional edges originating from the given index.
     */
    public List<Long> getH3UnidirectionalEdgesFromHexagon(long h3) {
        long[] results = new long[6];

        h3Api.getH3UnidirectionalEdgesFromHexagon(h3, results);

        return nonZeroLongArrayToList(results);
    }

    /**
     * Returns all unidirectional edges originating from the given index.
     */
    public List<String> getH3UnidirectionalEdgesFromHexagon(String h3) {
        return h3ToStringList(getH3UnidirectionalEdgesFromHexagon(stringToH3(h3)));
    }

    /**
     * Returns a list of coordinates representing the given edge.
     */
    public List<GeoCoord> getH3UnidirectionalEdgeBoundary(long h3) {
        double[] verts = new double[MAX_CELL_BNDRY_VERTS * 2];
        int numVerts = h3Api.getH3UnidirectionalEdgeBoundary(h3, verts);
        List<GeoCoord> out = new ArrayList<>(numVerts);
        for (int i = 0; i < numVerts; i++) {
            GeoCoord coord = new GeoCoord(
                    toDegrees(verts[i * 2]),
                    toDegrees(verts[(i * 2) + 1])
            );
            out.add(coord);
        }
        return out;
    }

    /**
     * Returns a list of coordinates representing the given edge.
     */
    public List<GeoCoord> getH3UnidirectionalEdgeBoundary(String h3) {
        return getH3UnidirectionalEdgeBoundary(stringToH3(h3));
    }

    /**
     * Transforms a collection of H3 indexes in string form to a list of H3
     * indexes in long form.
     */
    private List<Long> stringToH3List(Collection<String> collection) {
        return collection.stream()
                .map(this::stringToH3)
                .collect(Collectors.toList());
    }

    /**
     * Transforms a list of H3 indexes in long form to a list of H3
     * indexes in string form.
     */
    private List<String> h3ToStringList(List<Long> list) {
        return list.stream()
                .map(this::h3ToString)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new list with all non-zero elements of the array as members.
     */
    private static List<Long> nonZeroLongArrayToList(long[] out) {
        // Allocate for the case that we need to copy everything from
        // the `out` array.
        List<Long> ret = new ArrayList<>(out.length);

        for (int i = 0; i < out.length; i++) {
            long h = out[i];
            if (h != 0) {
                ret.add(h);
            }
        }

        return ret;
    }

    /**
     * Returns an array of <code>long</code> with the contents of the collection.
     */
    private static long[] collectionToLongArray(Collection<Long> collection) {
        return collection.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * @throws IllegalArgumentException <code>res</code> is not a valid H3 resolution.
     */
    private static void checkResolution(int res) {
        if (res < 0 || res > 15) {
            throw new IllegalArgumentException(String.format("resolution %d is out of range (must be 0 <= res <= 15)", res));
        }
    }
}
