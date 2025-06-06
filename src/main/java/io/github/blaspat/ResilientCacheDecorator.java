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
import org.springframework.cache.Cache;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.Callable;

public class ResilientCacheDecorator implements Cache {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Cache delegate;

    public ResilientCacheDecorator(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            return delegate.get(key);
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache retrieval error: " + e.getMessage());
            return null;
        } catch (SerializationException ex) {
            logger.debug("Cache retrieval error serialization for key {} : {}, cache evicted then returned", key, ex.getMessage());
            this.evict(key);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        try {
            return delegate.get(key, type);
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache retrieval error: " + e.getMessage());
            return null;
        } catch (SerializationException ex) {
            logger.debug("Cache retrieval error serialization for key {} : {}, cache evicted then returned", key, ex.getMessage());
            this.evict(key);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            return delegate.get(key, valueLoader);
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache retrieval error: " + e.getMessage());
            return null;
        } catch (SerializationException ex) {
            logger.debug("Cache retrieval error serialization for key {} : {}, cache evicted then returned", key, ex.getMessage());
            this.evict(key);
            return null;
        }
    }

    @Override
    public void put(Object key, Object value) {
        try {
            delegate.put(key, value);
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache put error: " + e.getMessage());
        }
    }

    @Override
    public void evict(Object key) {
        try {
            delegate.evict(key);
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache eviction error: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        try {
            delegate.clear();
        } catch (RedisConnectionException |
                 RedisCommandTimeoutException |
                 RedisCommandExecutionException |
                 DataAccessException |
                 RedisCommandInterruptedException
                e  )
        {
            logger.error("Cache clear error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }
}
