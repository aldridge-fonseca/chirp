package com.chirp.service;

import com.chirp.util.Keys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Hashtag trending leaderboard backed by a Redis sorted set.
 *
 * <p>Every hashtag occurrence increments the tag's score in the {@code trending}
 * sorted set via {@code ZINCRBY}. The top tags are read with
 * {@code ZREVRANGE ... WITHSCORES}, which returns them ranked by score.
 */
public class TrendingService {

    private final JedisPool pool;

    public TrendingService(JedisPool pool) {
        this.pool = pool;
    }

    public List<TrendingTag> getTopTrending(int count) {
        try (Jedis jedis = pool.getResource()) {
            List<Tuple> tuples = jedis.zrevrangeWithScores(Keys.trending(), 0, count - 1);
            List<TrendingTag> result = new ArrayList<>();
            for (Tuple t : tuples) {
                result.add(new TrendingTag(t.getElement(), t.getScore()));
            }
            return result;
        }
    }

    public long getTagTweetCount(String tag) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.scard(Keys.tag(tag));
        }
    }

    public static class TrendingTag {
        private final String tag;
        private final double score;

        public TrendingTag(String tag, double score) {
            this.tag = tag;
            this.score = score;
        }

        public String getTag() {
            return tag;
        }

        public double getScore() {
            return score;
        }

        @Override
        public String toString() {
            return "#" + tag + " (" + (long) score + ")";
        }
    }
}
