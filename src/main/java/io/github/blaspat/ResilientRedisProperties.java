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

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Primary
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class ResilientRedisProperties extends RedisProperties {
    private Master master;
    private Replica replica;
    private Duration timeout;
    private Duration connectTimeout;
    private Integer batchSize;

    // Circuit breaker settings
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    // Retry settings
    private Retry retry = new Retry();
    // Fallback enabled
    private boolean fallbackEnabled = false;

    @Override
    public Duration getConnectTimeout() {
        if (connectTimeout == null) return Duration.ofSeconds(1L);
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Duration getTimeout() {
        if (timeout == null) return Duration.ofMinutes(1L);
        return timeout;
    }

    @Override
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }

    public Replica getReplica() {
        return replica;
    }

    public void setReplica(Replica replica) {
        this.replica = replica;
    }

    public Integer getBatchSize() {
        if (batchSize == null) return 1000;
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public static class Master {
        private String host = "localhost";
        private Integer port = 6379;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    public static class Replica {
        private String host = "localhost";
        private Integer port = 6379;
        private Boolean enabled = false;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    public static class CircuitBreaker {
        private boolean enabled = false;
        private int failureRateThreshold = 50;
        private int slowCallRateThreshold = 80;
        private Duration slowCallDurationThreshold = Duration.ofSeconds(2);
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);
        private int permittedCallsInHalfOpenState = 5;
        private int slidingWindowSize = 10;
        private int minimumCalls = 5;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getFailureRateThreshold() { return failureRateThreshold; }
        public void setFailureRateThreshold(int failureRateThreshold) { this.failureRateThreshold = failureRateThreshold; }
        public int getSlowCallRateThreshold() { return slowCallRateThreshold; }
        public void setSlowCallRateThreshold(int slowCallRateThreshold) { this.slowCallRateThreshold = slowCallRateThreshold; }
        public Duration getSlowCallDurationThreshold() { return slowCallDurationThreshold; }
        public void setSlowCallDurationThreshold(Duration slowCallDurationThreshold) { this.slowCallDurationThreshold = slowCallDurationThreshold; }
        public Duration getWaitDurationInOpenState() { return waitDurationInOpenState; }
        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) { this.waitDurationInOpenState = waitDurationInOpenState; }
        public int getPermittedCallsInHalfOpenState() { return permittedCallsInHalfOpenState; }
        public void setPermittedCallsInHalfOpenState(int permittedCallsInHalfOpenState) { this.permittedCallsInHalfOpenState = permittedCallsInHalfOpenState; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public void setSlidingWindowSize(int slidingWindowSize) { this.slidingWindowSize = slidingWindowSize; }
        public int getMinimumCalls() { return minimumCalls; }
        public void setMinimumCalls(int minimumCalls) { this.minimumCalls = minimumCalls; }
    }

    public static class Retry {
        private boolean enabled = false;
        private int maxAttempts = 3;
        private Duration waitDuration = Duration.ofMillis(200);
        private Duration maxRetryDuration = Duration.ofSeconds(5);

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public Duration getWaitDuration() { return waitDuration; }
        public void setWaitDuration(Duration waitDuration) { this.waitDuration = waitDuration; }
        public Duration getMaxRetryDuration() { return maxRetryDuration; }
        public void setMaxRetryDuration(Duration maxRetryDuration) { this.maxRetryDuration = maxRetryDuration; }
    }

    public CircuitBreaker getCircuitBreakerConfig() {
        return circuitBreaker;
    }

    public void setCircuitBreakerConfig(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public Retry getRetryConfig() {
        return retry;
    }

    public void setRetryConfig(Retry retry) {
        this.retry = retry;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }
}
