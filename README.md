Resilient Java Redis Client
===========================

## Overview
Resilient Java Redis Client is a robust, high-performance library for interacting with Redis in Java applications. Designed with fault tolerance and resilience in mind, it enhances the standard Redis Lettuce client capabilities with advanced features to handle common Redis-related issues seamlessly, ensuring that your application remains stable even when Redis encounters problems.

## Key Features
- **Error Handling** — Automatically manages Redis server errors, serialization errors, and connection issues.
- **`@Cacheable` Compatibility** — Ensures that `@Cacheable` annotations continue to work without breaking functionality, even if Redis is temporarily unavailable or misbehaving.
- **Circuit Breaker** — Stops hammering Redis when it's clearly down, preventing cascade failures.
- **Retry Policy** — Automatically retries transient Redis failures with configurable backoff.
- **Metrics** — Built-in Micrometer metrics for cache hits, misses, errors, and latencies.
- **Fallback Support** — Optionally falls back to a supplier when Redis operations fail.

Supports **Java 8 or later** and **Spring Boot 2.x / 3.x**.

## Maven

    <dependency>
        <groupId>io.github.blaspat</groupId>
        <artifactId>resilient-redis-lettuce-client</artifactId>
        <version>1.0.4</version>
    </dependency>

## Configuration

Uses standard `spring.redis` properties. Additional properties:

    spring:
      redis:
        timeout: 100ms
        connect-timeout: 100ms
        batch-size: 1000
        master:
          host: localhost
          port: 6379
        replica:
          enabled: true
          host: localhost
          port: 6380
        circuit-breaker:
          enabled: true
          failure-rate-threshold: 50
          slow-call-rate-threshold: 80
          slow-call-duration-threshold: 2s
          wait-duration-in-open-state: 30s
          permitted-calls-in-half-open-state: 5
          sliding-window-size: 10
          minimum-calls: 5
        retry:
          enabled: true
          max-attempts: 3
          wait-duration: 200ms
          max-retry-duration: 5s

### Properties Reference

| Property | Default | Description |
|---|---|---|
| `redis.batch-size` | 1000 | Batch size for Redis writer |
| `redis.replica.enabled` | false | Enable read from replica |
| `redis.circuit-breaker.enabled` | false | Enable circuit breaker |
| `redis.circuit-breaker.failure-rate-threshold` | 50 | Failure rate % to trip circuit |
| `redis.circuit-breaker.slow-call-rate-threshold` | 80 | Slow call rate % to trip circuit |
| `redis.circuit-breaker.slow-call-duration-threshold` | 2s | Duration above which a call is slow |
| `redis.circuit-breaker.wait-duration-in-open-state` | 30s | Time before transitioning to half-open |
| `redis.circuit-breaker.permitted-calls-in-half-open-state` | 5 | Calls allowed in half-open state |
| `redis.circuit-breaker.sliding-window-size` | 10 | Size of sliding window for failure tracking |
| `redis.circuit-breaker.minimum-calls` | 5 | Minimum calls before calculating failure rate |
| `redis.retry.enabled` | false | Enable retry on transient failures |
| `redis.retry.max-attempts` | 3 | Maximum retry attempts |
| `redis.retry.wait-duration` | 200ms | Wait time between retries |
| `redis.retry.max-retry-duration` | 5s | Maximum total retry time |

### Metrics (Micrometer)

When Micrometer is on the classpath, the following metrics are exposed:

- `redis.cache.hit{operation}` — Cache hit count per operation
- `redis.cache.miss{operation}` — Cache miss count per operation
- `redis.error{operation,error}` — Error count per operation and error type
- `redis.latency{operation}` — Operation latency histogram
- `redis.fallback{operation}` — Fallback activation count

Prometheus registry is included by default. Access metrics at `/actuator/prometheus`.

## Updates

- **1.0.4** — Circuit breaker, retry policy, Micrometer metrics, scan cursor resource leak fixed
- **1.0.3** — Add `ResilientRedisTemplate`
- **1.0.2** — Non-locking batch size, serialization error handling
- **1.0.1** — Specific exception handling
- **1.0.0** — Initial release

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Blasius Patrick.
