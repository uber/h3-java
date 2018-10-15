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

import com.uber.h3core.util.CoordIJ;
import com.uber.h3core.util.GeoCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to native code. Implementation of these functions is in
 * <code>src/main/c/h3-java/src/jniapi.c</code>.
 */
final class NativeMethods {
    NativeMethods() {
        // Prevent instantiation
    }

    native int maxH3ToChildrenSize(long h3, int childRes);
    native void h3ToChildren(long h3, int childRes, long[] results);

    native boolean h3IsValid(long h3);
    native int h3GetBaseCell(long h3);
    native boolean h3IsPentagon(long h3);
    native long geoToH3(double lat, double lon, int res);
    native void h3ToGeo(long h3, double[] verts);
    native int h3ToGeoBoundary(long h3, double[] verts);

    native int maxKringSize(int k);
    native void kRing(long h3, int k, long[] results);
    native void kRingDistances(long h3, int k, long[] results, int[] distances);
    native int hexRange(long h3, int k, long[] results);
    native int hexRing(long h3, int k, long[] results);

    native int h3Distance(long a, long b);
    native int experimentalH3ToLocalIj(long origin, long h3, int[] coords);
    native long experimentalLocalIjToH3(long origin, int i, int j);

    native int maxPolyfillSize(double[] verts, int[] holeSizes, double[] holeVerts, int res);
    native void polyfill(double[] verts, int[] holeSizes, double[] holeVerts, int res, long[] results);

    native void h3SetToLinkedGeo(long[] h3, ArrayList<List<List<GeoCoord>>> results);

    native int compact(long[] h3, long[] results);
    native int maxUncompactSize(long[] h3, int res);
    native int uncompact(long[] h3, int res, long[] results);

    native double hexAreaKm2(int res);
    native double hexAreaM2(int res);
    native double edgeLengthKm(int res);
    native double edgeLengthM(int res);
    native long numHexagons(int res);

    native boolean h3IndexesAreNeighbors(long a, long b);
    native long getH3UnidirectionalEdge(long a, long b);
    native boolean h3UnidirectionalEdgeIsValid(long h3);
    native long getOriginH3IndexFromUnidirectionalEdge(long h3);
    native long getDestinationH3IndexFromUnidirectionalEdge(long h3);
    native void getH3IndexesFromUnidirectionalEdge(long h3, long[] results);
    native void getH3UnidirectionalEdgesFromHexagon(long h3, long[] results);
    native int getH3UnidirectionalEdgeBoundary(long h3, double[] verts);
}
