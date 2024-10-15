/*
 * Copyright 2019 Uber Technologies, Inc.
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

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;

/** Base class for tests of the class {@link H3Core} */
public abstract class BaseTestH3Core {
  public static final double EPSILON = 1e-6;

  protected static H3Core h3;

  @BeforeAll
  public static void setup() throws IOException {
    h3 = H3Core.newInstance();
  }
}
