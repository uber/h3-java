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

static jclass java_util_ArrayList;
static jclass java_lang_OutOfMemoryError;
static jclass com_uber_h3core_exceptions_H3Exception;
static jclass com_uber_h3core_util_LatLng;

static jmethodID com_uber_h3core_exceptions_H3Exception_init;
static jmethodID com_uber_h3core_util_LatLng_init;
static jmethodID java_lang_OutOfMemoryError_init;
static jmethodID java_util_ArrayList_init;
static jmethodID java_util_ArrayList_add;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((**vm).GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass local_arrayListClass =
            (**env).FindClass(env, "java/util/ArrayList");
        java_util_ArrayList_init =
            (**env).GetMethodID(env, local_arrayListClass, "<init>", "()V");
        java_util_ArrayList_add = (**env).GetMethodID(
            env, local_arrayListClass, "add", "(Ljava/lang/Object;)Z");
        java_util_ArrayList =
            (jclass)(**env).NewGlobalRef(env, local_arrayListClass);

        jclass local_latLngClass =
            (**env).FindClass(env, "com/uber/h3core/util/LatLng");
        com_uber_h3core_util_LatLng_init =
            (**env).GetMethodID(env, local_latLngClass, "<init>", "(DD)V");
        com_uber_h3core_util_LatLng =
            (jclass)(**env).NewGlobalRef(env, local_latLngClass);

        jclass local_h3eClass =
            (**env).FindClass(env, "com/uber/h3core/exceptions/H3Exception");
        com_uber_h3core_exceptions_H3Exception_init =
            (**env).GetMethodID(env, local_h3eClass, "<init>", "(I)V");
        com_uber_h3core_exceptions_H3Exception =
            (jclass)(**env).NewGlobalRef(env, local_h3eClass);

        jclass local_oomeClass =
            (**env).FindClass(env, "java/lang/OutOfMemoryError");
        java_lang_OutOfMemoryError_init =
            (**env).GetMethodID(env, local_oomeClass, "<init>", "()V");
        java_lang_OutOfMemoryError =
            (jclass)(**env).NewGlobalRef(env, local_oomeClass);

        return JNI_VERSION_1_6;
    }
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((**vm).GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        // Something is wrong but nothing we can do about this :(
        return;
    } else {
        // delete global references so the GC can collect them
        if (com_uber_h3core_exceptions_H3Exception != NULL) {
            (**env).DeleteGlobalRef(env,
                                    com_uber_h3core_exceptions_H3Exception);
        }
        if (java_lang_OutOfMemoryError != NULL) {
            (**env).DeleteGlobalRef(env, java_lang_OutOfMemoryError);
        }
        if (java_util_ArrayList != NULL) {
            (**env).DeleteGlobalRef(env, java_util_ArrayList);
        }
        if (com_uber_h3core_util_LatLng != NULL) {
            (**env).DeleteGlobalRef(env, com_uber_h3core_util_LatLng);
        }
    }
}

/**
 * Triggers an H3Exception
 */
void ThrowH3Exception(JNIEnv *env, H3Error err) {
    jthrowable h3eInstance = (jthrowable)((**env).NewObject(
        env, com_uber_h3core_exceptions_H3Exception,
        com_uber_h3core_exceptions_H3Exception_init, err));

    if (h3eInstance != NULL) {
        (**env).Throw(env, h3eInstance);
        (**env).DeleteLocalRef(env, h3eInstance);
    }
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
    jthrowable oomeInstance = (jthrowable)((**env).NewObject(
        env, java_lang_OutOfMemoryError, java_lang_OutOfMemoryError_init));

    if (oomeInstance != NULL) {
        (**env).ExceptionClear(env);
        (**env).Throw(env, oomeInstance);
        (**env).DeleteLocalRef(env, oomeInstance);
    }
}

/**
 * Populates the given GeoPolygon
 *
 * Returns 0 on success.
 */
H3Error CreateGeoPolygon(JNIEnv *env, jdoubleArray verts, jintArray holeSizes,
                         jdoubleArray holeVerts, GeoPolygon *polygon) {
    // This is the number of doubles, so convert to number of verts
    polygon->geoloop.numVerts = (**env).GetArrayLength(env, verts) / 2;
    polygon->geoloop.verts = (**env).GetDoubleArrayElements(env, verts, 0);
    if (polygon->geoloop.verts != NULL) {
        polygon->numHoles = (**env).GetArrayLength(env, holeSizes);

        if (polygon->numHoles > 0) {
            polygon->holes = calloc(polygon->numHoles, sizeof(GeoPolygon));
            if (polygon->holes == NULL) {
                (**env).ReleaseDoubleArrayElements(
                    env, verts, polygon->geoloop.verts, JNI_ABORT);
                ThrowOutOfMemoryError(env);
                return E_MEMORY_ALLOC;
            }

            jint *holeSizesElements =
                (**env).GetIntArrayElements(env, holeSizes, 0);
            if (holeSizesElements == NULL) {
                (**env).ReleaseDoubleArrayElements(
                    env, verts, polygon->geoloop.verts, JNI_ABORT);
                free(polygon->holes);
                ThrowOutOfMemoryError(env);
                return E_MEMORY_ALLOC;
            }

            jdouble *holeVertsElements =
                (**env).GetDoubleArrayElements(env, holeVerts, 0);
            if (holeVertsElements == NULL) {
                (**env).ReleaseDoubleArrayElements(
                    env, verts, polygon->geoloop.verts, JNI_ABORT);
                free(polygon->holes);
                (**env).ReleaseIntArrayElements(env, holeSizes,
                                                holeSizesElements, JNI_ABORT);
                ThrowOutOfMemoryError(env);
                return E_MEMORY_ALLOC;
            }

            size_t offset = 0;
            for (int i = 0; i < polygon->numHoles; i++) {
                // This is the number of doubles, so convert to number of verts
                polygon->holes[i].numVerts = holeSizesElements[i] / 2;
                polygon->holes[i].verts = holeVertsElements + offset;
                offset += holeSizesElements[i];
            }
            (**env).ReleaseIntArrayElements(env, holeSizes, holeSizesElements,
                                            JNI_ABORT);
            // holeVertsElements is not released here because it is still being
            // pointed to by polygon->holes[*].verts. It will be released in
            // DestroyGeoPolygon.
        }
        return E_SUCCESS;
    } else {
        ThrowOutOfMemoryError(env);
        return E_MEMORY_ALLOC;
    }
}

void DestroyGeoPolygon(JNIEnv *env, jdoubleArray verts,
                       jintArray holeSizesElements, jdoubleArray holeVerts,
                       GeoPolygon *polygon) {
    (**env).ReleaseDoubleArrayElements(env, verts, polygon->geoloop.verts,
                                       JNI_ABORT);

    if (polygon->numHoles > 0) {
        // The hole verts were pinned only once, so we don't need to iterate.
        (**env).ReleaseDoubleArrayElements(env, holeVerts,
                                           polygon->holes[0].verts, JNI_ABORT);

        free(polygon->holes);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    constructCell
 * Signature: (II[I)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_constructCell(
    JNIEnv *env, jobject thiz, jint res, jint baseCell, jintArray digits) {
    H3Index result = 0;
    jint *digitsElements = (**env).GetIntArrayElements(env, digits, 0);

    if (digitsElements != NULL) {
        // if sz is too small, bad things will happen
        // note: We assume int can at least contain `jint` on the current
        // platform. This may not be true if sizeof(int) < 32, but we don't
        // support any platforms where this would be the case.
        H3Error err = constructCell(res, baseCell, digitsElements, &result);

        (**env).ReleaseIntArrayElements(env, digits, digitsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
    return result;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    isValidCell
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_isValidCell(
    JNIEnv *env, jobject thiz, jlong h3) {
    return isValidCell(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    isValidIndex
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_isValidIndex(
    JNIEnv *env, jobject thiz, jlong h3) {
    return isValidIndex(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getBaseCellNumber
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_getBaseCellNumber(
    JNIEnv *env, jobject thiz, jlong h3) {
    return getBaseCellNumber(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    isPentagon
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_isPentagon(
    JNIEnv *env, jobject thiz, jlong h3) {
    return isPentagon(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    latLngToCell
 * Signature: (DDI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_latLngToCell(
    JNIEnv *env, jobject thiz, jdouble lat, jdouble lng, jint res) {
    LatLng geo = {lat, lng};
    H3Index out;
    H3Error err = latLngToCell(&geo, res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToLatLng
 * Signature: (J[D)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_cellToLatLng(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray verts) {
    LatLng coord;
    H3Error err = cellToLatLng(h3, &coord);
    if (err) {
        ThrowH3Exception(env, err);
        return;
    }

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *coordsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (coordsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            coordsElements[0] = coord.lat;
            coordsElements[1] = coord.lng;
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
 * Method:    cellToBoundary
 * Signature: (J[D)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_cellToBoundary(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray verts) {
    CellBoundary boundary;
    H3Error err = cellToBoundary(h3, &boundary);
    if (err) {
        ThrowH3Exception(env, err);
        return -1;
    }

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *vertsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (vertsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        for (jsize i = 0; i < sz && i < boundary.numVerts * 2; i += 2) {
            vertsElements[i] = boundary.verts[i / 2].lat;
            vertsElements[i + 1] = boundary.verts[i / 2].lng;
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
 * Method:    maxGridDiskSize
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_maxGridDiskSize(
    JNIEnv *env, jobject thiz, jint k) {
    jlong sz;
    H3Error err = maxGridDiskSize(k, &sz);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return sz;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridDisk
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridDisk(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = gridDisk(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridDiskDistances
 * Signature: (JI[J[I)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridDiskDistances(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results,
    jintArray distances) {
    H3Error err = E_SUCCESS;
    bool isOom = false;
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);
    if (resultsElements != NULL) {
        jint *distancesElements =
            (**env).GetIntArrayElements(env, distances, 0);
        if (distancesElements != NULL) {
            // if sz is too small, bad things will happen
            err = gridDiskDistances(h3, k, resultsElements, distancesElements);

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
    } else if (err) {
        ThrowH3Exception(env, err);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridDiskUnsafe
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridDiskUnsafe(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = gridDiskUnsafe(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridRing
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridRing(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = gridRing(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridRingUnsafe
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridRingUnsafe(
    JNIEnv *env, jobject thiz, jlong h3, jint k, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = gridRingUnsafe(h3, k, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridDistance
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_gridDistance(
    JNIEnv *env, jobject thiz, jlong a, jlong b) {
    jlong distance;
    H3Error err = gridDistance(a, b, &distance);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return distance;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToLocalIj
 * Signature: (JJ[I)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_cellToLocalIj(
    JNIEnv *env, jobject thiz, jlong origin, jlong h3, jintArray coords) {
    CoordIJ ij = {0};
    H3Error err = cellToLocalIj(origin, h3, 0, &ij);
    if (err) {
        ThrowH3Exception(env, err);
        return;
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
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    localIjToCell
 * Signature: (JII)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_localIjToCell(
    JNIEnv *env, jobject thiz, jlong origin, jint i, jint j) {
    CoordIJ ij = {.i = i, .j = j};
    H3Index index;
    H3Error err = localIjToCell(origin, &ij, 0, &index);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return index;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridPathCellsSize
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_gridPathCellsSize(
    JNIEnv *env, jobject thiz, jlong start, jlong end) {
    jlong sz;
    H3Error err = gridPathCellsSize(start, end, &sz);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return sz;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    gridPathCells
 * Signature: (JJ[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_gridPathCells(
    JNIEnv *env, jobject thiz, jlong start, jlong end, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = gridPathCells(start, end, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxPolygonToCellsSize
 * Signature: ([D[I[DII)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_maxPolygonToCellsSize(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res, jint flags) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return -1;
    }

    jlong numHexagons;
    H3Error err = maxPolygonToCellsSize(&polygon, res, flags, &numHexagons);

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);

    if (err) {
        ThrowH3Exception(env, err);
    }
    return numHexagons;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    maxPolygonToCellsSizeExperimental
 * Signature: ([D[I[DII)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_maxPolygonToCellsSizeExperimental(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res, jint flags) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return -1;
    }

    jlong numHexagons;
    H3Error err =
        maxPolygonToCellsSizeExperimental(&polygon, res, flags, &numHexagons);

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);

    if (err) {
        ThrowH3Exception(env, err);
    }
    return numHexagons;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getRes0Cells
 * Signature: ([J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_getRes0Cells(
    JNIEnv *env, jobject thiz, jlongArray results) {
    jsize size = (**env).GetArrayLength(env, results);
    if (size < res0CellCount()) {
        ThrowOutOfMemoryError(env);
        return;
    }

    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        H3Error err = getRes0Cells(resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getPentagons
 * Signature: (I[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_getPentagons(
    JNIEnv *env, jobject thiz, jint res, jlongArray results) {
    jsize size = (**env).GetArrayLength(env, results);
    if (size < pentagonCount()) {
        ThrowOutOfMemoryError(env);
        return;
    }

    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        H3Error err = getPentagons(res, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    polygonToCells
 * Signature: ([D[I[DII[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_polygonToCells(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res, jint flags, jlongArray results) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return;
    }

    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    H3Error err = E_SUCCESS;
    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        err = polygonToCells(&polygon, res, flags, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);

    if (err) {
        ThrowH3Exception(env, err);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    polygonToCellsExperimental
 * Signature: ([D[I[DII[J)V
 */
JNIEXPORT void JNICALL
Java_com_uber_h3core_NativeMethods_polygonToCellsExperimental(
    JNIEnv *env, jobject thiz, jdoubleArray verts, jintArray holeSizes,
    jdoubleArray holeVerts, jint res, jint flags, jlongArray results) {
    GeoPolygon polygon;
    if (CreateGeoPolygon(env, verts, holeSizes, holeVerts, &polygon)) {
        return;
    }

    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);
    jsize resultsSize = (**env).GetArrayLength(env, results);

    H3Error err = E_SUCCESS;
    if (resultsElements != NULL) {
        err = polygonToCellsExperimental(&polygon, res, flags, resultsSize,
                                         resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }

    DestroyGeoPolygon(env, verts, holeSizes, holeVerts, &polygon);

    if (err) {
        ThrowH3Exception(env, err);
    }
}

/**
 * Converts the given polygon to managed objects
 * (ArrayList<ArrayList<ArrayList<LatLng>>>)
 *
 * May return early if allocation or finding required classes or methods fails.
 */
void ConvertLinkedGeoPolygonToManaged(JNIEnv *env,
                                      LinkedGeoPolygon *currentPolygon,
                                      jobject results) {
    while (currentPolygon != NULL) {
        jobject resultLoops = (**env).NewObject(env, java_util_ArrayList,
                                                java_util_ArrayList_init);
        if (resultLoops == NULL) {
            return;
        }

        // Check if the polygon is empty.
        // Don't have to do this other times because a loop can be gauranteed
        // to have coordinates.
        if (resultLoops != NULL && currentPolygon->first != NULL) {
            LinkedGeoLoop *currentLoop = currentPolygon->first;
            while (currentLoop != NULL) {
                jobject resultLoop = (**env).NewObject(
                    env, java_util_ArrayList, java_util_ArrayList_init);
                if (resultLoop == NULL) {
                    return;
                }

                LinkedLatLng *coord = currentLoop->first;
                while (coord != NULL) {
                    jobject v =
                        (**env).NewObject(env, com_uber_h3core_util_LatLng,
                                          com_uber_h3core_util_LatLng_init,
                                          coord->vertex.lat, coord->vertex.lng);
                    if (v == NULL) {
                        return;
                    }

                    (**env).CallBooleanMethod(env, resultLoop,
                                              java_util_ArrayList_add, v);
                    RETURN_ON_EXCEPTION(env);

                    coord = coord->next;
                }

                (**env).CallBooleanMethod(env, resultLoops,
                                          java_util_ArrayList_add, resultLoop);
                RETURN_ON_EXCEPTION(env);

                currentLoop = currentLoop->next;
            }

            (**env).CallBooleanMethod(env, results, java_util_ArrayList_add,
                                      resultLoops);
            RETURN_ON_EXCEPTION(env);
        }

        currentPolygon = currentPolygon->next;
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellsToLinkedMultiPolygon
 * Signature: ([JLjava/util/ArrayList;)V
 */
JNIEXPORT void JNICALL
Java_com_uber_h3core_NativeMethods_cellsToLinkedMultiPolygon(JNIEnv *env,
                                                             jobject thiz,
                                                             jlongArray h3,
                                                             jobject results) {
    LinkedGeoPolygon polygon;

    jsize numH3 = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        H3Error err = cellsToLinkedMultiPolygon(h3Elements, numH3, &polygon);

        if (err) {
            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            ThrowH3Exception(env, err);
        } else {
            // Parse the output now
            LinkedGeoPolygon *currentPolygon = &polygon;

            ConvertLinkedGeoPolygonToManaged(env, currentPolygon, results);

            destroyLinkedMultiPolygon(&polygon);

            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToChildrenSize
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_cellToChildrenSize(
    JNIEnv *env, jobject thiz, jlong h3, jint childRes) {
    jlong sz;
    H3Error err = cellToChildrenSize(h3, childRes, &sz);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return sz;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToChildren
 * Signature: (JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_cellToChildren(
    JNIEnv *env, jobject thiz, jlong h3, jint childRes, jlongArray results) {
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, bad things will happen
        H3Error err = cellToChildren(h3, childRes, resultsElements);

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToCenterChild
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_cellToCenterChild(
    JNIEnv *env, jobject thiz, jlong h3, jint childRes) {
    H3Index child;
    H3Error err = cellToCenterChild(h3, childRes, &child);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return child;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    compactCells
 * Signature: ([J[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_compactCells(
    JNIEnv *env, jobject thiz, jlongArray h3, jlongArray results) {
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

        if (resultsElements != NULL) {
            jsize numHexes = (**env).GetArrayLength(env, h3);
            H3Error err = compactCells(h3Elements, resultsElements, numHexes);

            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);

            if (err) {
                ThrowH3Exception(env, err);
            }
        } else {
            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            ThrowOutOfMemoryError(env);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    uncompactCellsSize
 * Signature: ([JI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_uncompactCellsSize(
    JNIEnv *env, jobject thiz, jlongArray h3, jint res) {
    jsize numHexes = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jlong sz;
        H3Error err = uncompactCellsSize(h3Elements, numHexes, res, &sz);

        (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);

        if (err) {
            ThrowH3Exception(env, err);
        }
        return sz;
    } else {
        ThrowOutOfMemoryError(env);
        return 0;
    }
}

/*
 * Class:     com_uber_h3_NativeMethods
 * Method:    uncompactCells
 * Signature: ([JI[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_uncompactCells(
    JNIEnv *env, jobject thiz, jlongArray h3, jint res, jlongArray results) {
    jsize numHexes = (**env).GetArrayLength(env, h3);
    jlong *h3Elements = (**env).GetLongArrayElements(env, h3, 0);

    if (h3Elements != NULL) {
        jsize maxHexes = (**env).GetArrayLength(env, results);
        jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

        if (resultsElements != NULL) {
            H3Error err = uncompactCells(h3Elements, numHexes, resultsElements,
                                         maxHexes, res);

            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);

            if (err) {
                ThrowH3Exception(env, err);
            }
        } else {
            (**env).ReleaseLongArrayElements(env, h3, h3Elements, 0);
            ThrowOutOfMemoryError(env);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellAreaRads2
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_cellAreaRads2(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = cellAreaRads2(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellAreaKm2
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_cellAreaKm2(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = cellAreaKm2(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellAreaM2
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_cellAreaM2(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = cellAreaM2(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    greatCircleDistanceRads
 * Signature: (DDDD)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_greatCircleDistanceRads(
    JNIEnv *env, jobject thiz, jdouble lat1, jdouble lng1, jdouble lat2,
    jdouble lng2) {
    LatLng c1 = {.lat = lat1, .lng = lng1};
    LatLng c2 = {.lat = lat2, .lng = lng2};
    return greatCircleDistanceRads(&c1, &c2);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    greatCircleDistanceKm
 * Signature: (DDDD)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_greatCircleDistanceKm(
    JNIEnv *env, jobject thiz, jdouble lat1, jdouble lng1, jdouble lat2,
    jdouble lng2) {
    LatLng c1 = {.lat = lat1, .lng = lng1};
    LatLng c2 = {.lat = lat2, .lng = lng2};
    return greatCircleDistanceKm(&c1, &c2);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    greatCircleDistanceM
 * Signature: (DDDD)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_greatCircleDistanceM(
    JNIEnv *env, jobject thiz, jdouble lat1, jdouble lng1, jdouble lat2,
    jdouble lng2) {
    LatLng c1 = {.lat = lat1, .lng = lng1};
    LatLng c2 = {.lat = lat2, .lng = lng2};
    return greatCircleDistanceM(&c1, &c2);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    edgeLengthRads
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_edgeLengthRads(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = edgeLengthRads(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    edgeLengthKm
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_edgeLengthKm(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = edgeLengthKm(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    edgeLengthM
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_uber_h3core_NativeMethods_edgeLengthM(
    JNIEnv *env, jobject thiz, jlong h3) {
    jdouble out;
    H3Error err = edgeLengthM(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getHexagonAreaAvgKm2
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_getHexagonAreaAvgKm2(JNIEnv *env,
                                                        jobject thiz,
                                                        jint res) {
    jdouble out;
    H3Error err = getHexagonAreaAvgKm2(res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getHexagonAreaAvgM2
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_getHexagonAreaAvgM2(JNIEnv *env,
                                                       jobject thiz, jint res) {
    jdouble out;
    H3Error err = getHexagonAreaAvgM2(res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getHexagonEdgeLengthAvgKm
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_getHexagonEdgeLengthAvgKm(JNIEnv *env,
                                                             jobject thiz,
                                                             jint res) {
    jdouble out;
    H3Error err = getHexagonEdgeLengthAvgKm(res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getHexagonEdgeLengthAvgM
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL
Java_com_uber_h3core_NativeMethods_getHexagonEdgeLengthAvgM(JNIEnv *env,
                                                            jobject thiz,
                                                            jint res) {
    jdouble out;
    H3Error err = getHexagonEdgeLengthAvgM(res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getNumCells
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_getNumCells(
    JNIEnv *env, jobject thiz, jint res) {
    jlong out;
    H3Error err = getNumCells(res, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    areNeighborCells
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_areNeighborCells(
    JNIEnv *env, jobject thiz, jlong a, jlong b) {
    int out;
    H3Error err = areNeighborCells(a, b, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellsToDirectedEdge
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_cellsToDirectedEdge(
    JNIEnv *env, jobject thiz, jlong a, jlong b) {
    H3Index out;
    H3Error err = cellsToDirectedEdge(a, b, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    isValidDirectedEdge
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_uber_h3core_NativeMethods_isValidDirectedEdge(JNIEnv *env,
                                                       jobject thiz, jlong h3) {
    return isValidDirectedEdge(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getDirectedEdgeOrigin
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_getDirectedEdgeOrigin(JNIEnv *env,
                                                         jobject thiz,
                                                         jlong h3) {
    H3Index out;
    H3Error err = getDirectedEdgeOrigin(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getDirectedEdgeDestination
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_uber_h3core_NativeMethods_getDirectedEdgeDestination(JNIEnv *env,
                                                              jobject thiz,
                                                              jlong h3) {
    H3Index out;
    H3Error err = getDirectedEdgeDestination(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    directedEdgeToCells
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_directedEdgeToCells(
    JNIEnv *env, jobject thiz, jlong h3, jlongArray results) {
    jsize sz = (**env).GetArrayLength(env, results);
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            H3Error err = directedEdgeToCells(h3, resultsElements);
            if (err) {
                ThrowH3Exception(env, err);
            }
        } else {
            ThrowOutOfMemoryError(env);
        }

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    originToDirectedEdges
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_originToDirectedEdges(
    JNIEnv *env, jobject thiz, jlong h3, jlongArray results) {
    jsize sz = (**env).GetArrayLength(env, results);
    jlong *resultsElements = (**env).GetLongArrayElements(env, results, 0);

    if (resultsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= MAX_HEX_EDGES) {
            H3Error err = originToDirectedEdges(h3, resultsElements);
            if (err) {
                ThrowH3Exception(env, err);
            }
        } else {
            ThrowOutOfMemoryError(env);
        }

        (**env).ReleaseLongArrayElements(env, results, resultsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    directedEdgeToBoundary
 * Signature: (J[D)I
 */
JNIEXPORT jint JNICALL
Java_com_uber_h3core_NativeMethods_directedEdgeToBoundary(JNIEnv *env,
                                                          jobject thiz,
                                                          jlong h3,
                                                          jdoubleArray verts) {
    CellBoundary boundary;
    H3Error err = directedEdgeToBoundary(h3, &boundary);
    if (err) {
        ThrowH3Exception(env, err);
        return -1;
    }

    jsize sz = (**env).GetArrayLength(env, verts);
    jdouble *vertsElements = (**env).GetDoubleArrayElements(env, verts, 0);

    if (vertsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        for (jsize i = 0; i < sz && i < boundary.numVerts * 2; i += 2) {
            vertsElements[i] = boundary.verts[i / 2].lat;
            vertsElements[i + 1] = boundary.verts[i / 2].lng;
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
 * Method:    maxFaceCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_uber_h3core_NativeMethods_maxFaceCount(
    JNIEnv *env, jobject thiz, jlong h3) {
    int out;
    H3Error err = maxFaceCount(h3, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    getIcosahedronFaces
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_getIcosahedronFaces(
    JNIEnv *env, jobject thiz, jlong h3, jintArray faces) {
    // TODO: Unused; use maxFaceCount here
    // jsize sz = (**env).GetArrayLength(env, faces);
    jint *facesElements = (**env).GetIntArrayElements(env, faces, 0);

    if (facesElements != NULL) {
        H3Error err = getIcosahedronFaces(h3, facesElements);

        (**env).ReleaseIntArrayElements(env, faces, facesElements, 0);

        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToVertex
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_cellToVertex(
    JNIEnv *env, jobject thiz, jlong h3, jint vertexNum) {
    H3Index out;
    H3Error err = cellToVertex(h3, vertexNum, &out);
    if (err) {
        ThrowH3Exception(env, err);
    }
    return out;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToVertexes
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_cellToVertexes(
    JNIEnv *env, jobject thiz, jlong h3, jlongArray vertexes) {
    jsize sz = (**env).GetArrayLength(env, vertexes);
    jlong *vertexesElements = (**env).GetLongArrayElements(env, vertexes, 0);

    if (vertexesElements != NULL && sz >= 6) {
        H3Error err = cellToVertexes(h3, vertexesElements);

        (**env).ReleaseLongArrayElements(env, vertexes, vertexesElements, 0);

        if (err) {
            ThrowH3Exception(env, err);
        }
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    vertexToLatLng
 * Signature: (J[D)V
 */
JNIEXPORT void JNICALL Java_com_uber_h3core_NativeMethods_vertexToLatLng(
    JNIEnv *env, jobject thiz, jlong h3, jdoubleArray latLng) {
    LatLng coord;
    H3Error err = vertexToLatLng(h3, &coord);
    if (err) {
        ThrowH3Exception(env, err);
        return;
    }

    jsize sz = (**env).GetArrayLength(env, latLng);
    jdouble *coordsElements = (**env).GetDoubleArrayElements(env, latLng, 0);

    if (coordsElements != NULL) {
        // if sz is too small, we will fail to write all the elements
        if (sz >= 2) {
            coordsElements[0] = coord.lat;
            coordsElements[1] = coord.lng;
        }

        (**env).ReleaseDoubleArrayElements(env, latLng, coordsElements, 0);
    } else {
        ThrowOutOfMemoryError(env);
    }
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    isValidVertex
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_uber_h3core_NativeMethods_isValidVertex(
    JNIEnv *env, jobject thiz, jlong h3) {
    return isValidVertex(h3);
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    cellToChildPos
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_cellToChildPos(
    JNIEnv *env, jobject thiz, jlong child, jint parentRes) {
    jlong pos;
    H3Error err = cellToChildPos(child, parentRes, &pos);
    if (err) {
        ThrowH3Exception(env, err);
        return 0;
    }
    return pos;
}

/*
 * Class:     com_uber_h3core_NativeMethods
 * Method:    childPosToCell
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_h3core_NativeMethods_childPosToCell(
    JNIEnv *env, jobject thiz, jlong childPos, jlong parent, jint childRes) {
    H3Index out;
    H3Error err = childPosToCell(childPos, parent, childRes, &out);
    if (err) {
        ThrowH3Exception(env, err);
        return 0;
    }
    return out;
}
