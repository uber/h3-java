/*
 * Copyright 2017-2018, 2022 Uber Technologies, Inc.
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
package com.uber.h3core.util;

import java.util.Objects;

/** Immutable two-dimensional spherical coordinates, in degrees. */
public class LatLng {
  /** Latitude (north-south) coordinate in degrees */
  public final double lat;

  /** Longitude (east-west) coordinate in degrees */
  public final double lng;

  /**
   * Construct with latitude and longitude
   *
   * @param lat Latitude coordinate
   * @param lng Longitude coordinate
   */
  public LatLng(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LatLng coord = (LatLng) o;
    return Double.compare(coord.lat, lat) == 0 && Double.compare(coord.lng, lng) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lat, lng);
  }

  @Override
  public String toString() {
    return String.format("LatLng{lat=%f, lng=%f}", lat, lng);
  }
}
