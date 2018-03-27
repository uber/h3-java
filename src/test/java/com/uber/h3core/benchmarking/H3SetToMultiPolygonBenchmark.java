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

import com.google.common.collect.ImmutableList;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Benchmarks <code>h3SetToMultiPolygon</code>.
 */
public class H3SetToMultiPolygonBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<List<List<Vector2D>>> benchmarkH3SetToMultiPolygon2() {
        return BenchmarkState.h3Core.h3SetToMultiPolygon(BenchmarkState.list2, false);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public List<List<List<Vector2D>>> benchmarkH3SetToMultiPolygon20() {
        return BenchmarkState.h3Core.h3SetToMultiPolygon(BenchmarkState.list20, true);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        static List<Long> list2 = ImmutableList.of(0x89283082837ffffL, 0x89283082833ffffL);
        static List<Long> list20;

        static H3Core h3Core;

        static {
            try {
                h3Core = H3Core.newInstance();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }

            list20 = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                list20.add(h3Core.geoToH3(i, 0, 10));
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(H3SetToMultiPolygonBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
