package com.chirp.util;

/**
 * Central place for every Redis key pattern used by Chirp.
 *
 * <p>Keeping keys in one spot makes the data model easy to audit and prevents
 * typos that would scatter data across wrong namespaces.
 */
public final class Keys {

    private Keys() {
    }

    /** HASH holding user profile fields. Fields: username, displayName, bio, createdAt. */
    public static String user(long id) {
        return "user:" + id;
    }

    /** SET of user ids that {@code id} is following. */
    public static String following(long id) {
        return "user:" + id + ":following";
    }

    /** SET of user ids that follow {@code id}. */
    public static String followers(long id) {
        return "user:" + id + ":followers";
    }

    /** LIST (newest first) of tweet ids posted by {@code id}. */
    public static String timeline(long id) {
        return "user:" + id + ":timeline";
    }

    /** LIST (newest first) of tweet ids in {@code id}'s home feed (fan-out). */
    public static String feed(long id) {
        return "user:" + id + ":feed";
    }

    /** LIST (newest first) of all tweet ids across the platform. */
    public static String globalTimeline() {
        return "global:timeline";
    }

    /** HASH holding a single tweet. Fields: authorId, authorUsername, text, createdAt, likes. */
    public static String tweet(long id) {
        return "tweet:" + id;
    }

    /** Counter key for the next tweet id. */
    public static String tweetCounter() {
        return "counter:tweet";
    }

    /** Counter key for the next user id. */
    public static String userCounter() {
        return "counter:user";
    }

    /** SET of user ids that liked tweet {@code id}. */
    public static String likedBy(long tweetId) {
        return "tweet:" + tweetId + ":likedBy";
    }

    /** SET of tweet ids that use tag {@code tag}. */
    public static String tag(String tag) {
        return "tag:" + tag.toLowerCase();
    }

    /** SORTED SET leaderboard of hashtag scores. Member = tag, score = usage count. */
    public static String trending() {
        return "trending";
    }
}
