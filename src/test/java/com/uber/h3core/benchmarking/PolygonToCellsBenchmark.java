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
package com.uber.h3core.benchmarking;

import com.google.common.collect.ImmutableList;
import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** Benchmarks <code>polyfill</code>. */
public class PolygonToCellsBenchmark {
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkPolyfill() {
    return BenchmarkState.h3Core.polygonToCells(
        ImmutableList.of(
            new LatLng(37.813318999983238, -122.4089866999972145),
            new LatLng(37.7866302000007224, -122.3805436999997056),
            new LatLng(37.7198061999978478, -122.3544736999993603),
            new LatLng(37.7076131999975672, -122.5123436999983966),
            new LatLng(37.7835871999971715, -122.5247187000021967),
            new LatLng(37.8151571999998453, -122.4798767000009008)),
        null,
        9);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkPolyfillWithHole() {
    return BenchmarkState.h3Core.polygonToCells(
        ImmutableList.of(
            new LatLng(37.813318999983238, -122.4089866999972145),
            new LatLng(37.7866302000007224, -122.3805436999997056),
            new LatLng(37.7198061999978478, -122.3544736999993603),
            new LatLng(37.7076131999975672, -122.5123436999983966),
            new LatLng(37.7835871999971715, -122.5247187000021967),
            new LatLng(37.8151571999998453, -122.4798767000009008)),
        ImmutableList.of(
            ImmutableList.of(
                new LatLng(37.7869802, -122.4471197),
                new LatLng(37.7664102, -122.4590777),
                new LatLng(37.7710682, -122.4137097))),
        9);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkPolyfillWithTwoHoles() {
    return BenchmarkState.h3Core.polygonToCells(
        ImmutableList.of(
            new LatLng(37.813318999983238, -122.4089866999972145),
            new LatLng(37.7866302000007224, -122.3805436999997056),
            new LatLng(37.7198061999978478, -122.3544736999993603),
            new LatLng(37.7076131999975672, -122.5123436999983966),
            new LatLng(37.7835871999971715, -122.5247187000021967),
            new LatLng(37.8151571999998453, -122.4798767000009008)),
        ImmutableList.of(
            ImmutableList.of(
                new LatLng(37.7869802, -122.4471197),
                new LatLng(37.7664102, -122.4590777),
                new LatLng(37.7710682, -122.4137097)),
            ImmutableList.of(
                new LatLng(37.747976, -122.490025),
                new LatLng(37.731550, -122.503758),
                new LatLng(37.725440, -122.452603))),
        9);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    static H3Core h3Core;

    static {
      try {
        h3Core = H3Core.newInstance();
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(PolygonToCellsBenchmark.class.getSimpleName())
            .forks(1)
            .build();

    new Runner(opt).run();
  }
}
