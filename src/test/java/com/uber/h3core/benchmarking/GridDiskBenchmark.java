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

import com.uber.h3core.H3Core;
import com.uber.h3core.exceptions.PentagonEncounteredException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.List;

/**
 * Benchmarks <code>kRing</code> and related functions.
 */
public class GridDiskBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<Long> benchmarkHexRingsCore() {
        return BenchmarkState.h3Core.gridDisk(0x8928308280fffffL, BenchmarkState.k);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<List<Long>> benchmarkHexRangeCore() throws PentagonEncounteredException {
        return BenchmarkState.h3Core.gridDiskDistances(0x8928308280fffffL, BenchmarkState.k);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<Long> benchmarkHexRingsCoreNearPentagon() {
        return BenchmarkState.h3Core.gridDisk(0x821d5ffffffffffL, BenchmarkState.k);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<List<Long>> benchmarkHexRangeCoreNearPentagon() throws PentagonEncounteredException {
        return BenchmarkState.h3Core.gridDiskDistances(0x821d5ffffffffffL, BenchmarkState.k);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        static int k = 10;

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
        Options opt = new OptionsBuilder()
            .include(GridDiskBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
