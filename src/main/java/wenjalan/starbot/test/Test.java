package wenjalan.starbot.test;

import wenjalan.starbot.engine.YouTubeEngine;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        final String itte = "https://www.youtube.com/watch?v=F64yFFnZfkI";
        // test the YouTube API Engine
        List<String> recommendations = YouTubeEngine.getRecommendation(itte, 50);
        System.out.println("recommendation: " + recommendations);
    }

}
