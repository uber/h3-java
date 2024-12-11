/*
 * Copyright 2024 Uber Technologies, Inc.
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

/** Flags for polygonToCellsExperimental */
public enum PolygonToCellsFlags {
  /** Cell center is contained in the shape */
  containment_center(0),
  /** Cell is fully contained in the shape */
  containment_full(1),
  /** Cell overlaps the shape at any point */
  containment_overlapping(2),
  /** Cell bounding box overlaps shape */
  containment_overlapping_bbox(3);

  private final int value;

  PolygonToCellsFlags(int value) {
    this.value = value;
  }

  public int toInt() {
    return this.value;
  }
}
