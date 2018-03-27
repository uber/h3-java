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
package com.uber.h3core.benchmarking;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.Vector2D;
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
 * Benchmarks creating the library and simple index operations (index, get coordinates, get boundary)
 */
public class H3CoreBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public H3Core benchmarkCreateH3Core() throws IOException {
        return H3Core.newInstance();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public long benchmarkGeoToHex() {
        return BenchmarkState.h3.geoToH3(37.775938728915946, -122.41795063018799, 5);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Vector2D benchmarkGetCenterCoordinates() {
        return BenchmarkState.h3.h3ToGeo("85283083fffffff");
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<Vector2D> benchmarkGetBoundary() {
        return BenchmarkState.h3.h3ToGeoBoundary("85283083fffffff");
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        static final H3Core h3;

        static {
            try {
                h3 = H3Core.newInstance();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(H3CoreBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
