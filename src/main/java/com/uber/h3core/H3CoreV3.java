/*
 * Copyright 2017-2019, 2022 Uber Technologies, Inc.
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

import com.uber.h3core.util.CoordIJ;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * H3Core provides all functions of the H3 API.
 *
 * <p>This class is thread safe and can be used as a singleton.
 *
 * <p>This class provides backwards compatability with the V3 API.
 */
public class H3CoreV3 {
  /** Native implementation of the H3 library. */
  private final H3Core h3Api;

  /**
   * Create by unpacking the H3 native library to disk and loading it. The library will attempt to
   * detect the correct operating system and architecture of native library to unpack.
   *
   * @throws SecurityException Loading the library was not allowed by the SecurityManager.
   * @throws UnsatisfiedLinkError The library could not be loaded
   * @throws IOException The library could not be extracted to disk.
   */
  public static H3CoreV3 newInstance() throws IOException {
    return new H3CoreV3(H3Core.newInstance());
  }

  /**
   * Create by unpacking the H3 native library to disk and loading it. The library will attempt to
   * extract the native library matching the given arguments to disk.
   *
   * @throws SecurityException Loading the library was not allowed by the SecurityManager.
   * @throws UnsatisfiedLinkError The library could not be loaded
   * @throws IOException The library could not be extracted to disk.
   */
  public static H3CoreV3 newInstance(H3CoreLoader.OperatingSystem os, String arch)
      throws IOException {
    return new H3CoreV3(H3Core.newInstance(os, arch));
  }

  /**
   * Create by using the H3 native library already installed on the system.
   *
   * @throws SecurityException The library could not be loaded
   * @throws UnsatisfiedLinkError The library could not be loaded
   */
  public static H3CoreV3 newSystemInstance() {
    return new H3CoreV3(H3Core.newSystemInstance());
  }

  /** Construct with the given NativeMethods, from {@link H3CoreLoader}. */
  private H3CoreV3(H3Core h3Api) {
    this.h3Api = h3Api;
  }

  /** Returns true if this is a valid H3 index. */
  public boolean h3IsValid(long h3) {
    return h3Api.isValidCell(h3);
  }

  /** Returns true if this is a valid H3 index. */
  public boolean h3IsValid(String h3Address) {
    return h3Api.isValidCell(h3Address);
  }

  /** Returns the base cell number for this index. */
  public int h3GetBaseCell(long h3) {
    return h3Api.getBaseCellNumber(h3);
  }

  /** Returns the base cell number for this index. */
  public int h3GetBaseCell(String h3Address) {
    return h3Api.getBaseCellNumber(h3Address);
  }

  /** Returns <code>true</code> if this index is one of twelve pentagons per resolution. */
  public boolean h3IsPentagon(long h3) {
    return h3Api.isPentagon(h3);
  }

  /** Returns <code>true</code> if this index is one of twelve pentagons per resolution. */
  public boolean h3IsPentagon(String h3Address) {
    return h3Api.isPentagon(h3Address);
  }

  /**
   * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
   *
   * @param lat Latitude in degrees.
   * @param lng Longitude in degrees.
   * @param res Resolution, 0 &lt;= res &lt;= 15
   * @return The H3 index.
   */
  public long geoToH3(double lat, double lng, int res) {
    return h3Api.latLngToCell(lat, lng, res);
  }

  /**
   * Find the H3 index of the resolution <code>res</code> cell containing the lat/lon (in degrees)
   *
   * @param lat Latitude in degrees.
   * @param lng Longitude in degrees.
   * @param res Resolution, 0 &lt;= res &lt;= 15
   * @return The H3 index.
   */
  public String geoToH3Address(double lat, double lng, int res) {
    return h3Api.latLngToCellAddress(lat, lng, res);
  }

  /** Find the latitude, longitude (both in degrees) center point of the cell. */
  public LatLng h3ToGeo(long h3) {
    return h3Api.cellToLatLng(h3);
  }

  /** Find the latitude, longitude (degrees) center point of the cell. */
  public LatLng h3ToGeo(String h3Address) {
    return h3Api.cellToLatLng(h3Address);
  }

  /** Find the cell boundary in latitude, longitude (degrees) coordinates for the cell */
  public List<LatLng> h3ToGeoBoundary(long h3) {
    return h3Api.cellToBoundary(h3);
  }

  /** Find the cell boundary in latitude, longitude (degrees) coordinates for the cell */
  public List<LatLng> h3ToGeoBoundary(String h3Address) {
    return h3Api.cellToBoundary(h3Address);
  }

  /**
   * Neighboring indexes in all directions.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   */
  public List<String> kRing(String h3Address, int k) {
    return h3Api.gridDisk(h3Address, k);
  }

  /**
   * Neighboring indexes in all directions.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
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
   * @param k Number of rings around the origin
   */
  public List<Long> kRing(long h3, int k) {
    return h3Api.gridDisk(h3, k);
  }

  /**
   * Neighboring indexes in all directions, ordered by distance from the origin index.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<String>> kRingDistances(String h3Address, int k) {
    return h3Api.gridDiskDistances(h3Address, k);
  }

  /**
   * Neighboring indexes in all directions, ordered by distance from the origin index.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<Long>> kRingDistances(long h3, int k) {
    return h3Api.gridDiskDistances(h3, k);
  }

  /**
   * Returns in order neighbor traversal.
   *
   * @param h3Address Origin hexagon index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<String>> hexRange(String h3Address, int k) {
    return h3Api.gridDiskUnsafe(h3Address, k);
  }

  /**
   * Returns in order neighbor traversal.
   *
   * @param h3 Origin hexagon index
   * @param k Number of rings around the origin
   * @return A list of rings, each of which is a list of addresses. The rings are in order from
   *     closest to origin to farthest.
   */
  public List<List<Long>> hexRange(long h3, int k) {
    return h3Api.gridDiskUnsafe(h3, k);
  }

  /**
   * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
   *
   * @param h3Address Origin index
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<String> hexRing(String h3Address, int k) {
    return h3Api.gridRingUnsafe(h3Address, k);
  }

  /**
   * Returns in order neighbor traversal, of indexes with distance of <code>k</code>.
   *
   * @param h3 Origin index
   * @param k Number of rings around the origin
   * @return All indexes <code>k</code> away from the origin
   */
  public List<Long> hexRing(long h3, int k) {
    return h3Api.gridRingUnsafe(h3, k);
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
  public int h3Distance(String a, String b) {
    return longToIntDistance(h3Api.gridDistance(a, b));
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
  public int h3Distance(long a, long b) {
    return longToIntDistance(h3Api.gridDistance(a, b));
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
  public CoordIJ experimentalH3ToLocalIj(long origin, long h3) {
    return h3Api.experimentalH3ToLocalIj(origin, h3);
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
  public CoordIJ experimentalH3ToLocalIj(String originAddress, String h3Address) {
    return h3Api.experimentalH3ToLocalIj(originAddress, h3Address);
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
  public long experimentalLocalIjToH3(long origin, CoordIJ ij) {
    return h3Api.experimentalLocalIjToH3(origin, ij);
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
  public String experimentalLocalIjToH3(String originAddress, CoordIJ ij) {
    return h3Api.experimentalLocalIjToH3(originAddress, ij);
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
  public List<String> h3Line(String startAddress, String endAddress) {
    return h3Api.gridPathCells(startAddress, endAddress);
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
  public List<Long> h3Line(long start, long end) {
    return h3Api.gridPathCells(start, end);
  }

  /**
   * Finds indexes within the given geofence.
   *
   * @param points Outline geofence
   * @param holes Geofences of any internal holes
   * @param res Resolution of the desired indexes
   */
  public List<String> polyfillAddress(List<LatLng> points, List<List<LatLng>> holes, int res) {
    return h3Api.polygonToCellAddresses(points, holes, res);
  }

  /**
   * Finds indexes within the given geofence.
   *
   * @param points Outline geofence
   * @param holes Geofences of any internal holes
   * @param res Resolution of the desired indexes
   */
  public List<Long> polyfill(List<LatLng> points, List<List<LatLng>> holes, int res) {
    return h3Api.polygonToCells(points, holes, res);
  }

  /** Create polygons from a set of contiguous indexes */
  public List<List<List<LatLng>>> h3AddressSetToMultiPolygon(
      Collection<String> h3Addresses, boolean geoJson) {
    return h3Api.cellAddressesToMultiPolygon(h3Addresses, geoJson);
  }

  /** Create polygons from a set of contiguous indexes */
  public List<List<List<LatLng>>> h3SetToMultiPolygon(Collection<Long> h3, boolean geoJson) {
    return h3Api.cellsToMultiPolygon(h3, geoJson);
  }

  /** Returns the resolution of the provided index */
  public int h3GetResolution(String h3Address) {
    return h3Api.getResolution(h3Address);
  }

  /** Returns the resolution of the provided index */
  public int h3GetResolution(long h3) {
    return h3Api.getResolution(h3);
  }

  /**
   * Returns the parent of the index at the given resolution.
   *
   * @param h3 H3 index.
   * @param res Resolution of the parent, <code>0 &lt;= res &lt;= h3GetResolution(h3)</code>
   * @throws IllegalArgumentException <code>res</code> is not between 0 and the resolution of <code>
   *     h3</code>, inclusive.
   */
  public long h3ToParent(long h3, int res) {
    return h3Api.cellToParent(h3, res);
  }

  /**
   * Returns the parent of the index at the given resolution.
   *
   * @param h3Address H3 index.
   * @param res Resolution of the parent, <code>0 &lt;= res &lt;= h3GetResolution(h3)</code>
   */
  public String h3ToParentAddress(String h3Address, int res) {
    return h3Api.cellToParentAddress(h3Address, res);
  }

  /**
   * Provides the children of the index at the given resolution.
   *
   * @param childRes Resolution of the children
   */
  public List<String> h3ToChildren(String h3Address, int childRes) {
    return h3Api.cellToChildren(h3Address, childRes);
  }

  /**
   * Provides the children of the index at the given resolution.
   *
   * @param h3 H3 index.
   * @param childRes Resolution of the children
   * @throws IllegalArgumentException Invalid resolution
   */
  public List<Long> h3ToChildren(long h3, int childRes) {
    return h3Api.cellToChildren(h3, childRes);
  }

  /**
   * Returns the center child at the given resolution.
   *
   * @param h3 Parent H3 index
   * @param childRes Resolution of the child
   */
  public String h3ToCenterChild(String h3, int childRes) {
    return h3Api.cellToCenterChild(h3, childRes);
  }

  /**
   * Returns the center child at the given resolution.
   *
   * @param h3 Parent H3 index
   * @param childRes Resolution of the child
   */
  public long h3ToCenterChild(long h3, int childRes) {
    return h3Api.cellToCenterChild(h3, childRes);
  }

  /**
   * Determines if an index is Class III or Class II.
   *
   * @return <code>true</code> if the index is Class III
   */
  public boolean h3IsResClassIII(String h3Address) {
    return h3Api.isResClassIII(h3Address);
  }

  /**
   * Determines if an index is Class III or Class II.
   *
   * @param h3 H3 index.
   * @return <code>true</code> if the index is Class III
   */
  public boolean h3IsResClassIII(long h3) {
    return h3Api.isResClassIII(h3);
  }

  /** Returns a compacted set of indexes, at possibly coarser resolutions. */
  public List<String> compactAddress(Collection<String> h3Addresses) {
    return h3Api.compactCellAddresses(h3Addresses);
  }

  /** Returns a compacted set of indexes, at possibly coarser resolutions. */
  public List<Long> compact(Collection<Long> h3) {
    return h3Api.compactCells(h3);
  }

  /** Uncompacts all the given indexes to resolution <code>res</code>. */
  public List<String> uncompactAddress(Collection<String> h3Addresses, int res) {
    return h3Api.uncompactCellAddresses(h3Addresses, res);
  }

  /** Uncompacts all the given indexes to resolution <code>res</code>. */
  public List<Long> uncompact(Collection<Long> h3, int res) {
    return h3Api.uncompactCells(h3, res);
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
    return h3Api.cellArea(h3Address, unit);
  }

  /**
   * Calculates the area of the given H3 cell.
   *
   * @param h3 Cell to find the area of.
   * @param unit Unit to calculate the area in.
   * @return Cell area in the given units.
   */
  public double cellArea(long h3, AreaUnit unit) {
    return h3Api.cellArea(h3, unit);
  }

  /**
   * Return the distance along the sphere between two points.
   *
   * @param a First point
   * @param b Second point
   * @param unit Unit to return the distance in.
   * @return Distance from point <code>a</code> to point <code>b</code>
   */
  public double pointDist(LatLng a, LatLng b, LengthUnit unit) {
    return h3Api.distance(a, b, unit);
  }

  /**
   * Calculate the edge length of the given H3 edge.
   *
   * @param edgeAddress Edge to find the edge length of.
   * @param unit Unit of measure to use.
   * @return Length of the given edge.
   */
  public double exactEdgeLength(String edgeAddress, LengthUnit unit) {
    return h3Api.exactEdgeLength(edgeAddress, unit);
  }

  /**
   * Calculate the edge length of the given H3 edge.
   *
   * @param edge Edge to find the edge length of.
   * @param unit Unit of measure to use.
   * @return Length of the given edge.
   */
  public double exactEdgeLength(long edge, LengthUnit unit) {
    return h3Api.exactEdgeLength(edge, unit);
  }

  /**
   * Returns the average area in <code>unit</code> for indexes at resolution <code>res</code>.
   *
   * @throws IllegalArgumentException Invalid parameter value
   */
  public double hexArea(int res, AreaUnit unit) {
    return h3Api.getHexagonAreaAvg(res, unit);
  }

  /**
   * Returns the average edge length in <code>unit</code> for indexes at resolution <code>res</code>
   * .
   *
   * @throws IllegalArgumentException Invalid parameter value
   */
  public double edgeLength(int res, LengthUnit unit) {
    return h3Api.getHexagonEdgeLengthAvg(res, unit);
  }

  /** Returns the number of unique H3 indexes at resolution <code>res</code>. */
  public long numHexagons(int res) {
    return h3Api.getNumCells(res);
  }

  /** Returns a collection of all base cells (H3 indexes are resolution 0). */
  public Collection<String> getRes0IndexesAddresses() {
    return h3Api.getRes0CellAddresses();
  }

  /** Returns a collection of all base cells (H3 indexes are resolution 0). */
  public Collection<Long> getRes0Indexes() {
    return h3Api.getRes0Cells();
  }

  /** Returns a collection of all topologically pentagonal cells at the given resolution. */
  public Collection<String> getPentagonIndexesAddresses(int res) {
    return h3Api.getPentagonAddresses(res);
  }

  /** Returns a collection of all topologically pentagonal cells at the given resolution. */
  public Collection<Long> getPentagonIndexes(int res) {
    return h3Api.getPentagons(res);
  }

  /** Returns <code>true</code> if the two indexes are neighbors. */
  public boolean h3IndexesAreNeighbors(long a, long b) {
    return h3Api.areNeighborCells(a, b);
  }

  /** Returns <code>true</code> if the two indexes are neighbors. */
  public boolean h3IndexesAreNeighbors(String a, String b) {
    return h3Api.areNeighborCells(a, b);
  }

  /** Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>. */
  public long getH3UnidirectionalEdge(long a, long b) {
    return h3Api.cellsToDirectedEdge(a, b);
  }

  /** Returns a unidirectional edge index representing <code>a</code> towards <code>b</code>. */
  public String getH3UnidirectionalEdge(String a, String b) {
    return h3Api.cellsToDirectedEdge(a, b);
  }

  /** Returns <code>true</code> if the given index is a valid unidirectional edge. */
  public boolean h3UnidirectionalEdgeIsValid(long h3) {
    return h3Api.isValidDirectedEdge(h3);
  }

  /** Returns <code>true</code> if the given index is a valid unidirectional edge. */
  public boolean h3UnidirectionalEdgeIsValid(String h3) {
    return h3Api.isValidDirectedEdge(h3);
  }

  /** Returns the origin index of the given unidirectional edge. */
  public long getOriginH3IndexFromUnidirectionalEdge(long h3) {
    return h3Api.getDirectedEdgeOrigin(h3);
  }

  /** Returns the origin index of the given unidirectional edge. */
  public String getOriginH3IndexFromUnidirectionalEdge(String h3) {
    return h3Api.getDirectedEdgeOrigin(h3);
  }

  /** Returns the destination index of the given unidirectional edge. */
  public long getDestinationH3IndexFromUnidirectionalEdge(long h3) {
    return h3Api.getDirectedEdgeDestination(h3);
  }

  /** Returns the destination index of the given unidirectional edge. */
  public String getDestinationH3IndexFromUnidirectionalEdge(String h3) {
    return h3Api.getDirectedEdgeDestination(h3);
  }

  /**
   * Returns the origin and destination indexes (in that order) of the given unidirectional edge.
   */
  public List<Long> getH3IndexesFromUnidirectionalEdge(long h3) {
    return h3Api.directedEdgeToCells(h3);
  }

  /**
   * Returns the origin and destination indexes (in that order) of the given unidirectional edge.
   */
  public List<String> getH3IndexesFromUnidirectionalEdge(String h3) {
    return h3Api.directedEdgeToCells(h3);
  }

  /** Returns all unidirectional edges originating from the given index. */
  public List<Long> getH3UnidirectionalEdgesFromHexagon(long h3) {
    return h3Api.originToDirectedEdges(h3);
  }

  /** Returns all unidirectional edges originating from the given index. */
  public List<String> getH3UnidirectionalEdgesFromHexagon(String h3) {
    return h3Api.originToDirectedEdges(h3);
  }

  /** Returns a list of coordinates representing the given edge. */
  public List<LatLng> getH3UnidirectionalEdgeBoundary(long h3) {
    return h3Api.directedEdgeToBoundary(h3);
  }

  /** Returns a list of coordinates representing the given edge. */
  public List<LatLng> getH3UnidirectionalEdgeBoundary(String h3) {
    return h3Api.directedEdgeToBoundary(h3);
  }

  /**
   * Find all icosahedron faces intersected by a given H3 index, represented as integers from 0-19.
   *
   * @param h3 Index to find icosahedron faces for.
   * @return A collection of faces intersected by the index.
   */
  public Collection<Integer> h3GetFaces(String h3) {
    return h3Api.getIcosahedronFaces(h3);
  }

  /**
   * Find all icosahedron faces intersected by a given H3 index, represented as integers from 0-19.
   *
   * @param h3 Index to find icosahedron faces for.
   * @return A collection of faces intersected by the index.
   */
  public Collection<Integer> h3GetFaces(long h3) {
    return h3Api.getIcosahedronFaces(h3);
  }

  /**
   * @throws IllegalArgumentException <code>sz</code> cannot be losslessly cast to an <code>int
   *     </code>
   */
  private static int longToIntDistance(long sz) {
    if (sz < 0 || sz > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(String.format("Distance %d is out of range", sz));
    }
    return (int) sz;
  }
}
