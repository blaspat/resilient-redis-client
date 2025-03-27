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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.TimeUnit;

public class ResilientRedisTemplate<K, V>{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RedisTemplate<K, V> redisTemplate;

    public ResilientRedisTemplate(RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public V get(K key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (SerializationException ex) {
            logger.warn("Serialization error for key '{}', evicting corrupted cache entry: {}", key, ex.getMessage());
            this.evict(key);
            return null;
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis get error: {}", e.getMessage());
            return null;
        }
    }

    public void put(K key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (SerializationException ex) {
            logger.warn("Serialization error while putting key '{}': {}", key, ex.getMessage());
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis put error: {}", e.getMessage());
        }
    }

    public void putWithTTL(K key, V value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (SerializationException ex) {
            logger.warn("Serialization error while putting key '{}' with TTL: {}", key, ex.getMessage());
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis putWithTTL error: {}", e.getMessage());
        }
    }

    public void evict(K key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis eviction error: {}", e.getMessage());
        }
    }

    public void clear() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis clear error: {}", e.getMessage());
        }
    }

    public void clear(String keyNamePrefix) {
        try {
            redisTemplate.scan(ScanOptions.scanOptions().match(keyNamePrefix + "*").build()).forEachRemaining(redisTemplate::delete);
        } catch (RedisConnectionException | RedisCommandTimeoutException |
                 RedisCommandExecutionException | DataAccessException |
                 RedisCommandInterruptedException e) {
            logger.error("Redis clear error: {}", e.getMessage());
        }
    }
}
