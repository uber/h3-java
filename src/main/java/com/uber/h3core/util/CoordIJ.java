
/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 */
package com.uber.h3core.util;

import java.util.Objects;

/**
 * Immutable two-dimensional IJ grid coordinates.
 */
public class CoordIJ {
    public final int i;
    public final int j;

    public CoordIJ(int i, int j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordIJ ij = (CoordIJ) o;
        return ij.i == i && ij.j == j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }

    @Override
    public String toString() {
        return String.format("CoordIJ{i=%d, j=%d}", i, j);
    }
}
