package com.chirp.service;

import com.chirp.model.Tweet;
import com.chirp.util.Keys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Posting tweets and reading timelines.
 *
 * <p>Posting uses fan-out-on-write: the tweet id is pushed onto the author's
 * own timeline, the global timeline, and every follower's home feed inside a
 * single Redis transaction so readers never see a partial state.
 */
public class TimelineService {

    private static final Pattern HASHTAG = Pattern.compile("#(\\w+)");

    private final JedisPool pool;

    public TimelineService(JedisPool pool) {
        this.pool = pool;
    }

    public Tweet postTweet(long authorId, String authorUsername, String text) {
        try (Jedis jedis = pool.getResource()) {
            long tweetId = jedis.incr(Keys.tweetCounter());
            long createdAt = System.currentTimeMillis();

            Map<String, String> fields = new HashMap<>();
            fields.put("id", String.valueOf(tweetId));
            fields.put("authorId", String.valueOf(authorId));
            fields.put("authorUsername", authorUsername);
            fields.put("text", text);
            fields.put("createdAt", String.valueOf(createdAt));
            fields.put("likes", "0");

            Set<String> followerIds = jedis.smembers(Keys.followers(authorId));

            Transaction tx = jedis.multi();
            tx.hset(Keys.tweet(tweetId), fields);
            tx.lpush(Keys.timeline(authorId), String.valueOf(tweetId));
            tx.lpush(Keys.globalTimeline(), String.valueOf(tweetId));
            tx.lpush(Keys.feed(authorId), String.valueOf(tweetId));
            for (String fid : followerIds) {
                tx.lpush(Keys.feed(Long.parseLong(fid)), String.valueOf(tweetId));
            }
            tx.exec();

            indexHashtags(jedis, tweetId, text);

            return new Tweet(tweetId, authorId, authorUsername, text, createdAt, 0);
        }
    }

    private void indexHashtags(Jedis jedis, long tweetId, String text) {
        List<String> tags = extractHashtags(text);
        if (tags.isEmpty()) {
            return;
        }
        try (Pipeline pipe = jedis.pipelined()) {
            for (String tag : tags) {
                pipe.sadd(Keys.tag(tag), String.valueOf(tweetId));
                pipe.zincrby(Keys.trending(), 1.0, tag.toLowerCase());
            }
            pipe.sync();
        }
    }

    public static List<String> extractHashtags(String text) {
        List<String> tags = new ArrayList<>();
        Matcher matcher = HASHTAG.matcher(text);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
    }

    public List<Tweet> getUserTimeline(long userId, int count) {
        try (Jedis jedis = pool.getResource()) {
            List<String> tweetIds = jedis.lrange(Keys.timeline(userId), 0, count - 1);
            return loadTweets(jedis, tweetIds);
        }
    }

    public List<Tweet> getGlobalTimeline(int count) {
        try (Jedis jedis = pool.getResource()) {
            List<String> tweetIds = jedis.lrange(Keys.globalTimeline(), 0, count - 1);
            return loadTweets(jedis, tweetIds);
        }
    }

    private List<Tweet> loadTweets(Jedis jedis, List<String> tweetIds) {
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
