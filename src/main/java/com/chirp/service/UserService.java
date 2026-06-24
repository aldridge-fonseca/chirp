package com.chirp.service;

import com.chirp.model.User;
import com.chirp.util.Keys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Registration, profile lookup, and the follow graph.
 *
 * <p>Users live in HASHes. The follow graph uses two SETs per user: one for
 * who they follow and one for who follows them. Following is bidirectional in
 * bookkeeping: when A follows B, B is added to A's following set and A is
 * added to B's followers set.
 */
public class UserService {

    private final JedisPool pool;

    public UserService(JedisPool pool) {
        this.pool = pool;
    }

    public User register(String username, String displayName, String bio) {
        try (Jedis jedis = pool.getResource()) {
            long id = jedis.incr(Keys.userCounter());
            long createdAt = System.currentTimeMillis();

            Map<String, String> fields = new HashMap<>();
            fields.put("id", String.valueOf(id));
            fields.put("username", username);
            fields.put("displayName", displayName);
            fields.put("bio", bio == null ? "" : bio);
            fields.put("createdAt", String.valueOf(createdAt));

            jedis.hset(Keys.user(id), fields);
            return new User(id, username, displayName, bio, createdAt);
        }
    }

    public User findById(long id) {
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> fields = jedis.hgetAll(Keys.user(id));
            if (fields == null || fields.isEmpty()) {
                return null;
            }
            return new User(
                    id,
                    fields.get("username"),
                    fields.get("displayName"),
                    fields.get("bio"),
                    Long.parseLong(fields.get("createdAt")));
        }
    }

    public User findByUsername(String username) {
        try (Jedis jedis = pool.getResource()) {
            for (String key : jedis.keys("user:*")) {
                if (key.contains(":following") || key.contains(":followers")
                        || key.contains(":timeline") || key.contains(":feed")) {
                    continue;
                }
                Map<String, String> fields = jedis.hgetAll(key);
                if (username.equals(fields.get("username"))) {
                    long id = Long.parseLong(key.substring("user:".length()));
                    return new User(id, username, fields.get("displayName"), fields.get("bio"),
                            Long.parseLong(fields.get("createdAt")));
                }
            }
            return null;
        }
    }

    public void follow(long followerId, long targetId) {
        if (followerId == targetId) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.sadd(Keys.following(followerId), String.valueOf(targetId));
            jedis.sadd(Keys.followers(targetId), String.valueOf(followerId));
        }
    }

    public void unfollow(long followerId, long targetId) {
        try (Jedis jedis = pool.getResource()) {
            jedis.srem(Keys.following(followerId), String.valueOf(targetId));
            jedis.srem(Keys.followers(targetId), String.valueOf(followerId));
        }
    }

    public java.util.Set<String> getFollowing(long userId) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(Keys.following(userId));
        }
    }

    public java.util.Set<String> getFollowers(long userId) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(Keys.followers(userId));
        }
    }
}
