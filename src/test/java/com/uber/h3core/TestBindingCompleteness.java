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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

/** Tests all expected functions are exposed. */
class TestBindingCompleteness {
  /** Functions to ignore from the bindings. */
  private static final Set<String> WHITELIST =
      ImmutableSet.of(
          // These are provided by the Java library (java.lang.Math)
          "degsToRads", "radsToDegs");

  @Test
  @DisabledInNativeImage
  void test() throws IOException {
    Set<String> exposed = new HashSet<>();
    for (Method m : H3Core.class.getMethods()) {
      exposed.add(m.getName());
    }

    int checkedFunctions = 0;
    try (Scanner in = new Scanner(new File("./target/binding-functions"), "UTF-8")) {
      while (in.hasNext()) {
        String function = in.next();

        if (WHITELIST.contains(function)) {
          continue;
        }

        assertTrue(exposed.contains(function), function + " is exposed in binding");
        checkedFunctions++;
      }
    }
    assertTrue(checkedFunctions > 10, "Checked that the API exists");
  }
}
