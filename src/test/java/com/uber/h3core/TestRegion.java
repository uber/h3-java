/*
 * Copyright 2019, 2022 Uber Technologies, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uber.h3core.exceptions.H3Exception;
import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for region (polyfill, h3SetToMultiPolygon) functions. */
class TestRegion extends BaseTestH3Core {
  @Test
  void polyfill() {
    List<Long> hexagons =
        h3.polygonToCells(
            ImmutableList.of(
                new LatLng(37.813318999983238, -122.4089866999972145),
                new LatLng(37.7866302000007224, -122.3805436999997056),
                new LatLng(37.7198061999978478, -122.3544736999993603),
                new LatLng(37.7076131999975672, -122.5123436999983966),
                new LatLng(37.7835871999971715, -122.5247187000021967),
                new LatLng(37.8151571999998453, -122.4798767000009008)),
            null,
            9);

    assertTrue(hexagons.size() > 1000);
  }

  @Test
  void polyfillAddresses() {
    List<String> hexagons =
        h3.polygonToCellAddresses(
            ImmutableList.<LatLng>of(
                new LatLng(37.813318999983238, -122.4089866999972145),
                new LatLng(37.7866302000007224, -122.3805436999997056),
                new LatLng(37.7198061999978478, -122.3544736999993603),
                new LatLng(37.7076131999975672, -122.5123436999983966),
                new LatLng(37.7835871999971715, -122.5247187000021967),
                new LatLng(37.8151571999998453, -122.4798767000009008)),
            null,
            9);

    assertTrue(hexagons.size() > 1000);
  }

  @Test
  void polyfillWithHole() {
    List<Long> hexagons =
        h3.polygonToCells(
            ImmutableList.<LatLng>of(
                new LatLng(37.813318999983238, -122.4089866999972145),
                new LatLng(37.7866302000007224, -122.3805436999997056),
                new LatLng(37.7198061999978478, -122.3544736999993603),
                new LatLng(37.7076131999975672, -122.5123436999983966),
                new LatLng(37.7835871999971715, -122.5247187000021967),
                new LatLng(37.8151571999998453, -122.4798767000009008)),
            ImmutableList.<List<LatLng>>of(
                ImmutableList.<LatLng>of(
                    new LatLng(37.7869802, -122.4471197),
                    new LatLng(37.7664102, -122.4590777),
                    new LatLng(37.7710682, -122.4137097))),
            9);

    assertTrue(hexagons.size() > 1000);
  }

  @Test
  void polyfillWithTwoHoles() {
    List<Long> hexagons =
        h3.polygonToCells(
            ImmutableList.<LatLng>of(
                new LatLng(37.813318999983238, -122.4089866999972145),
                new LatLng(37.7866302000007224, -122.3805436999997056),
                new LatLng(37.7198061999978478, -122.3544736999993603),
                new LatLng(37.7076131999975672, -122.5123436999983966),
                new LatLng(37.7835871999971715, -122.5247187000021967),
                new LatLng(37.8151571999998453, -122.4798767000009008)),
            ImmutableList.<List<LatLng>>of(
                ImmutableList.<LatLng>of(
                    new LatLng(37.7869802, -122.4471197),
                    new LatLng(37.7664102, -122.4590777),
                    new LatLng(37.7710682, -122.4137097)),
                ImmutableList.<LatLng>of(
                    new LatLng(37.747976, -122.490025),
                    new LatLng(37.731550, -122.503758),
                    new LatLng(37.725440, -122.452603))),
            9);

    assertTrue(hexagons.size() > 1000);
  }

  @Test
  void polyfillKnownHoles() {
    List<Long> inputHexagons = h3.gridDisk(0x85283083fffffffL, 2);
    inputHexagons.remove(0x8528308ffffffffL);
    inputHexagons.remove(0x85283097fffffffL);
    inputHexagons.remove(0x8528309bfffffffL);

    List<List<LatLng>> geo = h3.cellsToMultiPolygon(inputHexagons, true).get(0);

    List<LatLng> outline = geo.remove(0); // geo is now holes

    List<Long> outputHexagons = h3.polygonToCells(outline, geo, 5);

    assertEquals(ImmutableSet.copyOf(inputHexagons), ImmutableSet.copyOf(outputHexagons));
  }

  @Test
  void h3SetToMultiPolygonEmpty() {
    assertEquals(0, h3.cellsToMultiPolygon(new ArrayList<Long>(), false).size());
  }

  @Test
  void h3SetToMultiPolygonSingle() {
    long testIndex = 0x89283082837ffffL;

    List<LatLng> actualBounds = h3.cellToBoundary(testIndex);
    List<List<List<LatLng>>> multiBounds =
        h3.cellsToMultiPolygon(ImmutableList.of(testIndex), true);

    // This is tricky, because output in an order starting from any vertex
    // would also be correct, but that's difficult to assert and there's
    // value in being specific here

    assertEquals(1, multiBounds.size());
    assertEquals(1, multiBounds.get(0).size());
    assertEquals(actualBounds.size() + 1, multiBounds.get(0).get(0).size());

    int[] expectedIndices = {0, 1, 2, 3, 4, 5, 0};

    for (int i = 0; i < actualBounds.size(); i++) {
      assertEquals(
          actualBounds.get(expectedIndices[i]).lat, multiBounds.get(0).get(0).get(i).lat, EPSILON);
      assertEquals(
          actualBounds.get(expectedIndices[i]).lng, multiBounds.get(0).get(0).get(i).lng, EPSILON);
    }
  }

  @Test
  void h3SetToMultiPolygonSingleNonGeoJson() {
    long testIndex = 0x89283082837ffffL;

    List<LatLng> actualBounds = h3.cellToBoundary(testIndex);
    List<List<List<LatLng>>> multiBounds =
        h3.cellsToMultiPolygon(ImmutableList.of(testIndex), false);

    // This is tricky, because output in an order starting from any vertex
    // would also be correct, but that's difficult to assert and there's
    // value in being specific here

    assertEquals(1, multiBounds.size());
    assertEquals(1, multiBounds.get(0).size());
    assertEquals(actualBounds.size(), multiBounds.get(0).get(0).size());

    int[] expectedIndices = {0, 1, 2, 3, 4, 5};

    for (int i = 0; i < actualBounds.size(); i++) {
      assertEquals(
          actualBounds.get(expectedIndices[i]).lat, multiBounds.get(0).get(0).get(i).lat, EPSILON);
      assertEquals(
          actualBounds.get(expectedIndices[i]).lng, multiBounds.get(0).get(0).get(i).lng, EPSILON);
    }
  }

  @Test
  void h3SetToMultiPolygonContiguous2() {
    long testIndex = 0x89283082837ffffL;
    long testIndex2 = 0x89283082833ffffL;

    List<LatLng> actualBounds = h3.cellToBoundary(testIndex);
    List<LatLng> actualBounds2 = h3.cellToBoundary(testIndex2);

    // Note this is different than the h3core-js bindings, in that it uses GeoJSON (possible bug)
    List<List<List<LatLng>>> multiBounds =
        h3.cellsToMultiPolygon(ImmutableList.of(testIndex, testIndex2), false);

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
  void h3SetToMultiPolygonNonContiguous2() {
    long testIndex = 0x89283082837ffffL;
    long testIndex2 = 0x8928308280fffffL;

    List<List<List<LatLng>>> multiBounds =
        h3.cellsToMultiPolygon(ImmutableList.of(testIndex, testIndex2), false);

    assertEquals(2, multiBounds.size());
    assertEquals(1, multiBounds.get(0).size());
    assertEquals(6, multiBounds.get(0).get(0).size());
    assertEquals(1, multiBounds.get(1).size());
    assertEquals(6, multiBounds.get(1).get(0).size());
  }

  @Test
  void h3SetToMultiPolygonHole() {
    // Six hexagons in a ring around a hole
    List<List<List<LatLng>>> multiBounds =
        h3.cellAddressesToMultiPolygon(
            ImmutableList.of(
                "892830828c7ffff",
                "892830828d7ffff",
                "8928308289bffff",
                "89283082813ffff",
                "8928308288fffff",
                "89283082883ffff"),
            false);

    assertEquals(1, multiBounds.size());
    assertEquals(2, multiBounds.get(0).size());
    assertEquals(6 * 3, multiBounds.get(0).get(0).size());
    assertEquals(6, multiBounds.get(0).get(1).size());
  }

  @Test
  void h3SetToMultiPolygonLarge() {
    int numHexes = 20000;

    List<String> addresses = new ArrayList<>(numHexes);
    for (int i = 0; i < numHexes; i++) {
      addresses.add(h3.latLngToCellAddress(0, i * 0.01, 15));
    }

    List<List<List<LatLng>>> multiBounds = h3.cellAddressesToMultiPolygon(addresses, false);

    assertEquals(numHexes, multiBounds.size());
    for (int i = 0; i < multiBounds.size(); i++) {
      assertEquals(1, multiBounds.get(i).size());
      assertEquals(6, multiBounds.get(i).get(0).size());
    }
  }

  @Test
  void h3SetToMultiPolygonIssue753() {
    List<Long> cells =
        ImmutableList.of(
            617683643010646015L,
            617683648070287359L,
            617683642951663615L,
            617683648070287359L,
            617683648070549503L,
            617683643014840319L,
            617683643013791743L,
            617683642951663615L,
            617683642951663615L,
            617683648065044479L,
            617683648070549503L,
            617683643010383871L,
            617683643010646015L,
            617683643013791743L,
            617683643008024575L,
            617683643014840319L,
            617683643010383871L,
            617683642941177855L,
            617683642941177855L,
            617683642941439999L,
            617683642951663615L,
            617683642950615039L,
            617683642950877183L,
            617683648065044479L,
            617683648070549503L,
            617683648057180159L,
            617683648065044479L,
            617683648064520191L,
            617683643013791743L,
            617683643014316031L,
            617683643008548863L,
            617683643007238143L,
            617683643008024575L,
            617683643010383871L,
            617683642953498623L,
            617683642941177855L,
            617683643014316031L,
            617683642953760767L,
            617683642941439999L,
            617683642951139327L,
            617683642950615039L,
            617683642951663615L,
            617683648065044479L,
            617683642950877183L,
            617683648064258047L,
            617683648063471615L,
            617683648066093055L,
            617683648057180159L,
            617683648064520191L,
            617683648057180159L,
            617683648070549503L,
            617683648066093055L,
            617683643019296767L,
            617683643008548863L,
            617683643010383871L,
            617683643018510335L,
            617683643017723903L,
            617683642953760767L,
            617683642954285055L,
            617683642953236479L,
            617683642953498623L,
            617683642953498623L,
            617683642940129279L,
            617683642941177855L,
            617683642950877183L,
            617683644212576255L,
            617683644211003391L,
            617683644212314111L,
            617683648064258047L,
            617683648057180159L,
            617683648057442303L,
            617683648068190207L,
            617683648057180159L,
            617683648064520191L,
            617683648064782335L,
            617683648060850175L,
            617683648057442303L,
            617683648070811647L,
            617683648071335935L,
            617683648070287359L,
            617683648070549503L,
            617683648131366911L,
            617683648070811647L,
            617683648066093055L,
            617683648067141631L,
            617683642944585727L,
            617683642947469311L,
            617683642940915711L,
            617683642940129279L,
            617683642953498623L,
            617683642945372159L,
            617683642944323583L,
            617683642944585727L,
            617683648065568767L,
            617683648066093055L,
            617683648065830911L,
            617683648065830911L,
            617683648068190207L,
            617683648067665919L,
            617683648028344319L,
            617683648065830911L,
            617683648067665919L,
            617683648068976639L,
            617683648028606463L);
    assertThrows(H3Exception.class, () -> h3.cellsToMultiPolygon(cells, true));
  }
}
