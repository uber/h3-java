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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** */
class TestH3Exception {
  @Test
  void test() {
    Set<String> messages = new HashSet<>();
    int maxErrorCode = 16;
    for (int i = 0; i < maxErrorCode + 10; i++) {
      H3Exception e = new H3Exception(i);
      messages.add(e.getMessage());
      assertEquals(i, e.getCode());
    }
    // +1 for the unknown message
    assertEquals(maxErrorCode + 1, messages.size());
  }
}
