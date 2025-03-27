Resilient Java Redis Client 
===========================

## Overview
Resilient Java Redis Client is a robust, high-performance library for interacting with Redis in Java applications. Designed with fault tolerance and resilience in mind, it enhances the standard Redis Lettuce client capabilities with advanced features to handle common Redis-related issues seamlessly, ensuring that your application remains stable even when Redis encounters problems.

## Key Features:
* Error Handling
  * Automatically manages Redis server errors, serialization errors, and connection issues.
* `@Cacheable`, `@CachePut`, and `@CacheEvict` Compatibility
  * Ensures that the use of `@Cacheable` annotations continues to work without breaking functionality, even if Redis is temporarily unavailable or misbehaving.
* Resilience Focus
  * Provides a robust caching mechanism that mitigates risks associated with Redis downtimes or failures, allowing your application to operate smoothly.

Support **Java 8 or later**

## Updates
* **1.0.3**
  * Add `ResilientRedisTemplate`, a complementary class to RedisTemplate that provides additional resilience features.
* **1.0.2**
  * Add new configuration to set non-locking batch size
  * Handle serialization error, if a serialization error occurs, methods using `@Cacheable` will continue to executed and won’t be stopped.
* **1.0.1**
  * Handle only specific exception
    * `RedisConnectionException`
    * `RedisCommandTimeoutException`
    * `RedisCommandExecutionException`
    * `DataAccessException`
    * `RedisCommandInterruptedException`
* **1.0.0**
  * Initial release


## Maven

    <dependency>
        <groupId>io.github.blaspat</groupId>
        <artifactId>resilient-redis-lettuce-client</artifactId>
        <version>1.0.3</version>
    </dependency>


## Configuration
I use original `spring.redis` properties, but you need to add this additional properties

    spring:
        timeout: 100ms
        connect-timeout: 100ms
        redis:
          batch-size: 1000
          master:
            host: localhost
            port: 6379
          replica:
            enabled: true
            host: localhost
            port: 6380



* `host`: your Redis host
* `port`: your Redis port 
* `timeout`: maximum time the client will wait for a response from the Redis server after a command is sent
* `connect-timeout`: timeout for establishing a connection to the Redis server
* `redis.batch-size`: batch size setting for Redis writer, default 1000
* `redis.replica.enabled`: set to`true` if you want to enable read from replica
  
## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Blasius Patrick.
