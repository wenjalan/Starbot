package wenjalan.starbot.test;

import wenjalan.starbot.engine.YouTubeEngine;

public class Test {

    public static void main(String[] args) {
        final String itte = "https://www.youtube.com/watch?v=F64yFFnZfkI";
        // test the YouTube API Engine
        String recommendation = YouTubeEngine.getRecommendation(itte);
        System.out.println("recommendation: " + recommendation);
    }

}
