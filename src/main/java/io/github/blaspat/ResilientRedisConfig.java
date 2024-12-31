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

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class ResilientRedisConfig {
    @Value("${version}")
    private String projectVersion;
    @Value("${artifactId}")
    private String projectId;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ResilientRedisProperties resilientRedisProperties;

    public ResilientRedisConfig(ResilientRedisProperties resilientRedisProperties) {
        this.resilientRedisProperties = resilientRedisProperties;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        GenericObjectPoolConfig<Object> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(resilientRedisProperties.getLettuce().getPool().getMaxActive());
        config.setMaxIdle(resilientRedisProperties.getLettuce().getPool().getMaxIdle());
        config.setMinIdle(resilientRedisProperties.getLettuce().getPool().getMinIdle());
        config.setMaxWait(resilientRedisProperties.getLettuce().getPool().getMaxWait());

        if (Boolean.TRUE == resilientRedisProperties.getReplica().getEnabled()) {
            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(config)
                    .commandTimeout(resilientRedisProperties.getTimeout())
                    .clientOptions(
                            ClientOptions.builder()
                                    .autoReconnect(true)
                                    .socketOptions(SocketOptions.builder().connectTimeout(resilientRedisProperties.getConnectTimeout()).build())
                                    .build()
                    )
                    .readFrom(ReadFrom.REPLICA)
                    .build();

            RedisStaticMasterReplicaConfiguration redisConfiguration = new RedisStaticMasterReplicaConfiguration(resilientRedisProperties.getMaster().getHost(), resilientRedisProperties.getMaster().getPort());
            redisConfiguration.addNode(resilientRedisProperties.getReplica().getHost(), resilientRedisProperties.getReplica().getPort());
            return new LettuceConnectionFactory(redisConfiguration, clientConfig);
        } else {
            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(config)
                    .commandTimeout(resilientRedisProperties.getTimeout())
                    .clientOptions(
                            ClientOptions.builder()
                                    .autoReconnect(true)
                                    .socketOptions(SocketOptions.builder().connectTimeout(resilientRedisProperties.getConnectTimeout()).build())
                                    .build()
                    )
                    .build();

            RedisStaticMasterReplicaConfiguration redisConfiguration = new RedisStaticMasterReplicaConfiguration(resilientRedisProperties.getMaster().getHost(), resilientRedisProperties.getMaster().getPort());
            return new LettuceConnectionFactory(redisConfiguration, clientConfig);
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> map = new HashMap<>();
        return new ResilientRedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.scan(resilientRedisProperties.getBatchSize())),
                RedisCacheConfiguration.defaultCacheConfig(),
                map
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        log.info("Running {} version {} with {} replica setting", projectId, projectVersion, resilientRedisProperties.getReplica().getEnabled());
    }
}