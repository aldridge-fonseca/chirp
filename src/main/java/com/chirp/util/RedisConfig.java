package com.chirp.util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Builds a {@link JedisPool} from environment variables.
 *
 * <p>Supported environment variables:
 * <ul>
 *   <li>{@code REDIS_HOST} (default {@code localhost})</li>
 *   <li>{@code REDIS_PORT} (default {@code 6379})</li>
 * </ul>
 */
public final class RedisConfig {

    private RedisConfig() {
    }

    public static JedisPool createPool() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(2);

        return new JedisPool(poolConfig, host, port);
    }
}
