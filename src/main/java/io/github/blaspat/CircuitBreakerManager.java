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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

public class CircuitBreakerManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerManager(
            int failureRateThreshold,
            int slowCallRateThreshold,
            Duration slowCallDurationThreshold,
            Duration waitDurationInOpenState,
            int permittedCallsInHalfOpenState,
            int slidingWindowSize,
            int minimumCalls) {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .slowCallRateThreshold(slowCallRateThreshold)
                .slowCallDurationThreshold(slowCallDurationThreshold)
                .waitDurationInOpenState(waitDurationInOpenState)
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpenState)
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumCalls)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker("redis");
        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        logger.warn("Redis circuit breaker state changed: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));
    }

    public <T> T execute(Supplier<T> supplier) {
        return circuitBreaker.executeSupplier(supplier);
    }

    public void execute(Runnable runnable) {
        circuitBreaker.executeRunnable(runnable);
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public boolean isOpen() {
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    public boolean is_half_open() {
        return circuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN;
    }
}
