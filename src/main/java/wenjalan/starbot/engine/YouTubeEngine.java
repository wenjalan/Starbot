package wenjalan.starbot.engine;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

// handles all the YouTube Data API stuff
public class YouTubeEngine {

    // the instance of YouTube's Data API
    private static YouTube youtube = null;

    // returns a new YouTube instance
    // instance is not OAuth certified
    private static YouTube getYoutube() {
        YouTube youTube = null;
        try {
            // youTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
            youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException { }
            }).setApplicationName("starbot").build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return youTube;
    }

    // returns the next recommended video for a given video
    public static String getRecommendation(String videoUrl) {
        String id = videoUrl.replace("https://www.youtube.com/watch?v=", "");
        return getRecommendationFor(id);
    }

    // searches for a recommendation based on a video id
    // returns the recommended video's id
    private static String getRecommendationFor(String videoId) {
        // init check
        if (youtube == null) {
            youtube = getYoutube();
        }

        // return the first related video we find
        try {
            List<SearchResult> related = youtube.search().list("snippet")
                    .setRelatedToVideoId(videoId)
                    .setType("video")
                    .setKey(DataEngine.getProperty("youtube-data-api-secret"))
                    .execute().getItems();
            for (SearchResult result : related) {
                // System.out.println(result.toPrettyString());
                ResourceId resId = result.getId();
                if (resId.getKind().equalsIgnoreCase("youtube#video")) {
                    String id = resId.getVideoId();
                    return id;
                }
            }
        } catch (IOException e) {
            System.err.println("error getting recommendations for video id " + videoId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

//    // example of a search call, from scratchpad
//    // for reference only
//    private static void search() throws IOException {
//        // create a new YouTube instance to use
//        YouTube youTube = youtube;
//
//        // get something to look up
//        System.out.print("enter something to look up: ");
//        String query = new Scanner(System.in).nextLine();
//
//        // create a new Search List
//        YouTube.Search.List search = youTube.search().list("id,snippet");
//
//
//        // search.setKey(/* API KEY GOES HERE */);
//        search.setQ(query);
//
//        search.setType("video");
//
//        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
//        search.setMaxResults(10L);
//
//        SearchListResponse response = search.execute();
//
//        List<SearchResult> responses = response.getItems();
//
//        for (SearchResult r : responses) {
//            System.out.print(r.getSnippet().getTitle());
//            System.out.println(" URL: " + "https://youtu.be/" + r.getId().getVideoId());
//        }
//    }

}
