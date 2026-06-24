package com.chirp.service;

import com.chirp.util.Keys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Likes and unlikes on tweets.
 *
 * <p>Each tweet keeps an integer like count inside its HASH (incremented with
 * {@code HINCRBY}) and a SET of the user ids that liked it, so we can prevent
 * duplicate likes and support unlike.
 */
public class LikeService {

    private final JedisPool pool;

    public LikeService(JedisPool pool) {
        this.pool = pool;
    }

    public long like(long tweetId, long userId) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.sismember(Keys.likedBy(tweetId), String.valueOf(userId))) {
                return Long.parseLong(jedis.hget(Keys.tweet(tweetId), "likes"));
            }
            jedis.sadd(Keys.likedBy(tweetId), String.valueOf(userId));
            return jedis.hincrBy(Keys.tweet(tweetId), "likes", 1);
        }
    }

    public long unlike(long tweetId, long userId) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.sismember(Keys.likedBy(tweetId), String.valueOf(userId))) {
                return Long.parseLong(jedis.hget(Keys.tweet(tweetId), "likes"));
            }
            jedis.srem(Keys.likedBy(tweetId), String.valueOf(userId));
            return jedis.hincrBy(Keys.tweet(tweetId), "likes", -1);
        }
    }

    public boolean hasLiked(long tweetId, long userId) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sismember(Keys.likedBy(tweetId), String.valueOf(userId));
        }
    }

    public long getLikeCount(long tweetId) {
        try (Jedis jedis = pool.getResource()) {
            String val = jedis.hget(Keys.tweet(tweetId), "likes");
            return val == null ? 0 : Long.parseLong(val);
        }
    }
}
