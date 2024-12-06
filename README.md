Resilient Java Redis Client 
===========================

## Overview
Resilient Java Redis Client is a robust, high-performance library for interacting with Redis in Java applications. Designed with fault tolerance and resilience in mind, it enhances the standard Redis Lettuce client capabilities with advanced features to handle common challenges in distributed systems.

Support **Java 8 or later**

## Updates
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
        <version>1.0.1</version>
    </dependency>


## Configuration
I use original `spring.redis` properties, but you need to add this additional properties

    spring:
        timeout: 100ms
        connect-timeout: 100ms
        redis:
          master:
            host: localhost
            port: 6379
          replica:
            enabled: true
            host: localhost
            port: 6380



* `host`: your Redis host
* `port`: your Redis port 
* `redis.replica.enabled`: set to`true` if you want to enable read from replica
* `timeout`: maximum time the client will wait for a response from the Redis server after a command is sent
* `connect-timeout`: timeout for establishing a connection to the Redis server

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Blasius Patrick.
