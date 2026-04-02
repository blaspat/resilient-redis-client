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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class RedisFallbackSupport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Supplier<?> fallbackSupplier;

    public RedisFallbackSupport(Supplier<?> fallbackSupplier) {
        this.fallbackSupplier = fallbackSupplier;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrFallback(Supplier<T> primarySupplier) {
        try {
            return primarySupplier.get();
        } catch (Exception e) {
            logger.warn("Redis operation failed, falling back: {}", e.getMessage());
            if (fallbackSupplier != null) {
                return (T) fallbackSupplier.get();
            }
            return null;
        }
    }

    public interface FallbackSupplier<T> extends Supplier<T> {
    }
}
