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

#include <stdbool.h>
#include "com_uber_h3core_NativeMethods.h"
#include "h3api.h"

/**
 * Maximum number of directions from an H3 index.
 * This is not the same as the maximum number of vertices.
 */
#define MAX_HEX_EDGES 6

/**
 * Return if an exception is pending
 */
#define RETURN_ON_EXCEPTION(env)       \
    if ((**env).ExceptionCheck(env)) { \
        return;                        \
    }

/**
 * Triggers an OutOfMemoryError.
 *
 * Calling function should return the Java control immediately after calling
 * this.
 */
void ThrowOutOfMemoryError(JNIEnv *env) {
    // Alternately, we could call the JNI function FatalError(JNIEnv *env, const
    // char *msg)
    jclass oome = (**env).FindClass(env, "java/lang/OutOfMemoryError");

    if (oome != NULL) {
        jmethodID oomeConstructor =
            (**env).GetMethodID(env, oome, "<init>", "()V");

        if (oomeConstructor != NULL) {
            jthrowable oomeInstance =
                (jthrowable)((**env).NewObject(env, oome, oomeConstructor));

            if (oomeInstance != NULL) {
                (**env).ExceptionClear(env);
                (**env).Throw(env, oomeInstance);
            }
        }
    }
}

/**
 * Populates the given GeoPolygon
 *
 * Returns 0 on success.
 */
int CreateGeoPolygon(JNIEnv *env, jdoubleArray verts, jintArray holeSizes,
                     jdoubleArray holeVerts, GeoPolygon *polygon) {
    // This is the number of doubles, so convert to number of verts
    polygon->geofence.numVerts = (**env).GetArrayLength(env, verts) / 2;
    polygon->geofence.verts = (**env).GetDoubleArrayElements(env, verts, 0);
    if (polygon->geofence.verts != NULL) {
        polygon->numHoles = (**env).GetArrayLength(env, holeSizes);

        polygon->holes = calloc(sizeof(GeoPolygon), polygon->numHoles);
        if (polygon->holes == NULL) {
            ThrowOutOfMemoryError(env);
            return 2;
        }

        jint *holeSizesElements =
            (**env).GetIntArrayElements(env, holeSizes, 0);
        if (holeSizesElements == NULL) {
            free(polygon->holes);
            ThrowOutOfMemoryError(env);
            return 3;
        }

        jdouble *holeVertsElements =
            (**env).GetDoubleArrayElements(env, holeVerts, 0);
        if (holeVertsElements == NULL) {
            free(polygon->holes);
            (**env).ReleaseIntArrayElements(env, holeSizes, holeSizesElements,
                                            0);
            ThrowOutOfMemoryError(env);
            return 4;
        }

        size_t offset = 0;
        for (int i = 0; i < polygon->numHoles; i++) {
            // This is the number of doubles, so convert to number of verts
            polygon->holes[i].numVerts = holeSizesElements[i] / 2;
            polygon->holes[i].verts = holeVertsElements + offset;
            offset += holeSizesElements[i];
        }

        (**env).ReleaseIntArrayElements(env, holeSizes, holeSizesElements, 0);
        // holeVertsElements is not released here because it is still being
        // pointed to by polygon->holes[*].verts. It will be released in
        // DestroyGeoPolygon.

        return 0;
    } else {
        ThrowOutOfMemoryError(env);
        return 1;
    }
}

void DestroyGeoPolygon(JNIEnv *env, jdoubleArray verts,
                       jintArray holeSizesElements, jdoubleArray holeVerts,
                       GeoPolygon *polygon) {
    (**env).ReleaseDoubleArrayElements(env, verts, polygon->geofence.verts, 0);

    if (polygon->numHoles > 0) {
        // The hole verts were pinned only once, so we don't need to iterate.
        (**env).ReleaseDoubleArrayElements(env, holeVerts,
                                           polygon->holes[0].verts, 0);
    }

    free(polygon->holes);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3IsValid
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_h3IsValid(
    JNIEnv *env, jobject thiz, jlong h3) {
    return h3IsValid(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3GetBaseCell
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_h3GetBaseCell(
    JNIEnv *env, jobject thiz, jlong h3) {
    return h3GetBaseCell(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3IsPentagon
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_h3IsPentagon(
    JNIEnv *env, jobject thiz, jlong h3) {
    return h3IsPentagon(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    geoToH3
 * Signature: (DDI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_geoToH3(
    JNIEnv *env, jobject thiz, jdouble lat, jdouble lng, jint res) {
    GeoCoord geo = {lat, lng};
    return geoToH3(&geo, res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3ToGeo
 * Signature: (J[D)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_h3ToGeo(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray verts) {
    GeoCoord coord;
    h3ToGeo(h3, &coord);

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *coordsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (coordsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            coordsElements[0] = coord.lat;
            coordsElements[1] = coord.lon;
        }

        // 0 is the mode
        // reference
        // https://developer.android.com/training/articles/perf-jni.html
        (**env).ReleaseDoubleArrayElements(env, verts, coordsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3ToGeoBoundary
 * Signature: (J[D)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_h3ToGeoBoundary(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray verts) {
    GeoBoundary boundary;
    h3ToGeoBoundary(h3, &boundary);

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *vertsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (vertsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        for (jsize i = 0; i < sz && i < boundary.numVerts * 2; i += 2) {
            vertsElements[i] = boundary.verts[i / 2].lat;
            vertsElements[i + 1] = boundary.verts[i / 2].lon;
        }

        (**env).ReleaseDoubleArrayElements(env, verts, vertsElements, 0);

        return boundary.numVerts;
    } else {
        ThrowOutOfMemoryError(env);
        return -1;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxKringSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_maxKringSize(
    JNIEnv *env, jobject thiz, jint k) {
    return maxKringSize(k);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    kRing
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_kRing(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        kRing(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    kRingDistances
 * Signature: (JI[J[I)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_kRingDistances(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results,
    jintArray distances) {
    bool isOom = false;
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);
    if (resultsElements != NULL) {
        jint *distancesElements =
            (**env).GetIntArrayElements(env, distances, 0);
        if (distancesElements != NULL) {
            // if sz is too small, bad things will happen
            kRingDistances(h3, k, resultsElements, distancesElements);

            (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        } else {
            isOom = true;
        }
        (**env).ReleaseIntArrayElements(env, distances, distancesElements, 0);
    } else {
        isOom = true;
    }

    if (isOom) {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    hexRange
 * Signature: (JI[J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_hexRange(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        int ret = hexRange(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        return ret;
    } else {
        ThrowOutOfMemoryError(env);
        return -1;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    hexRing
 * Signature: (JI[J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_hexRing(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        int ret = hexRing(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        return ret;
    } else {
        ThrowOutOfMemoryError(env);
        return -1;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3Distance
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_h3Distance(
    JNIEnv *env, jobject thiz, jlong a, jlong b) {
    return h3Distance(a, b);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    experimentalH3ToLocalIj
 * Signature: (JJ[I)I
 */
JNIEXPORT int JNICALL
Java_com_uber_h3core_NativeMethods_experimentalH3ToLocalIj(
    JNIEnv *env, jobject thiz, jlong origin, jlong h3, jintArray coords) {
    CoordIJ ij = {0};
    int result = experimentalH3ToLocalIj(origin, h3, &ij);
    if (result != 0) {
        return result;
    }

    jsize sz = (**env).GetArrayLength(env, coords);
    jint *coordsElements = (**env).GetIntArrayElements(env, coords, 0);

    if (coordsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            coordsElements[0] = ij.i;
            coordsElements[1] = ij.j;
        }

        // 0 is the mode
        // reference
        // https://developer.android.com/training/articles/perf-jni.html
        (**env).ReleaseIntArrayElements(env, coords, coordsElements, 0);
        return 0;
    } else {
        ThrowOutOfMemoryError(env);
        return -1;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    experimentalLocalIjToH3
 * Signature: (JII)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_experimentalLocalIjToH3(JNIEnv *env,
                                                           jobject thiz,
                                                           jlong origin, jint i,
                                                           jint j) {
    CoordIJ ij = {.i = i, .j = j};
    H3Index index;
    int result = experimentalLocalIjToH3(origin, &ij, &index);
    if (result != 0) {
        // Exact error is not preserved, just that the operation failed.
        return 0;
    }
    return index;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxPolyfillSize
 * Signature: ([D[I[DI)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_maxPolyfillSize(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return -1;
    }

    int numHexagons = maxPolyfillSize(&polygon, res);

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);

    return numHexagons;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    polyfill
 * Signature: ([D[I[DI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_polyfill(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res, jlongArray results) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return;
    }

    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        polyfill(&polygon, res, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
        return;
    }

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);
}

/**
 * Converts the given polygon to managed objects
 * (ArrayList<ArrayList<ArrayList<GeoCoord>>>)
 *
 * May return early if allocation or finding required classes or methods fails.
 */
void ConvertLinkedGeoPolygonToManaged(JNIEnv *env,
                                      LinkedGeoPolygon *currentPolygon,
                                      jobject results) {
    jclass arrayListClass = (**env).FindClass(env, "java/util/ArrayList");
    if (arrayListClass == NULL) {
        ThrowOutOfMemoryError(env);
        return;
    }
    jclass geoCoordClass =
        (**env).FindClass(env, "com/uber/h3core/util/GeoCoord");
    if (geoCoordClass == NULL) {
        ThrowOutOfMemoryError(env);
        return;
    }
    jmethodID arrayListConstructor =
        (**env).GetMethodID(env, arrayListClass, "<init>", "()V");
    if (arrayListConstructor == NULL) {
        ThrowOutOfMemoryError(env);
        return;
    }
    jmethodID arrayListAdd = (**env).GetMethodID(env, arrayListClass, "add",
                                                 "(Ljava/lang/Object;)Z");
    if (arrayListAdd == NULL) {
        ThrowOutOfMemoryError(env);
        return;
    }
    jmethodID geoCoordConstructor =
        (**env).GetMethodID(env, geoCoordClass, "<init>", "(DD)V");
    if (geoCoordConstructor == NULL) {
        ThrowOutOfMemoryError(env);
        return;
    }

    while (currentPolygon != NULL) {
        jobject resultLoops =
            (**env).NewObject(env, arrayListClass, arrayListConstructor);
        if (resultLoops == NULL) {
            return;
        }

        // Check if the polygon is empty.
        // Don't have to do this other times because a loop can be gauranteed
        // to have coordinates.
        if (resultLoops != NULL && currentPolygon->first != NULL) {
            LinkedGeoLoop *currentLoop = currentPolygon->first;
            while (currentLoop != NULL) {
                jobject resultLoop = (**env).NewObject(env, arrayListClass,
                                                       arrayListConstructor);
                if (resultLoop == NULL) {
                    return;
                }

                LinkedGeoCoord *coord = currentLoop->first;
                while (coord != NULL) {
                    jobject v = (**env).NewObject(
                        env, geoCoordClass, geoCoordConstructor,
                        coord->vertex.lat, coord->vertex.lon);
                    if (v == NULL) {
                        return;
                    }

                    (**env).CallBooleanMethod(env, resultLoop, arrayListAdd, v);
                    RETURN_ON_EXCEPTION(env);

                    coord = coord->next;
                }

                (**env).CallBooleanMethod(env, resultLoops, arrayListAdd,
                                          resultLoop);
                RETURN_ON_EXCEPTION(env);

                currentLoop = currentLoop->next;
            }

            (**env).CallBooleanMethod(env, results, arrayListAdd, resultLoops);
            RETURN_ON_EXCEPTION(env);
        }

        currentPolygon = currentPolygon->next;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3SetToLinkedGeo
 * Signature: ([JLjava/util/ArrayList;)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_h3SetToLinkedGeo(
    JNIEnv *env, jobject thiz, jlongArray h3, jobject results) {
    LinkedGeoPolygon polygon;

    jsize numH3 = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        h3SetToLinkedGeo(h3Elements, numH3, &polygon);

        // Parse the output now
        LinkedGeoPolygon *currentPolygon = &polygon;

        ConvertLinkedGeoPolygonToManaged(env, currentPolygon, results);

        destroyLinkedPolygon(&polygon);

        (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxH3ToChildrenSize
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_maxH3ToChildrenSize(
    JNIEnv *env, jobject thiz, jlong h3, jint childRes) {
    return maxH3ToChildrenSize(h3, childRes);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3ToChildren
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_h3ToChildren(
    JNIEnv *env, jobject thiz, jlong h3, jint childRes, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        h3ToChildren(h3, childRes, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    compact
 * Signature: ([J[J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_compact(
    JNIEnv *env, jobject thiz, jlongArray h3, jlongArray results) {
    jint ret = 0;
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

        if (resultsElements != NULL) {
            jsize numHexes = (**env).GetArrayLength(env, h3);
            ret = compact(h3Elements, resultsElements, numHexes);

            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        } else {
            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            ThrowOutOfMemoryError(env);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }

    return ret;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxUncompactSize
 * Signature: ([JI)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_maxUncompactSize(
    JNIEnv *env, jobject thiz, jlongArray h3, jint res) {
    jsize numHexes = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jint ret = maxUncompactSize(h3Elements, numHexes, res);

        (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);

        return ret;
    } else {
        ThrowOutOfMemoryError(env);
        return 0;
    }
}

/*
 * Class:     com_uber_h3_NativeMethods
 * Method:    uncompact
 * Signature: ([JI[J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_uncompact(
    JNIEnv *env, jobject thiz, jlongArray h3, jint res, jlongArray results) {
    jint ret = 0;
    jsize numHexes = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jsize maxHexes = (**env).GetArrayLength(env, results);
        jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

        if (resultsElements != NULL) {
            ret =
                uncompact(h3Elements, numHexes, resultsElements, maxHexes, res);

            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        } else {
            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            ThrowOutOfMemoryError(env);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }

    return ret;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    hexAreaKm2
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_hexAreaKm2(
    JNIEnv *env, jobject thiz, jint res) {
    return hexAreaKm2(res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    hexAreaM2
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_hexAreaM2(
    JNIEnv *env, jobject thiz, jint res) {
    return hexAreaM2(res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    edgeLengthKm
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_edgeLengthKm(
    JNIEnv *env, jobject thiz, jint res) {
    return edgeLengthKm(res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    edgeLengthM
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_edgeLengthM(
    JNIEnv *env, jobject thiz, jint res) {
    return edgeLengthM(res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    numHexagons
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_numHexagons(
    JNIEnv *env, jobject thiz, jint res) {
    return numHexagons(res);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3IndexesAreNeighbors
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_uber_h3core_NativeMethods_h3IndexesAreNeighbors(JNIEnv *env,
                                                         jobject thiz, jlong a,
                                                         jlong b) {
    return h3IndexesAreNeighbors(a, b);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getH3UnidirectionalEdge
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_getH3UnidirectionalEdge(JNIEnv *env,
                                                           jobject thiz,
                                                           jlong a, jlong b) {
    return getH3UnidirectionalEdge(a, b);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    h3UnidirectionalEdgeIsValid
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_uber_h3core_NativeMethods_h3UnidirectionalEdgeIsValid(JNIEnv *env,
                                                               jobject thiz,
                                                               jlong h3) {
    return h3UnidirectionalEdgeIsValid(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getOriginH3IndexFromUnidirectionalEdge
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_getOriginH3IndexFromUnidirectionalEdge(
    JNIEnv *env, jobject thiz, jlong h3) {
    return getOriginH3IndexFromUnidirectionalEdge(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getDestinationH3IndexFromUnidirectionalEdge
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_getDestinationH3IndexFromUnidirectionalEdge(
    JNIEnv *env, jobject thiz, jlong h3) {
    return getDestinationH3IndexFromUnidirectionalEdge(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getH3IndexesFromUnidirectionalEdge
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL
Java_com_uber_h3core_NativeMethods_getH3IndexesFromUnidirectionalEdge(
    JNIEnv *env, jobject thiz, jlong h3, jlongArray results) {
    jsize sz = (**env).GetArrayLength(env, results);
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            getH3IndexesFromUnidirectionalEdge(h3, resultsElements);
        }

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getH3UnidirectionalEdgesFromHexagon
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL
Java_com_uber_h3core_NativeMethods_getH3UnidirectionalEdgesFromHexagon(
    JNIEnv *env, jobject thiz, jlong h3, jlongArray results) {
    jsize sz = (**env).GetArrayLength(env, results);
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= MAX_HEX_EDGES) {
            getH3UnidirectionalEdgesFromHexagon(h3, resultsElements);
        }

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getH3UnidirectionalEdgeBoundary
 * Signature: (J[D)I
 */
JNIEXPORT jint JNICALL
Java_com_uber_h3core_NativeMethods_getH3UnidirectionalEdgeBoundary(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray verts) {
    GeoBoundary boundary;
    getH3UnidirectionalEdgeBoundary(h3, &boundary);

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *vertsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (vertsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        for (jsize i = 0; i < sz && i < boundary.numVerts * 2; i += 2) {
            vertsElements[i] = boundary.verts[i / 2].lat;
            vertsElements[i + 1] = boundary.verts[i / 2].lon;
        }

        (**env).ReleaseDoubleArrayElements(env, verts, vertsElements, 0);

        return boundary.numVerts;
    } else {
        ThrowOutOfMemoryError(env);
        return -1;
    }
}
