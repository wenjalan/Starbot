package wenjalan.starbot.engine;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// the Spotify Engine handles the processing of Spotify playlists for audio playback
public class SpotifyEngine {

    // the Spotify Client ID
    public static final String CLIENT_ID = "95c8df5c1ed841bfb35c542c3776b6fa";

    // the instance of the Spotify Web API
    private static SpotifyApi spotify;

    // returns a list of Strings representing the names of the songs in a playlist given a URL
    public static List<String> getNamesOfTracks(String playlistUrl) {
        List<String> trackNamesAndArtists = new ArrayList<>();
        // if we need to (re)initialize the spotify API
        try {
            if (spotify == null || spotify.clientCredentials().build().execute().getExpiresIn() == 0) {
                spotify = initSpotify();
            }
        } catch (SpotifyWebApiException | IOException e) {
            System.err.println("error refreshing spotify api");
            e.printStackTrace();
            return null;
        }
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
    private static SpotifyApi initSpotify() {
        SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(DataEngine.getProperty("spotify-web-api-secret"))
                .build();
        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
        try {
            ClientCredentials credentials = clientCredentialsRequest.execute();
            api.setAccessToken(credentials.getAccessToken());
        } catch (SpotifyWebApiException | IOException e) {
            System.err.println("error getting client credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return api;
    }

}
