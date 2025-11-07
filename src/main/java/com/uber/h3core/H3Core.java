/*
 * Copyright 2017-2019, 2022-2023 Uber Technologies, Inc.
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

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.CoordIJ;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * H3Core provides all functions of the H3 API.
 *
 * <p>This class is thread safe and can be used as a singleton.
 *
 * <p>Any function in this class may throw {@link H3Exception}.
 */
public class H3Core {
  // These constants are from h3api.h and h3Index.h
  /** Maximum number of vertices for an H3 index */
  private static final int MAX_CELL_BNDRY_VERTS = 10;

  private static final int NUM_BASE_CELLS = 122;
  private static final int NUM_PENTAGONS = 12;

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

  private static final long INVALID_INDEX = 0L;

  /** Native implementation of the H3 library. */
  private final NativeMethods h3Api;

  /**
   * Create by unpacking the H3 native library to disk and loading it. The library will attempt to
   * detect the correct operating system and architecture of native library to unpack.
   *
   * @throws SecurityException Loading the library was not allowed by the SecurityManager.
   * @throws UnsatisfiedLinkError The library could not be loaded
   * @throws IOException The library could not be extracted to disk.
   */
  public static H3Core newInstance() throws IOException {
    NativeMethods h3Api = H3CoreLoader.loadNatives();
    return new H3Core(h3Api);
  }

  /**
   * Create by unpacking the H3 native library to disk and loading it. The library will attempt to
   * extract the native library matching the given arguments to disk.
   *
   * @throws SecurityException Loading the library was not allowed by the SecurityManager.
   * @throws UnsatisfiedLinkError The library could not be loaded
   * @throws IOException The library could not be extracted to disk.
   */
  public static H3Core newInstance(H3CoreLoader.OperatingSystem os, String arch)
      throws IOException {
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

  /** Construct with the given NativeMethods, from {@link H3CoreLoader}. */
  private H3Core(NativeMethods h3Api) {
    this.h3Api = h3Api;
  }

  /** Returns true if this is a valid H3 cell index. */
  public boolean isValidCell(long h3) {
    return h3Api.isValidCell(h3);
  }

  /** Returns true if this is a valid H3 cell index. */
  public boolean isValidCell(String h3Address) {
    return isValidCell(stringToH3(h3Address));
  }

  /** Returns true if this is a valid H3 index. */
  public boolean isValidIndex(long h3) {
    return h3Api.isValidIndex(h3);
  }

  /** Returns true if this is a valid H3 index. */
  public boolean isValidIndex(String h3Address) {
    return isValidIndex(stringToH3(h3Address));
  }

  /** Construct a cell index from component parts */
  public long constructCell(int res, int baseCellNumber, List<Integer> digits) {
    int[] digitsArray = digits.stream().mapToInt(Integer::intValue).toArray();
    if (digitsArray.length < res) {
      throw new IllegalArgumentException(
          String.format(
              "Number of provided digits is too few, must be at least %d, was %d",
              res - 1, digitsArray.length));
    }
    if (digitsArray.length > 15) {
      throw new IllegalArgumentException(
          String.format(
              "Additional unused digits provided, must be at most 15 but was %d",
              digitsArray.length));
    }
    return h3Api.constructCell(res, baseCellNumber, digitsArray);
  }

  /** Construct a cell index from component parts */
  public String constructCellAddress(int res, int baseCellNumber, List<Integer> digits) {
    return h3ToString(constructCell(res, baseCellNumber, digits));
  }

  /** Returns the base cell number for this index. */
  public int getBaseCellNumber(long h3) {
    return h3Api.getBaseCellNumber(h3);
  }

  /** Returns the base cell number for this index. */
  public int getBaseCellNumber(String h3Address) {
    return getBaseCellNumber(stringToH3(h3Address));
  }

  /** Returns <code>true</code> if this index is one of twelve pentagons per resolution. */
  public boolean isPentagon(long h3) {
    return h3Api.isPentagon(h3);
  }

  /** Returns <code>true</code> if this index is one of twelve pentagons per resolution. */
  public boolean isPentagon(String h3Address) {
    return isPentagon(stringToH3(h3Address));
  }

  /**
   * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
   *
   * @param lat Latitude in degrees.
   * @param lng Longitude in degrees.
   * @param res Resolution, 0 &lt;= res &lt;= 15
   * @return The H3 index.
   */
  public long latLngToCell(double lat, double lng, int res) {
    checkResolution(res);
    return h3Api.latLngToCell(toRadians(lat), toRadians(lng), res);
  }

  /**
   * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
   *
   * @param lat Latitude in degrees.
   * @param lng Longitude in degrees.
   * @param res Resolution, 0 &lt;= res &lt;= 15
   * @return The H3 index.
   */
  public String latLngToCellAddress(double lat, double lng, int res) {
    return h3ToString(latLngToCell(lat, lng, res));
  }

  /** Find the latitude, longitude (both in degrees) center point of the cell. */
  public LatLng cellToLatLng(long h3) {
    double[] coords = new double[2];
    h3Api.cellToLatLng(h3, coords);
    LatLng out = new LatLng(toDegrees(coords[0]), toDegrees(coords[1]));
    return out;
  }

  /** Find the latitude, longitude (degrees) center point of the cell. */
  public LatLng cellToLatLng(String h3Address) {
    return cellToLatLng(stringToH3(h3Address));
  }

  /** Find the cell boundary in latitude, longitude (degrees) coordinates for the cell */
  public List<LatLng> cellToBoundary(long h3) {
    double[] verts = new double[MAX_CELL_BNDRY_VERTS * 2];
    int numVerts = h3Api.cellToBoundary(h3, verts);
    List<LatLng> out = new ArrayList<>(numVerts);
    for (int i = 0; i < numVerts; i++) {
      LatLng coord = new LatLng(toDegrees(verts[i * 2]), toDegrees(verts[(i * 2) + 1]));
      out.add(coord);
    }
    return out;
  }

  /** Find the cell boundary in latitude, longitude (degrees) coordinates for the cell */
  public List<LatLng> cellToBoundary(String h3Address) {
    return cellToBoundary(stringToH3(h3Address));
  }

  /**
   * Neighboring indexes in all directions.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   */
  public List<String> gridDisk(String h3Address, int k) {
    return h3ToStringList(gridDisk(stringToH3(h3Address), k));
  }

  /**
   * Neighboring indexes in all directions.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   */
  public List<Long> gridDisk(long h3, int k) {
    int sz = longToIntSize(h3Api.maxGridDiskSize(k));

    long[] out = new long[sz];

    h3Api.gridDisk(h3, k, out);

    return nonZeroLongArrayToList(out);
  }

  /**
   * Neighboring indexes in all directions, ordered by distance from the origin index.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<String>> gridDiskDistances(String h3Address, int k) {
    List<List<Long>> rings = gridDiskDistances(stringToH3(h3Address), k);

    return rings.stream().map(this::h3ToStringList).collect(Collectors.toList());
  }

  /**
   * Neighboring indexes in all directions, ordered by distance from the origin index.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<Long>> gridDiskDistances(long h3, int k) {
    int sz = longToIntSize(h3Api.maxGridDiskSize(k));

    long[] out = new long[sz];
    int[] distances = new int[sz];

    h3Api.gridDiskDistances(h3, k, out, distances);

    List<List<Long>> ret = new ArrayList<>(k + 1);

    for (int i = 0; i <= k; i++) {
      ret.add(new ArrayList<>());
    }

    for (int i = 0; i < sz; i++) {
      long nextH3 = out[i];
      if (nextH3 != INVALID_INDEX) {
        ret.get(distances[i]).add(nextH3);
      }
    }

    return ret;
  }

  /**
   * Returns in order neighbor traversal.
   *
   * @param h3Address Origin hexagon index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<String>> gridDiskUnsafe(String h3Address, int k) {
    List<List<Long>> rings = gridDiskUnsafe(stringToH3(h3Address), k);

    return rings.stream().map(this::h3ToStringList).collect(Collectors.toList());
  }

  /**
   * Returns in order neighbor traversal.
   *
   * @param h3 Origin hexagon index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<Long>> gridDiskUnsafe(long h3, int k) {
    int sz = longToIntSize(h3Api.maxGridDiskSize(k));

    long[] out = new long[sz];

    h3Api.gridDiskUnsafe(h3, k, out);

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
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<String> gridRing(String h3Address, int k) {
    return h3ToStringList(gridRing(stringToH3(h3Address), k));
  }

  /**
   * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<Long> gridRing(long h3, int k) {
    int sz = k == 0 ? 1 : 6 * k;

    long[] out = new long[sz];

    h3Api.gridRing(h3, k, out);

    return nonZeroLongArrayToList(out);
  }

  /**
   * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<String> gridRingUnsafe(String h3Address, int k) {
    return h3ToStringList(gridRingUnsafe(stringToH3(h3Address), k));
  }

  /**
   * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<Long> gridRingUnsafe(long h3, int k) {
    int sz = k == 0 ? 1 : 6 * k;

    long[] out = new long[sz];

    h3Api.gridRingUnsafe(h3, k, out);

    return nonZeroLongArrayToList(out);
  }

  /**
   * Returns the distance between <code>a</code> and <code>b</code>. This is the grid distance, or
   * distance expressed in number of H3 cells.
   *
   * <p>In some cases H3 cannot compute the distance between two indexes. This can happen because:
   *
   * <ul>
   *   <li>The indexes are not comparable (difference resolutions, etc)
   *   <li>The distance is greater than the H3 core library supports
   *   <li>The H3 library does not support finding the distance between the two cells, because of
   *       pentagonal distortion.
   * </ul>
   *
   * @param a An H3 index
   * @param b Another H3 index
   * @return Distance between the two in grid cells
   */
  public long gridDistance(String a, String b) {
    return gridDistance(stringToH3(a), stringToH3(b));
  }

  /**
   * Returns the distance between <code>a</code> and <code>b</code>. This is the grid distance, or
   * distance expressed in number of H3 cells.
   *
   * <p>In some cases H3 cannot compute the distance between two indexes. This can happen because:
   *
   * <ul>
   *   <li>The indexes are not comparable (difference resolutions, etc)
   *   <li>The distance is greater than the H3 core library supports
   *   <li>The H3 library does not support finding the distance between the two cells, because of
   *       pentagonal distortion.
   * </ul>
   *
   * @param a An H3 index
   * @param b Another H3 index
   * @return Distance between the two in grid cells
   */
  public long gridDistance(long a, long b) {
    return h3Api.gridDistance(a, b);
  }

  /**
   * Converts <code>h3</code> to IJ coordinates in a local coordinate space defined by <code>origin
   * </code>.
   *
   * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
   * distortion. IJ coordinates are only comparable if they came from the same origin.
   *
   * <p>This function is experimental, and its output is not guaranteed to be compatible across
   * different versions of H3.
   *
   * @param origin Anchoring index for the local coordinate space.
   * @param h3 Index to find the coordinates of.
   * @return Coordinates for <code>h3</code> in the local coordinate space.
   */
  public CoordIJ cellToLocalIj(long origin, long h3) {
    final int[] coords = new int[2];
    h3Api.cellToLocalIj(origin, h3, coords);
    return new CoordIJ(coords[0], coords[1]);
  }

  /**
   * Converts <code>h3Address</code> to IJ coordinates in a local coordinate space defined by <code>
   * originAddress</code>.
   *
   * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
   * distortion. IJ coordinates are only comparable if they came from the same origin.
   *
   * <p>This function is experimental, and its output is not guaranteed to be compatible across
   * different versions of H3.
   *
   * @param originAddress Anchoring index for the local coordinate space.
   * @param h3Address Index to find the coordinates of.
   * @return Coordinates for <code>h3</code> in the local coordinate space.
   */
  public CoordIJ cellToLocalIj(String originAddress, String h3Address) {
    return cellToLocalIj(stringToH3(originAddress), stringToH3(h3Address));
  }

  /**
   * Converts the IJ coordinates to an index, using a local IJ coordinate space anchored by <code>
   * origin</code>.
   *
   * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
   * distortion. IJ coordinates are only comparable if they came from the same origin.
   *
   * <p>This function is experimental, and its output is not guaranteed to be compatible across
   * different versions of H3.
   *
   * @param origin Anchoring index for the local coordinate space.
   * @param ij Coordinates in the local IJ coordinate space.
   * @return Index represented by <code>ij</code>
   */
  public long localIjToCell(long origin, CoordIJ ij) {
    return h3Api.localIjToCell(origin, ij.i, ij.j);
  }

  /**
   * Converts the IJ coordinates to an index, using a local IJ coordinate space anchored by <code>
   * origin</code>.
   *
   * <p>The local IJ coordinate space may have deleted regions and warping due to pentagon
   * distortion. IJ coordinates are only comparable if they came from the same origin.
   *
   * <p>This function is experimental, and its output is not guaranteed to be compatible across
   * different versions of H3.
   *
   * @param originAddress Anchoring index for the local coordinate space.
   * @param ij Coordinates in the local IJ coordinate space.
   * @return Index represented by <code>ij</code>
   */
  public String localIjToCell(String originAddress, CoordIJ ij) {
    return h3ToString(localIjToCell(stringToH3(originAddress), ij));
  }

  /**
   * Given two H3 indexes, return the line of indexes between them (inclusive of endpoints).
   *
   * <p>This function may fail to find the line between two indexes, for example if they are very
   * far apart. It may also fail when finding distances for indexes on opposite sides of a pentagon.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>The specific output of this function should not be considered stable across library
   *       versions. The only guarantees the library provides are that the line length will be
   *       `h3Distance(start, end) + 1` and that every index in the line will be a neighbor of the
   *       preceding index.
   *   <li>Lines are drawn in grid space, and may not correspond exactly to either Cartesian lines
   *       or great arcs.
   * </ul>
   *
   * @param startAddress Start index of the line
   * @param endAddress End index of the line
   * @return Indexes making up the line.
   */
  public List<String> gridPathCells(String startAddress, String endAddress) {
    return h3ToStringList(gridPathCells(stringToH3(startAddress), stringToH3(endAddress)));
  }

  /**
   * Given two H3 indexes, return the line of indexes between them (inclusive of endpoints).
   *
   * <p>This function may fail to find the line between two indexes, for example if they are very
   * far apart. It may also fail when finding distances for indexes on opposite sides of a pentagon.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>The specific output of this function should not be considered stable across library
   *       versions. The only guarantees the library provides are that the line length will be
   *       `h3Distance(start, end) + 1` and that every index in the line will be a neighbor of the
   *       preceding index.
   *   <li>Lines are drawn in grid space, and may not correspond exactly to either Cartesian lines
   *       or great arcs.
   * </ul>
   *
   * @param start Start index of the line
   * @param end End index of the line
   * @return Indexes making up the line.
   */
  public List<Long> gridPathCells(long start, long end) {
    int size = longToIntSize(h3Api.gridPathCellsSize(start, end));

    long[] results = new long[size];
    h3Api.gridPathCells(start, end, results);

    return nonZeroLongArrayToList(results);
  }

  /**
   * Finds indexes within the given geopolygon.
   *
   * @param points Outline geopolygon
   * @param holes Geopolygons of any internal holes
   * @param res Resolution of the desired indexes
   */
  public List<String> polygonToCellAddressesExperimental(
      List<LatLng> points, List<List<LatLng>> holes, int res, PolygonToCellsFlags flags) {
    return h3ToStringList(polygonToCellsExperimental(points, holes, res, flags));
  }

  /**
   * Finds indexes within the given geopolygon.
   *
   * @param points Outline geopolygon
   * @param holes Geopolygon of any internal holes
   * @param res Resolution of the desired indexes
   * @throws IllegalArgumentException Invalid resolution
   */
  public List<Long> polygonToCellsExperimental(
      List<LatLng> points, List<List<LatLng>> holes, int res, PolygonToCellsFlags flags) {
    checkResolution(res);

    // pack the data for use by the polyfill JNI call
    double[] verts = new double[points.size() * 2];
    packGeofenceVertices(verts, points, 0);
    int[] holeSizes = new int[0];
    double[] holeVerts = new double[0];
    if (holes != null) {
      int holesSize = holes.size();
      holeSizes = new int[holesSize];
      int totalSize = 0;
      for (int i = 0; i < holesSize; i++) {
        int holeSize = holes.get(i).size() * 2;
        totalSize += holeSize;
        // Note we are storing the number of doubles
        holeSizes[i] = holeSize;
      }
      holeVerts = new double[totalSize];
      int offset = 0;
      for (int i = 0; i < holesSize; i++) {
        offset = packGeofenceVertices(holeVerts, holes.get(i), offset);
      }
    }

    int flagsInt = flags.toInt();
    int sz =
        longToIntSize(
            h3Api.maxPolygonToCellsSizeExperimental(verts, holeSizes, holeVerts, res, flagsInt));

    long[] results = new long[sz];

    h3Api.polygonToCellsExperimental(verts, holeSizes, holeVerts, res, flagsInt, results);

    return nonZeroLongArrayToList(results);
  }

  /**
   * Finds indexes within the given geopolygon.
   *
   * @param points Outline geopolygon
   * @param holes Geopolygons of any internal holes
   * @param res Resolution of the desired indexes
   */
  public List<String> polygonToCellAddresses(
      List<LatLng> points, List<List<LatLng>> holes, int res) {
    return h3ToStringList(polygonToCells(points, holes, res));
  }

  /**
   * Finds indexes within the given geopolygon.
   *
   * @param points Outline geopolygon
   * @param holes Geopolygon of any internal holes
   * @param res Resolution of the desired indexes
   * @throws IllegalArgumentException Invalid resolution
   */
  public List<Long> polygonToCells(List<LatLng> points, List<List<LatLng>> holes, int res) {
    checkResolution(res);

    // pack the data for use by the polyfill JNI call
    double[] verts = new double[points.size() * 2];
    packGeofenceVertices(verts, points, 0);
    int[] holeSizes = new int[0];
    double[] holeVerts = new double[0];
    if (holes != null) {
      int holesSize = holes.size();
      holeSizes = new int[holesSize];
      int totalSize = 0;
      for (int i = 0; i < holesSize; i++) {
        int holeSize = holes.get(i).size() * 2;
        totalSize += holeSize;
        // Note we are storing the number of doubles
        holeSizes[i] = holeSize;
      }
      holeVerts = new double[totalSize];
      int offset = 0;
      for (int i = 0; i < holesSize; i++) {
        offset = packGeofenceVertices(holeVerts, holes.get(i), offset);
      }
    }

    int flags = 0;
    int sz = longToIntSize(h3Api.maxPolygonToCellsSize(verts, holeSizes, holeVerts, res, flags));

    long[] results = new long[sz];

    h3Api.polygonToCells(verts, holeSizes, holeVerts, res, flags, results);

    return nonZeroLongArrayToList(results);
  }

  /**
   * Interleave the pairs in the given double array.
   *
   * @return Next offset to begin filling from
   */
  private static int packGeofenceVertices(double[] arr, List<LatLng> original, int offset) {
    int newOffset = (original.size() * 2) + offset;
    assert arr.length >= newOffset;

    for (int i = 0, size = original.size(); i < size; i++) {
      LatLng coord = original.get(i);

      int firstOffset = (i * 2) + offset;
      int secondOffset = firstOffset + 1;
      arr[firstOffset] = toRadians(coord.lat);
      arr[secondOffset] = toRadians(coord.lng);
    }

    return newOffset;
  }

  /** Create polygons from a set of contiguous indexes */
  public List<List<List<LatLng>>> cellAddressesToMultiPolygon(
      Collection<String> h3Addresses, boolean geoJson) {
    List<Long> indices = stringToH3List(h3Addresses);

    return cellsToMultiPolygon(indices, geoJson);
  }

  /** Create polygons from a set of contiguous indexes */
  public List<List<List<LatLng>>> cellsToMultiPolygon(Collection<Long> h3, boolean geoJson) {
    long[] h3AsArray = collectionToLongArray(h3);

    ArrayList<List<List<LatLng>>> result = new ArrayList<>();

    h3Api.cellsToLinkedMultiPolygon(h3AsArray, result);

    // For each polygon
    for (List<List<LatLng>> loops : result) {
      // For each loop within the polygon (first being the outline,
      // further loops being "holes" or exclusions in the polygon.)
      for (List<LatLng> loop : loops) {
        // For each coordinate in the loop, we need to convert to degrees,
        // and ensure the correct ordering (whether geoJson or not.)
        for (int vectorInLoop = 0, size = loop.size(); vectorInLoop < size; vectorInLoop++) {
          final LatLng v = loop.get(vectorInLoop);
          final double origLat = toDegrees(v.lat);
          final double origLng = toDegrees(v.lng);

          final LatLng replacement = new LatLng(origLat, origLng);

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

  /** Returns the resolution of the provided index */
  public int getResolution(String h3Address) {
    return getResolution(stringToH3(h3Address));
  }

  /** Returns the resolution of the provided index. */
  public int getResolution(long h3) {
    return (int) ((h3 & H3_RES_MASK) >> H3_RES_OFFSET);
  }

  /**
   * Returns the indexing digit of the index at `res`
   *
   * @param h3 H3 index.
   * @param res Resolution of the digit, <code>1 &lt;= res &lt;= 15</code>
   * @throws IllegalArgumentException <code>res</code> is not between 0 and 15, inclusive.
   */
  public int getIndexDigit(String h3Address, int res) {
    return getIndexDigit(stringToH3(h3Address), res);
  }

  /**
   * Returns the indexing digit of the index at `res`
   *
   * @param h3 H3 index.
   * @param res Resolution of the digit, <code>1 &lt;= res &lt;= 15</code>
   * @throws IllegalArgumentException <code>res</code> is not between 0 and 15, inclusive.
   */
  public int getIndexDigit(long h3, int res) {
    if (res < 1 || res > 15) {
      throw new IllegalArgumentException(
          String.format("resolution %d is out of range (must be 1 <= res <= 15)", res));
    }
    return (int) ((h3 >> ((15 - res) * 3)) & 7);
  }

  /**
   * Returns the parent of the index at the given resolution.
   *
   * @param h3 H3 index.
   * @param res Resolution of the parent, <code>0 &lt;= res &lt;= h3GetResolution(h3)</code>
   * @throws IllegalArgumentException <code>res</code> is not between 0 and the resolution of <code>
   *     h3</code>, inclusive.
   */
  public long cellToParent(long h3, int res) {
    // This is a ported version of h3ToParent from h3core.

    int childRes = (int) ((h3 & H3_RES_MASK) >> H3_RES_OFFSET);
    if (res < 0 || res > childRes) {
      throw new IllegalArgumentException(
          String.format("res (%d) must be between 0 and %d, inclusive", res, childRes));
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
  public String cellToParentAddress(String h3Address, int res) {
    long parent = cellToParent(stringToH3(h3Address), res);
    return h3ToString(parent);
  }

  /**
   * Provides the children of the index at the given resolution.
   *
   * @param childRes Resolution of the children
   */
  public List<String> cellToChildren(String h3Address, int childRes) {
    return h3ToStringList(cellToChildren(stringToH3(h3Address), childRes));
  }

  /**
   * Provides the children of the index at the given resolution.
   *
   * @param h3 H3 index.
   * @param childRes Resolution of the children
   * @throws IllegalArgumentException Invalid resolution
   */
  public List<Long> cellToChildren(long h3, int childRes) {
    checkResolution(childRes);

    int sz = longToIntSize(h3Api.cellToChildrenSize(h3, childRes));

    long[] out = new long[sz];

    h3Api.cellToChildren(h3, childRes, out);

    return nonZeroLongArrayToList(out);
  }

  /**
   * Returns the center child at the given resolution.
   *
   * @param h3 Parent H3 index
   * @param childRes Resolution of the child
   * @throws IllegalArgumentException Invalid resolution (e.g. coarser than the parent)
   */
  public String cellToCenterChild(String h3, int childRes) {
    return h3ToString(cellToCenterChild(stringToH3(h3), childRes));
  }

  /** Returns the number of children the cell index has at the given resolution. */
  public long cellToChildrenSize(long cell, int childRes) {
    return h3Api.cellToChildrenSize(cell, childRes);
  }

  /** Returns the number of children the cell index has at the given resolution. */
  public long cellToChildrenSize(String cellAddress, int childRes) {
    return cellToChildrenSize(stringToH3(cellAddress), childRes);
  }

  /**
   * Returns the center child at the given resolution.
   *
   * @param h3 Parent H3 index
   * @param childRes Resolution of the child
   * @throws IllegalArgumentException Invalid resolution (e.g. coarser than the parent)
   */
  public long cellToCenterChild(long h3, int childRes) {
    checkResolution(childRes);

    long result = h3Api.cellToCenterChild(h3, childRes);

    return result;
  }

  /**
   * Determines if an index is Class III or Class II.
   *
   * @return <code>true</code> if the index is Class III
   */
  public boolean isResClassIII(String h3Address) {
    return isResClassIII(stringToH3(h3Address));
  }

  /**
   * Determines if an index is Class III or Class II.
   *
   * @param h3 H3 index.
   * @return <code>true</code> if the index is Class III
   */
  public boolean isResClassIII(long h3) {
    return getResolution(h3) % 2 != 0;
  }

  /** Returns a compacted set of indexes, at possibly coarser resolutions. */
  public List<String> compactCellAddresses(Collection<String> h3Addresses) {
    List<Long> h3 = stringToH3List(h3Addresses);
    List<Long> compacted = compactCells(h3);
    return h3ToStringList(compacted);
  }

  /** Returns a compacted set of indexes, at possibly coarser resolutions. */
  public List<Long> compactCells(Collection<Long> h3) {
    int sz = h3.size();

    long[] h3AsArray = collectionToLongArray(h3);

    long[] out = new long[sz];

    h3Api.compactCells(h3AsArray, out);

    return nonZeroLongArrayToList(out);
  }

  /** Uncompacts all the given indexes to resolution <code>res</code>. */
  public List<String> uncompactCellAddresses(Collection<String> h3Addresses, int res) {
    List<Long> h3 = stringToH3List(h3Addresses);
    List<Long> uncompacted = uncompactCells(h3, res);
    return h3ToStringList(uncompacted);
  }

  /** Uncompacts all the given indexes to resolution <code>res</code>. */
  public List<Long> uncompactCells(Collection<Long> h3, int res) {
    checkResolution(res);

    long[] h3AsArray = collectionToLongArray(h3);

    int sz = longToIntSize(h3Api.uncompactCellsSize(h3AsArray, res));

    long[] out = new long[sz];

    h3Api.uncompactCells(h3AsArray, res, out);

    return nonZeroLongArrayToList(out);
  }

  /**
   * Converts from <code>long</code> representation of an index to <code>String</code>
   * representation.
   */
  public String h3ToString(long h3) {
    return Long.toHexString(h3);
  }

  /**
   * Converts from <code>String</code> representation of an index to <code>long</code>
   * representation.
   */
  public long stringToH3(String h3Address) {
    return Long.parseUnsignedLong(h3Address, 16);
  }

  /**
   * Calculates the area of the given H3 cell.
   *
   * @param h3Address Cell to find the area of.
   * @param unit Unit to calculate the area in.
   * @return Cell area in the given units.
   */
  public double cellArea(String h3Address, AreaUnit unit) {
    return cellArea(stringToH3(h3Address), unit);
  }

  /**
   * Calculates the area of the given H3 cell.
   *
   * @param h3 Cell to find the area of.
   * @param unit Unit to calculate the area in.
   * @return Cell area in the given units.
   */
  public double cellArea(long h3, AreaUnit unit) {
    if (unit == AreaUnit.rads2) return h3Api.cellAreaRads2(h3);
    else if (unit == AreaUnit.km2) return h3Api.cellAreaKm2(h3);
    else if (unit == AreaUnit.m2) return h3Api.cellAreaM2(h3);
    else throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
  }

  /**
   * Return the distance along the sphere between two points.
   *
   * @param a First point
   * @param b Second point
   * @param unit Unit to return the distance in.
   * @return Distance from point <code>a</code> to point <code>b</code>
   */
  public double greatCircleDistance(LatLng a, LatLng b, LengthUnit unit) {
    double lat1 = toRadians(a.lat);
    double lng1 = toRadians(a.lng);
    double lat2 = toRadians(b.lat);
    double lng2 = toRadians(b.lng);

    if (unit == LengthUnit.rads) return h3Api.greatCircleDistanceRads(lat1, lng1, lat2, lng2);
    else if (unit == LengthUnit.km) return h3Api.greatCircleDistanceKm(lat1, lng1, lat2, lng2);
    else if (unit == LengthUnit.m) return h3Api.greatCircleDistanceM(lat1, lng1, lat2, lng2);
    else throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
  }

  /**
   * Calculate the edge length of the given H3 edge.
   *
   * @param edgeAddress Edge to find the edge length of.
   * @param unit Unit of measure to use.
   * @return Length of the given edge.
   */
  public double edgeLength(String edgeAddress, LengthUnit unit) {
    return edgeLength(stringToH3(edgeAddress), unit);
  }

  /**
   * Calculate the edge length of the given H3 edge.
   *
   * @param edge Edge to find the edge length of.
   * @param unit Unit of measure to use.
   * @return Length of the given edge.
   */
  public double edgeLength(long edge, LengthUnit unit) {
    if (unit == LengthUnit.rads) return h3Api.edgeLengthRads(edge);
    else if (unit == LengthUnit.km) return h3Api.edgeLengthKm(edge);
    else if (unit == LengthUnit.m) return h3Api.edgeLengthM(edge);
    else throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
  }

  /**
   * Returns the average area in <code>unit</code> for indexes at resolution <code>res</code>.
   *
   * @throws IllegalArgumentException Invalid parameter value
   */
  public double getHexagonAreaAvg(int res, AreaUnit unit) {
    checkResolution(res);
    if (unit == AreaUnit.km2) return h3Api.getHexagonAreaAvgKm2(res);
    else if (unit == AreaUnit.m2) return h3Api.getHexagonAreaAvgM2(res);
    else throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
  }

  /**
   * Returns the average edge length in <code>unit</code> for indexes at resolution <code>res</code>
   * .
   *
   * @throws IllegalArgumentException Invalid parameter value
   */
  public double getHexagonEdgeLengthAvg(int res, LengthUnit unit) {
    checkResolution(res);
    if (unit == LengthUnit.km) return h3Api.getHexagonEdgeLengthAvgKm(res);
    else if (unit == LengthUnit.m) return h3Api.getHexagonEdgeLengthAvgM(res);
    else throw new IllegalArgumentException(String.format("Invalid unit: %s", unit));
  }

  /**
   * Returns the number of unique H3 indexes at resolution <code>res</code>.
   *
   * @throws IllegalArgumentException Invalid resolution
   */
  public long getNumCells(int res) {
    checkResolution(res);
    return h3Api.getNumCells(res);
  }

  /** Returns a collection of all base cells (H3 indexes are resolution 0). */
  public Collection<String> getRes0CellAddresses() {
    return h3ToStringList(getRes0Cells());
  }

  /** Returns a collection of all base cells (H3 indexes are resolution 0). */
  public Collection<Long> getRes0Cells() {
    long[] indexes = new long[NUM_BASE_CELLS];
    h3Api.getRes0Cells(indexes);
    return nonZeroLongArrayToList(indexes);
  }

  /**
   * Returns a collection of all topologically pentagonal cells at the given resolution.
   *
   * @throws IllegalArgumentException Invalid resolution.
   */
  public Collection<String> getPentagonAddresses(int res) {
    return h3ToStringList(getPentagons(res));
  }

  /**
   * Returns a collection of all topologically pentagonal cells at the given resolution.
   *
   * @throws IllegalArgumentException Invalid resolution.
   */
  public Collection<Long> getPentagons(int res) {
    checkResolution(res);
    long[] indexes = new long[NUM_PENTAGONS];
    h3Api.getPentagons(res, indexes);
    return nonZeroLongArrayToList(indexes);
  }

  /** Returns <code>true</code> if the two indexes are neighbors. */
  public boolean areNeighborCells(long a, long b) {
    return h3Api.areNeighborCells(a, b);
  }

  /** Returns <code>true</code> if the two indexes are neighbors. */
  public boolean areNeighborCells(String a, String b) {
    return areNeighborCells(stringToH3(a), stringToH3(b));
  }

  /**
   * Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>.
   *
   * @throws IllegalArgumentException The indexes are not neighbors.
   */
  public long cellsToDirectedEdge(long a, long b) {
    return h3Api.cellsToDirectedEdge(a, b);
  }

  /**
   * Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>.
   *
   * @throws IllegalArgumentException The indexes are not neighbors.
   */
  public String cellsToDirectedEdge(String a, String b) {
    return h3ToString(cellsToDirectedEdge(stringToH3(a), stringToH3(b)));
  }

  /** Returns <code>true</code> if the given index is a valid unidirectional edge. */
  public boolean isValidDirectedEdge(long h3) {
    return h3Api.isValidDirectedEdge(h3);
  }

  /** Returns <code>true</code> if the given index is a valid unidirectional edge. */
  public boolean isValidDirectedEdge(String h3) {
    return isValidDirectedEdge(stringToH3(h3));
  }

  /** Returns the origin index of the given unidirectional edge. */
  public long getDirectedEdgeOrigin(long h3) {
    return h3Api.getDirectedEdgeOrigin(h3);
  }

  /** Returns the origin index of the given unidirectional edge. */
  public String getDirectedEdgeOrigin(String h3) {
    return h3ToString(getDirectedEdgeOrigin(stringToH3(h3)));
  }

  /** Returns the destination index of the given unidirectional edge. */
  public long getDirectedEdgeDestination(long h3) {
    return h3Api.getDirectedEdgeDestination(h3);
  }

  /** Returns the destination index of the given unidirectional edge. */
  public String getDirectedEdgeDestination(String h3) {
    return h3ToString(getDirectedEdgeDestination(stringToH3(h3)));
  }

  /**
   * Returns the origin and destination indexes (in that order) of the given unidirectional edge.
   */
  public List<Long> directedEdgeToCells(long h3) {
    long[] results = new long[2];

    // TODO: could be a pair type
    h3Api.directedEdgeToCells(h3, results);

    return nonZeroLongArrayToList(results);
  }

  /**
   * Returns the origin and destination indexes (in that order) of the given unidirectional edge.
   */
  public List<String> directedEdgeToCells(String h3) {
    return h3ToStringList(directedEdgeToCells(stringToH3(h3)));
  }

  /** Returns all unidirectional edges originating from the given index. */
  public List<Long> originToDirectedEdges(long h3) {
    long[] results = new long[6];

    h3Api.originToDirectedEdges(h3, results);

    return nonZeroLongArrayToList(results);
  }

  /** Returns all unidirectional edges originating from the given index. */
  public List<String> originToDirectedEdges(String h3) {
    return h3ToStringList(originToDirectedEdges(stringToH3(h3)));
  }

  /** Returns a list of coordinates representing the given edge. */
  public List<LatLng> directedEdgeToBoundary(long h3) {
    double[] verts = new double[MAX_CELL_BNDRY_VERTS * 2];
    int numVerts = h3Api.directedEdgeToBoundary(h3, verts);
    List<LatLng> out = new ArrayList<>(numVerts);
    for (int i = 0; i < numVerts; i++) {
      LatLng coord = new LatLng(toDegrees(verts[i * 2]), toDegrees(verts[(i * 2) + 1]));
      out.add(coord);
    }
    return out;
  }

  /** Returns a list of coordinates representing the given edge. */
  public List<LatLng> directedEdgeToBoundary(String h3) {
    return directedEdgeToBoundary(stringToH3(h3));
  }

  /**
   * Find all icosahedron faces intersected by a given H3 index, represented as integers from 0-19.
   *
   * @param h3 Index to find icosahedron faces for.
   * @return A collection of faces intersected by the index.
   */
  public Collection<Integer> getIcosahedronFaces(String h3) {
    return getIcosahedronFaces(stringToH3(h3));
  }

  /**
   * Find all icosahedron faces intersected by a given H3 index, represented as integers from 0-19.
   *
   * @param h3 Index to find icosahedron faces for.
   * @return A collection of faces intersected by the index.
   */
  public Collection<Integer> getIcosahedronFaces(long h3) {
    int maxFaces = h3Api.maxFaceCount(h3);
    int[] faces = new int[maxFaces];

    h3Api.getIcosahedronFaces(h3, faces);

    return IntStream.of(faces).filter(f -> f != -1).boxed().collect(Collectors.toList());
  }

  public long cellToVertex(long h3, int vertexNum) {
    return h3Api.cellToVertex(h3, vertexNum);
  }

  public String cellToVertex(String h3Address, int vertexNum) {
    return h3ToString(h3Api.cellToVertex(stringToH3(h3Address), vertexNum));
  }

  public List<Long> cellToVertexes(long h3) {
    long[] results = new long[6];
    h3Api.cellToVertexes(h3, results);
    return nonZeroLongArrayToList(results);
  }

  public List<String> cellToVertexes(String h3Address) {
    return h3ToStringList(cellToVertexes(stringToH3(h3Address)));
  }

  public LatLng vertexToLatLng(long h3) {
    double[] results = new double[2];
    h3Api.vertexToLatLng(h3, results);
    return new LatLng(toDegrees(results[0]), toDegrees(results[1]));
  }

  public LatLng vertexToLatLng(String h3Address) {
    return vertexToLatLng(stringToH3(h3Address));
  }

  public boolean isValidVertex(long h3) {
    return h3Api.isValidVertex(h3);
  }

  public boolean isValidVertex(String h3Address) {
    return h3Api.isValidVertex(stringToH3(h3Address));
  }

  /**
   * Returns the position of the child cell within an ordered list of all children of the cell's
   * parent at the specified resolution parentRes.
   */
  public long cellToChildPos(String childAddress, int parentRes) {
    return cellToChildPos(stringToH3(childAddress), parentRes);
  }

  /**
   * Returns the position of the child cell within an ordered list of all children of the cell's
   * parent at the specified resolution parentRes.
   */
  public long cellToChildPos(long child, int parentRes) {
    return h3Api.cellToChildPos(child, parentRes);
  }

  /**
   * Returns the child cell at a given position within an ordered list of all children of parent at
   * the specified resolution childRes.
   */
  public long childPosToCell(long childPos, long parent, int childRes) {
    return h3Api.childPosToCell(childPos, parent, childRes);
  }

  /**
   * Returns the child cell at a given position within an ordered list of all children of parent at
   * the specified resolution childRes.
   */
  public String childPosToCell(long childPos, String parentAddress, int childRes) {
    return h3ToString(childPosToCell(childPos, stringToH3(parentAddress), childRes));
  }

  /** Transforms a collection of H3 indexes in string form to a list of H3 indexes in long form. */
  private List<Long> stringToH3List(Collection<String> collection) {
    return collection.stream().map(this::stringToH3).collect(Collectors.toList());
  }

  /** Transforms a list of H3 indexes in long form to a list of H3 indexes in string form. */
  private List<String> h3ToStringList(Collection<Long> collection) {
    return collection.stream().map(this::h3ToString).collect(Collectors.toList());
  }

  /** Creates a new list with all non-zero elements of the array as members. */
  private static List<Long> nonZeroLongArrayToList(long[] out) {
    List<Long> ret = new ArrayList<>();

    for (int i = 0; i < out.length; i++) {
      long h = out[i];
      if (h != 0) {
        ret.add(h);
      }
    }

    return ret;
  }

  /** Returns an array of <code>long</code> with the contents of the collection. */
  private static long[] collectionToLongArray(Collection<Long> collection) {
    return collection.stream().mapToLong(Long::longValue).toArray();
  }

  /**
   * @throws IllegalArgumentException <code>res</code> is not a valid H3 resolution.
   */
  private static void checkResolution(int res) {
    if (res < 0 || res > 15) {
      throw new IllegalArgumentException(
          String.format("resolution %d is out of range (must be 0 <= res <= 15)", res));
    }
  }

  /**
   * @throws IllegalArgumentException <code>sz</code> cannot be losslessly cast to an <code>int
   *     </code>
   */
  private static int longToIntSize(long sz) {
    if (sz < 0 || sz > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(String.format("size %d is out of range", sz));
    }
    return (int) sz;
  }
}
