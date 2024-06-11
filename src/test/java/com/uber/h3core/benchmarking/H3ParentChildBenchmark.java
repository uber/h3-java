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

/** Benchmark getting the parent, or children of some addresses. */
public class H3ParentChildBenchmark {
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public int benchmarkGetResolution() {
    return BenchmarkState.h3Core.getResolution(BenchmarkState.someHexagon);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkcellToParent() {
    return BenchmarkState.h3Core.cellToParent(BenchmarkState.someHexagon, 5);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkCellToChildrenRes10() {
    return BenchmarkState.h3Core.cellToChildren(BenchmarkState.someHexagon, 10);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkCellToChildrenRes11() {
    return BenchmarkState.h3Core.cellToChildren(BenchmarkState.someHexagon, 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  public List<Long> benchmarkCellToChildrenPentagon() {
    return BenchmarkState.h3Core.cellToChildren(BenchmarkState.somePentagon, 2);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    static long someHexagon = 0x89283082837ffffL;
    static long somePentagon = 0x8009fffffffffffL;

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
        new OptionsBuilder().include(H3ParentChildBenchmark.class.getSimpleName()).forks(1).build();

    new Runner(opt).run();
  }
}
