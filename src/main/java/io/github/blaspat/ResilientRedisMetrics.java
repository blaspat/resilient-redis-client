/*
 * Copyright 2024 Blasius Patrick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.blaspat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ResilientRedisMetrics {

    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, Counter> cacheHits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> cacheMisses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> fallbacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> latencies = new ConcurrentHashMap<>();

    public ResilientRedisMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordHit(String operation) {
        cacheHits.computeIfAbsent(operation, k ->
                Counter.builder("redis.cache.hit")
                        .tag("operation", operation)
                        .description("Redis cache hit count")
                        .register(registry))
                .increment();
    }

    public void recordMiss(String operation) {
        cacheMisses.computeIfAbsent(operation, k ->
                Counter.builder("redis.cache.miss")
                        .tag("operation", operation)
                        .description("Redis cache miss count")
                        .register(registry))
                .increment();
    }

    public void recordError(String operation, String errorType) {
        errors.computeIfAbsent(operation + "_" + errorType, k ->
                Counter.builder("redis.error")
                        .tag("operation", operation)
                        .tag("error", errorType)
                        .description("Redis error count")
                        .register(registry))
                .increment();
    }

    public void recordFallback(String operation) {
        fallbacks.computeIfAbsent(operation, k ->
                Counter.builder("redis.fallback")
                        .tag("operation", operation)
                        .description("Redis fallback activation count")
                        .register(registry))
                .increment();
    }

    public void recordLatency(String operation, long durationMs) {
        latencies.computeIfAbsent(operation, k ->
                Timer.builder("redis.latency")
                        .tag("operation", operation)
                        .description("Redis operation latency")
                        .register(registry))
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public <T> T record(String operation, Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        try {
            T result = supplier.get();
            recordHit(operation);
            return result;
        } catch (Exception e) {
            recordError(operation, e.getClass().getSimpleName());
            throw e;
        } finally {
            recordLatency(operation, System.currentTimeMillis() - start);
        }
    }

    public void record(String operation, Runnable runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
            recordHit(operation);
        } catch (Exception e) {
            recordError(operation, e.getClass().getSimpleName());
            throw e;
        } finally {
            recordLatency(operation, System.currentTimeMillis() - start);
        }
    }
}
