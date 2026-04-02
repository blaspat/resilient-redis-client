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

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

public class RetryManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Retry retry;

    public RetryManager(
            int maxAttempts,
            Duration waitDuration,
            Duration maxRetryDuration) {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryExceptions(Exception.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        this.retry = registry.retry("redis");

        this.retry.getEventPublisher()
                .onRetry(event ->
                        logger.warn("Redis retry attempt #{} due to: {}",
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable().getMessage()));
    }

    public <T> T execute(Supplier<T> supplier) {
        return Retry.decorateSupplier(retry, supplier).get();
    }

    public void execute(Runnable runnable) {
        Retry.decorateRunnable(retry, runnable).run();
    }

    public Retry getRetry() {
        return retry;
    }
}
