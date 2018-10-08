/*
 * Copyright 2018 Uber Technologies, Inc.
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
package com.uber.h3core.exceptions;

/**
 * The local IJ coordinates could not be determined for an index, or the
 * index could not be determined for IJ coordinates.
 *
 * <p>This can happen because the origin and index/IJ coordinates are too
 * far away from each other, or because pentagon distortion was encountered.
 */
public class LocalIjUndefinedException extends Exception {
    public LocalIjUndefinedException(String message) {
        super(message);
    }
}
