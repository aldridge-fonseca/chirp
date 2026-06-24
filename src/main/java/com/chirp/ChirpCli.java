package com.chirp;

import com.chirp.model.Tweet;
import com.chirp.model.User;
import com.chirp.seed.SeedData;
import com.chirp.service.FeedService;
import com.chirp.service.LikeService;
import com.chirp.service.TimelineService;
import com.chirp.service.TrendingService;
import com.chirp.service.UserService;
import com.chirp.util.RedisConfig;
import redis.clients.jedis.JedisPool;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive command-line interface for Chirp.
 *
 * <p>Run with {@code mvn compile exec:java -Dexec.mainClass="com.chirp.ChirpCli"}
 * or build the shaded jar and run {@code java -jar target/chirp-1.0.0.jar}.
 */
public class ChirpCli {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final UserService userService;
    private final TimelineService timelineService;
    private final FeedService feedService;
    private final LikeService likeService;
    private final TrendingService trendingService;
    private final Scanner in;

    public ChirpCli(JedisPool pool) {
        this.userService = new UserService(pool);
        this.timelineService = new TimelineService(pool);
        this.feedService = new FeedService(pool);
        this.likeService = new LikeService(pool);
        this.trendingService = new TrendingService(pool);
        this.in = new Scanner(System.in);
    }

    public static void main(String[] args) {
        try (JedisPool pool = RedisConfig.createPool()) {
            new ChirpCli(pool).run();
        }
    }

    public void run() {
        System.out.println("=== Chirp ===");
        System.out.println("A Redis-backed mini Twitter.");
        System.out.println();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> register();
                case "2" -> follow();
                case "3" -> post();
                case "4" -> showHomeFeed();
                case "5" -> showUserTimeline();
                case "6" -> like();
                case "7" -> showTrending();
                case "8" -> seedDemo();
                case "9" -> {
                    running = false;
                    System.out.println("Goodbye.");
                }
                default -> System.out.println("Unknown option. Pick 1-9.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("1. Register");
        System.out.println("2. Follow");
        System.out.println("3. Post");
        System.out.println("4. Show home feed");
        System.out.println("5. Show user timeline");
        System.out.println("6. Like");
        System.out.println("7. Show trending");
        System.out.println("8. Seed demo data");
        System.out.println("9. Quit");
        System.out.print("> ");
    }

    private void register() {
        System.out.print("Username: ");
        String username = in.nextLine().trim();
        System.out.print("Display name: ");
        String displayName = in.nextLine().trim();
        System.out.print("Bio (optional): ");
        String bio = in.nextLine().trim();

        User user = userService.register(username, displayName, bio);
        System.out.println("Registered " + user + " with id " + user.getId());
    }

    private void follow() {
        long followerId = readUserId("Your user id: ");
        long targetId = readUserId("User id to follow: ");
        try {
            userService.follow(followerId, targetId);
            System.out.println("User " + followerId + " is now following " + targetId);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void post() {
        long authorId = readUserId("Your user id: ");
        User author = userService.findById(authorId);
        if (author == null) {
            System.out.println("No user with id " + authorId);
            return;
        }
        System.out.print("Text: ");
        String text = in.nextLine().trim();
        if (text.isEmpty()) {
            System.out.println("Empty post, nothing to do.");
            return;
        }
        Tweet tweet = timelineService.postTweet(authorId, author.getUsername(), text);
        System.out.println("Posted: " + tweet);
    }

    private void showHomeFeed() {
        long userId = readUserId("Your user id: ");
        List<Tweet> feed = feedService.getHomeFeed(userId, 10);
        if (feed.isEmpty()) {
            System.out.println("Your feed is empty. Follow people or have them post.");
            return;
        }
        System.out.println("--- Home feed for user " + userId + " ---");
        for (Tweet t : feed) {
            printTweet(t);
        }
    }

    private void showUserTimeline() {
        long userId = readUserId("User id: ");
        List<Tweet> timeline = timelineService.getUserTimeline(userId, 10);
        if (timeline.isEmpty()) {
            System.out.println("No posts yet for user " + userId);
            return;
        }
        System.out.println("--- Timeline for user " + userId + " ---");
        for (Tweet t : timeline) {
            printTweet(t);
        }
    }

    private void like() {
        long userId = readUserId("Your user id: ");
        long tweetId = readLong("Tweet id: ");
        long count = likeService.like(tweetId, userId);
        System.out.println("Liked tweet " + tweetId + ". It now has " + count + " likes.");
    }

    private void showTrending() {
        List<TrendingService.TrendingTag> tags = trendingService.getTopTrending(10);
        if (tags.isEmpty()) {
            System.out.println("No hashtags tracked yet. Post something with a #tag.");
            return;
        }
        System.out.println("--- Trending hashtags ---");
        int rank = 1;
        for (TrendingService.TrendingTag tag : tags) {
            System.out.printf("%2d. %-20s %d posts%n", rank, "#" + tag.getTag(), (long) tag.getScore());
            rank++;
        }
    }

    private void seedDemo() {
        SeedData seed = new SeedData(userService, timelineService, likeService);
        seed.load();
        System.out.println("Demo data loaded: 5 users, follow graph, tweets, and likes.");
    }

    private void printTweet(Tweet t) {
        System.out.printf("[%d] @%s  %s%n", t.getId(), t.getAuthorUsername(), TIME.format(Instant.ofEpochMilli(t.getCreatedAt())));
        System.out.println("    " + t.getText());
        System.out.println("    " + t.getLikes() + " likes");
        System.out.println();
    }

    private long readUserId(String prompt) {
        return readLong(prompt);
    }

    private long readLong(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Long.parseLong(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a number: ");
            }
        }
    }
}
