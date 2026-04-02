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

import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisCommandInterruptedException;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ResilientRedisTemplate<K, V> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RedisTemplate<K, V> redisTemplate;
    private final CircuitBreakerManager circuitBreakerManager;
    private final RetryManager retryManager;
    private final ResilientRedisMetrics metrics;
    private final boolean circuitBreakerEnabled;
    private final boolean retryEnabled;

    public ResilientRedisTemplate(RedisTemplate<K, V> redisTemplate) {
        this(redisTemplate, null, null, null);
    }

    public ResilientRedisTemplate(
            RedisTemplate<K, V> redisTemplate,
            CircuitBreakerManager circuitBreakerManager,
            RetryManager retryManager,
            ResilientRedisMetrics metrics) {
        this.redisTemplate = redisTemplate;
        this.circuitBreakerManager = circuitBreakerManager;
        this.retryManager = retryManager;
        this.metrics = metrics;
        this.circuitBreakerEnabled = circuitBreakerManager != null;
        this.retryEnabled = retryManager != null;
    }

    public V get(K key) {
        return executeWithResilience("get", () -> {
            try {
                V result = redisTemplate.opsForValue().get(key);
                if (result != null) {
                    recordHit("get");
                } else {
                    recordMiss("get");
                }
                return result;
            } catch (SerializationException ex) {
                logger.warn("Serialization error for key '{}', evicting corrupted cache entry: {}", key, ex.getMessage());
                this.evict(key);
                return null;
            }
        });
    }

    public void put(K key, V value) {
        executeWithResilience("put", () -> {
            try {
                redisTemplate.opsForValue().set(key, value);
            } catch (SerializationException ex) {
                logger.warn("Serialization error while putting key '{}': {}", key, ex.getMessage());
                throw ex;
            }
        });
    }

    public void putWithTTL(K key, V value, long timeout, TimeUnit unit) {
        executeWithResilience("putWithTTL", () -> {
            try {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } catch (SerializationException ex) {
                logger.warn("Serialization error while putting key '{}' with TTL: {}", key, ex.getMessage());
                throw ex;
            }
        });
    }

    public void evict(K key) {
        executeWithResilience("evict", () -> redisTemplate.delete(key));
    }

    public void clear() {
        executeWithResilience("clear", () -> {
            try {
                redisTemplate.getConnectionFactory().getConnection().flushDb();
            } catch (RedisConnectionException | RedisCommandTimeoutException |
                     RedisCommandExecutionException | DataAccessException |
                     RedisCommandInterruptedException e) {
                logger.error("Redis clear error: {}", e.getMessage());
            }
        });
    }

    public void clear(String keyNamePrefix) {
        executeWithResilience("clearPrefix", () -> {
            try (Cursor<K> cursor = redisTemplate.scan(ScanOptions.scanOptions()
                    .match(keyNamePrefix + "*")
                    .count(100)
                    .build())) {
                while (cursor.hasNext()) {
                    K key = cursor.next();
                    redisTemplate.delete(key);
                }
            } catch (RedisConnectionException | RedisCommandTimeoutException |
                     RedisCommandExecutionException | DataAccessException |
                     RedisCommandInterruptedException e) {
                logger.error("Redis clear(prefix) error: {}", e.getMessage());
            }
        });
    }

    private <T> T executeWithResilience(String operation, Supplier<T> supplier) {
        Supplier<T> decorated = supplier;

        if (retryEnabled) {
            decorated = () -> retryManager.execute(supplier);
        }

        if (circuitBreakerEnabled) {
            return circuitBreakerManager.execute(decorated::get);
        }

        try {
            return decorated.get();
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis {} error: {}", operation, e.getMessage());
            if (metrics != null) {
                metrics.recordError(operation, e.getClass().getSimpleName());
            }
            return null;
        }
    }

    private void executeWithResilience(String operation, Runnable runnable) {
        Runnable decorated = runnable;

        if (retryEnabled) {
            decorated = () -> retryManager.execute(runnable);
        }

        if (circuitBreakerEnabled) {
            circuitBreakerManager.execute(decorated);
            return;
        }

        try {
            decorated.run();
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis {} error: {}", operation, e.getMessage());
            if (metrics != null) {
                metrics.recordError(operation, e.getClass().getSimpleName());
            }
        }
    }

    private void recordHit(String operation) {
        if (metrics != null) {
            metrics.recordHit(operation);
        }
    }

    private void recordMiss(String operation) {
        if (metrics != null) {
            metrics.recordMiss(operation);
        }
    }
}
