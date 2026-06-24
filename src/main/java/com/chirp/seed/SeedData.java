package com.chirp.seed;

import com.chirp.model.User;
import com.chirp.service.LikeService;
import com.chirp.service.TimelineService;
import com.chirp.service.UserService;

/**
 * Loads a small social graph so the demo is interesting on first run.
 *
 * <p>Creates five users, wires up a follow graph, posts a handful of tweets
 * with hashtags, and adds a few likes. Safe to call repeatedly: each call
 * produces fresh ids via {@code INCR}.
 */
public class SeedData {

    private final UserService userService;
    private final TimelineService timelineService;
    private final LikeService likeService;

    public SeedData(UserService userService, TimelineService timelineService, LikeService likeService) {
        this.userService = userService;
        this.timelineService = timelineService;
        this.likeService = likeService;
    }

    public void load() {
        User ada = userService.register("ada", "Ada Lovelace", "First programmer. Analytical engine enthusiast.");
        User alan = userService.register("alan", "Alan Turing", "Computing, cryptography, morphogenesis.");
        User grace = userService.register("grace", "Grace Hopper", "COBOL, nanoseconds, and good naval discipline.");
        User dennis = userService.register("dennis", "Dennis Ritchie", "C and Unix. Keep it simple.");
        User linus = userService.register("linus", "Linus Torvalds", "Linux, Git, and strong opinions.");

        userService.follow(alan.getId(), ada.getId());
        userService.follow(grace.getId(), ada.getId());
        userService.follow(dennis.getId(), alan.getId());
        userService.follow(dennis.getId(), grace.getId());
        userService.follow(linus.getId(), dennis.getId());
        userService.follow(linus.getId(), alan.getId());
        userService.follow(ada.getId(), alan.getId());

        timelineService.postTweet(ada.getId(), ada.getUsername(),
                "The analytical engine weaves algebraic patterns just like the Jacquard loom. #history #computing");
        timelineService.postTweet(alan.getId(), alan.getUsername(),
                "A computer would deserve to be called intelligent if it could deceive a human. #ai #computing");
        timelineService.postTweet(grace.getId(), grace.getUsername(),
                "The most dangerous phrase in language design: we have always done it this way. #programming");
        timelineService.postTweet(dennis.getId(), dennis.getUsername(),
                "Simplicity is prerequisite for reliability. #unix #programming");
        timelineService.postTweet(linus.getId(), linus.getUsername(),
                "Talk is cheap. Show me the code. #opensource #programming");
        timelineService.postTweet(alan.getId(), alan.getUsername(),
                "We can only see a short distance ahead, but we can see plenty there. #ai");
        timelineService.postTweet(grace.getId(), grace.getUsername(),
                "A ship in port is safe, but that is not what ships are for. #engineering");

        likeService.like(1, alan.getId());
        likeService.like(1, grace.getId());
        likeService.like(2, ada.getId());
        likeService.like(2, dennis.getId());
        likeService.like(2, linus.getId());
        likeService.like(5, ada.getId());
        likeService.like(5, grace.getId());
        likeService.like(5, alan.getId());
    }
}
