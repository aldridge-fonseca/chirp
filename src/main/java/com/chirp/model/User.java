package com.chirp.model;

/**
 * A registered Chirp user, stored as a Redis HASH at key {@code user:{id}}.
 */
public class User {

    private final long id;
    private final String username;
    private String displayName;
    private String bio;
    private final long createdAt;

    public User(long id, String username, String displayName, String bio, long createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "@" + username + " (" + displayName + ")";
    }
}
