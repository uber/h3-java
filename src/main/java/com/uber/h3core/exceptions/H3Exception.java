/*
 * Copyright 2022 Uber Technologies, Inc.
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
 * An exception from the H3 core library.
 *
 * <p>The error code contained in an H3Exception comes from the H3 core library. The H3
 * documentation contains a <a
 * href="https://h3geo.org/docs/library/errors/#table-of-error-codes">table of error codes</a>.
 */
public class H3Exception extends RuntimeException {
  private int code;

  public H3Exception(int code) {
    super(codeToMessage(code));
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static String codeToMessage(int code) {
    switch (code) {
      case 0:
        return "Success";
      case 1:
        return "The operation failed but a more specific error is not available";
      case 2:
        return "Argument was outside of acceptable range";
      case 3:
        return "Latitude or longitude arguments were outside of acceptable range";
      case 4:
        return "Resolution argument was outside of acceptable range";
      case 5:
        return "Cell argument was not valid";
      case 6:
        return "Directed edge argument was not valid";
      case 7:
        return "Undirected edge argument was not valid";
      case 8:
        return "Vertex argument was not valid";
      case 9:
        return "Pentagon distortion was encountered";
      case 10:
        return "Duplicate input";
      case 11:
        return "Cell arguments were not neighbors";
      case 12:
        return "Cell arguments had incompatible resolutions";
      case 13:
        return "Memory allocation failed";
      case 14:
        return "Bounds of provided memory were insufficient";
      case 15:
        return "Mode or flags argument was not valid";
      case 16:
        return "Index argument was not valid";
      case 17:
        return "Base cell number was outside of acceptable range";
      case 18:
        return "Child indexing digits invalid";
      case 19:
        return "Child indexing digits refer to a deleted subsequence";
      default:
        return "Unknown error";
    }
  }
}
