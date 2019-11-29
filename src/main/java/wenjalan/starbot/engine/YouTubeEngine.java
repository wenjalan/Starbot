//package wenjalan.starbot.engine;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.services.youtube.YouTube;
//
//import java.io.File;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.Scanner;
//
//// handles all the YouTube Data API stuff
//public class YouTubeEngine {
//
//    // the filepath of the text file with the API key
//    private static final String API_KEY_PATH = "keys/google.key";
//
//    // returns a recommended video URL for a given video
//    public static String getRecommendationFor(String url) {
//
//    }
//
//    // sets up the YouTube Data API for use
//    private static void init() {
//        String key = getKey(API_KEY_PATH);
//        YouTube yt = getYouTube(key);
//    }
//
//    // returns the key specified in the api key path
//    private static String getKey(String filepath) {
//        try {
//            // the key should be a single-line .key file
//            Scanner sc = new Scanner(new File(filepath));
//            return sc.nextLine();
//        } catch (IOException e) {
//            System.err.println("encountered an error while finding Google API Key");
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    // returns a new instance of YouTube given an API key
//    private static YouTube getYouTube(String apiKey) {
//        try {
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            Credential c = authorize(httpTransport);
//        } catch (GeneralSecurityException | IOException e) {
//
//        }
//    }
//
//    // returns an authorized credential object
//    private static Credential autorize(final NetHttpTransport netHttpTransport) {
//        GoogleClientSecrets.
//    }
//
//}
