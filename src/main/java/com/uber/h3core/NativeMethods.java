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

import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to native code. Implementation of these functions is in <code>
 * src/main/c/h3-java/src/jniapi.c</code>.
 */
final class NativeMethods {
  NativeMethods() {
    // Only H3CoreLoader is expected to instantiate
  }

  native long cellToChildrenSize(long h3, int childRes);

  native void cellToChildren(long h3, int childRes, long[] results);

  native long cellToCenterChild(long h3, int childRes);

  native boolean isValidCell(long h3);

  native int getBaseCellNumber(long h3);

  native boolean isPentagon(long h3);

  native long latLngToCell(double lat, double lon, int res);

  native void cellToLatLng(long h3, double[] verts);

  native int cellToBoundary(long h3, double[] verts);

  native long maxGridDiskSize(int k);

  native void gridDisk(long h3, int k, long[] results);

  native void gridDiskDistances(long h3, int k, long[] results, int[] distances);

  native void gridDiskUnsafe(long h3, int k, long[] results);

  native void gridRingUnsafe(long h3, int k, long[] results);

  native long gridDistance(long a, long b);

  native void cellToLocalIj(long origin, long h3, int[] coords);

  native long localIjToCell(long origin, int i, int j);

  native long gridPathCellsSize(long start, long end);

  native void gridPathCells(long start, long end, long[] results);

  native long maxPolygonToCellsSize(
      double[] verts, int[] holeSizes, double[] holeVerts, int res, int flags);

  native void polygonToCells(
      double[] verts, int[] holeSizes, double[] holeVerts, int res, int flags, long[] results);

  native void cellsToLinkedMultiPolygon(long[] h3, ArrayList<List<List<LatLng>>> results);

  native void compactCells(long[] h3, long[] results);

  native long uncompactCellsSize(long[] h3, int res);

  native void uncompactCells(long[] h3, int res, long[] results);

  native double cellAreaRads2(long h3);

  native double cellAreaKm2(long h3);

  native double cellAreaM2(long h3);

  native double greatCircleDistanceRads(double lat1, double lon1, double lat2, double lon2);

  native double greatCircleDistanceKm(double lat1, double lon1, double lat2, double lon2);

  native double greatCircleDistanceM(double lat1, double lon1, double lat2, double lon2);

  native double exactEdgeLengthRads(long h3);

  native double exactEdgeLengthKm(long h3);

  native double exactEdgeLengthM(long h3);

  native double getHexagonAreaAvgKm2(int res);

  native double getHexagonAreaAvgM2(int res);

  native double getHexagonEdgeLengthAvgKm(int res);

  native double getHexagonEdgeLengthAvgM(int res);

  native long getNumCells(int res);

  native void getRes0Cells(long[] indexes);

  native void getPentagons(int res, long[] h3);

  native boolean areNeighborCells(long a, long b);

  native long cellsToDirectedEdge(long a, long b);

  native boolean isValidDirectedEdge(long h3);

  native long getDirectedEdgeOrigin(long h3);

  native long getDirectedEdgeDestination(long h3);

  native void directedEdgeToCells(long h3, long[] results);

  native void originToDirectedEdges(long h3, long[] results);

  native int directedEdgeToBoundary(long h3, double[] verts);

  native int maxFaceCount(long h3);

  native void getIcosahedronFaces(long h3, int[] faces);

  native long cellToVertex(long h3, int vertexNum);

  native void cellToVertexes(long h3, long[] results);

  native void vertexToLatLng(long h3, double[] latLng);

  native boolean isValidVertex(long h3);
}
