package com.chirp.model;

import java.util.List;

/**
 * A single chirp (tweet), stored as a Redis HASH at key {@code tweet:{id}}.
 */
public class Tweet {

    private final long id;
    private final long authorId;
    private final String authorUsername;
    private final String text;
    private final long createdAt;
    private long likes;
    private List<String> hashtags;

    public Tweet(long id, long authorId, String authorUsername, String text, long createdAt, long likes) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.text = text;
        this.createdAt = createdAt;
        this.likes = likes;
    }

    public long getId() {
        return id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getText() {
        return text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public String toString() {
        return "[#" + id + "] @" + authorUsername + ": " + text + " (" + likes + " likes)";
    }
}
