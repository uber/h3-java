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
package com.uber.h3core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import javax.annotation.concurrent.NotThreadSafe;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * H3CoreLoader is mostly tested by {@link TestH3CoreFactory}. This also tests OS detection under
 * different locales.
 */
@NotThreadSafe
class TestH3CoreLoaderLocale {
  private static Locale systemLocale;

  @BeforeAll
  static void setup() {
    systemLocale = Locale.getDefault();
    // Turkish is used as the test locale as the Turkish lower case I
    // is dotless and this frequently triggers locale-dependent bugs.
    Locale.setDefault(Locale.forLanguageTag("tr-TR"));
  }

  @AfterAll
  static void tearDown() {
    Locale.setDefault(systemLocale);
  }

  @Test
  void detectOs() {
    assertEquals(
        H3CoreLoader.OperatingSystem.ANDROID, H3CoreLoader.detectOs("ANDROID", "anything"));
    assertEquals(H3CoreLoader.OperatingSystem.WINDOWS, H3CoreLoader.detectOs("vendor", "WINDOWS"));
    assertEquals(H3CoreLoader.OperatingSystem.LINUX, H3CoreLoader.detectOs("vendor", "LINUX"));
    assertEquals(H3CoreLoader.OperatingSystem.FREEBSD, H3CoreLoader.detectOs("vendor", "FREEBSD"));

    assertEquals(
        H3CoreLoader.OperatingSystem.LINUX, H3CoreLoader.detectOs("vendor", "anything else"));
  }

  @Test
  void detectArch() {
    assertEquals("I386", H3CoreLoader.detectArch("I386"));
  }

  @Test
  void osDir() {
    assertEquals(
        "darwin",
        H3CoreLoader.OperatingSystem.DARWIN.getDirName(),
        "Turkish lower case I (Darwin)");
    assertEquals(
        "linux", H3CoreLoader.OperatingSystem.LINUX.getDirName(), "Turkish lower case I (Linux)");
    assertEquals(
        "windows",
        H3CoreLoader.OperatingSystem.WINDOWS.getDirName(),
        "Turkish lower case I (Windows)");
    assertEquals(
        "android",
        H3CoreLoader.OperatingSystem.ANDROID.getDirName(),
        "Turkish lower case I (Android)");
  }
}
