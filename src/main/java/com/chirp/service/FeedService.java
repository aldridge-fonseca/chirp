package com.chirp.service;

import com.chirp.model.Tweet;
import com.chirp.util.Keys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads a user's home feed, which is populated by fan-out-on-write.
 *
 * <p>Each user has a LIST at {@code user:{id}:feed} that holds tweet ids in
 * reverse-chronological order. Reading the feed is a single {@code LRANGE}
 * call, which keeps read latency flat regardless of how many people the user
 * follows.
 */
public class FeedService {

    private final JedisPool pool;

    public FeedService(JedisPool pool) {
        this.pool = pool;
    }

    public List<Tweet> getHomeFeed(long userId, int count) {
        try (Jedis jedis = pool.getResource()) {
            List<String> tweetIds = jedis.lrange(Keys.feed(userId), 0, count - 1);
            List<Tweet> tweets = new ArrayList<>();
            for (String idStr : tweetIds) {
                Map<String, String> fields = jedis.hgetAll(Keys.tweet(Long.parseLong(idStr)));
                if (fields == null || fields.isEmpty()) {
                    continue;
                }
                tweets.add(new Tweet(
                        Long.parseLong(fields.get("id")),
                        Long.parseLong(fields.get("authorId")),
                        fields.get("authorUsername"),
                        fields.get("text"),
                        Long.parseLong(fields.get("createdAt")),
                        Long.parseLong(fields.getOrDefault("likes", "0"))));
            }
            return tweets;
        }
    }
}
