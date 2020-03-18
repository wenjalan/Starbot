package wenjalan.starbot.engine.media;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import wenjalan.starbot.core.Starbot;
import wenjalan.starbot.engine.DataEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// the Spotify Engine handles the processing of Spotify playlists for audio playback
public class SpotifyEngine {

    // the time we last refreshed the spotify token
    private static long lastTokenRefresh;

    // the refresh interval for the spotify token, in seconds
    public static final int REFRESH_INTERVAL = 3000;

    // the Spotify Client ID
    public static final String CLIENT_ID = "95c8df5c1ed841bfb35c542c3776b6fa";

    // the instance of the Spotify Web API
    private static SpotifyApi spotify = null;

    // returns a list of Strings representing the names of the songs in a playlist given a URL
    public static List<String> getNamesOfTracks(String playlistUrl) {
        // start Spotify token refresh loop if not already started
        if (spotify == null || (System.currentTimeMillis() - lastTokenRefresh) / 1000 >= REFRESH_INTERVAL) {
            lastTokenRefresh = System.currentTimeMillis();
            initSpotify();
        }

        // create queriable Strings
        List<String> trackNamesAndArtists = new ArrayList<>();
        String playlistId = playlistUrl.replace("https://open.spotify.com/playlist/", "").substring(0, 22);
        try {
            Playlist playlist = spotify.getPlaylist(playlistId).build().execute();
            PlaylistTrack[] tracks = playlist.getTracks().getItems();
            for (PlaylistTrack t : tracks) {
                Track track = t.getTrack();
                String name = track.getName();
                String artists = "";
                ArtistSimplified[] as = track.getArtists();
                for (ArtistSimplified a : as) {
                    artists += a.getName() + " ";
                }
                trackNamesAndArtists.add(name + " by " + artists);
            }
        } catch (SpotifyWebApiException | IOException e) {
            System.err.println("error retrieving playlist from spotify web api: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return trackNamesAndArtists;
    }

    // initializes an instance of the Spotify Web API for use
    private static void initSpotify() {
        spotify = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(DataEngine.getProperty("spotify-web-api-secret"))
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotify.clientCredentials().build();
        try {
            ClientCredentials credentials = clientCredentialsRequest.execute();
            spotify.setAccessToken(credentials.getAccessToken());
            System.out.println("refreshed Spotify token");
        } catch (SpotifyWebApiException | IOException e) {
            System.err.println("error getting client credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
