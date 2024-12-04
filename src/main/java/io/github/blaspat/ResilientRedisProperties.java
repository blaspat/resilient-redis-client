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
}
