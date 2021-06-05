package wenjalan.starbot.engine.audio;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wenjalan.starbot.data.AssetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// handles the processing of Spotify playlist and album links
public class SpotifyHelper {

    // logger
    Logger logger = LogManager.getLogger();

    // the number of milliseconds until authentication expires
    public static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(5); // 5 minutes

    // the timestamp of when we last authenticated
    private long lastRefresh = -1;

    // instance
    private static SpotifyHelper instance = null;

    // SpotifyApi
    private SpotifyApi spotify = null;
    private String clientId = null;
    private String clientSecret = null;

    // constructor
    private SpotifyHelper(String clientId, String clientSecret) {
        // singleton
        if (instance != null) {
            throw new IllegalStateException("New instances of SpotifyHelper cannot be instantiated");
        }

        // init Spotify API
        try {
            spotify = getSpotify(clientId, clientSecret);
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Encountered an error while authenticating Spotify API");
            logger.error(e.getMessage());
        }
    }

    // returns a list of search queries that correspond with a Spotify Playlist, Spotify Track, or Album URL
    public List<String> getSearchQueries(String spotifyUrl) {
        // refresh Spotify if we need to
        if (System.currentTimeMillis() - lastRefresh >= REFRESH_INTERVAL) {
            try {
                spotify = getSpotify(this.clientId, this.clientSecret);
            } catch (IOException | SpotifyWebApiException e) {
                logger.error("Encountered an error while refreshing Spotify API");
                logger.error(e.getMessage());
                return Collections.emptyList();
            }
        }

        // get the playlist or album
        // playlists and albums use track and tracksimplifieds respectively,
        // making this process somewhat annoying
        List<String> queries = new ArrayList<>();
        try {
            // URL is a playlist
            if (spotifyUrl.contains("/playlist/")) {
                // get tracks
                Track[] tracks = retrievePlaylist(spotifyUrl);
                // get all the names and the artists and combine into a query
                // "artist1 artist2 artist3 ... songName"
                for (Track t : tracks) {
                    String trackName = t.getName();
                    String artistNames = Arrays.stream(t.getArtists())
                            .map(ArtistSimplified::getName)
                            .collect(Collectors.joining(" "));
                    queries.add(artistNames + " " + trackName);
                }
            }

            // URL is a track
            else if (spotifyUrl.contains("/track/")) {
                // get track id
                String trackId = spotifyUrl.substring("https://open.spotify.com/track/".length()).substring(0, 22);

                // get track
                Track t = spotify.getTrack(trackId).build().execute();

                // get the title and the track artist
                String query = t.getName() + " " + Arrays.stream(t.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(" "));

                // return the query
                return Collections.singletonList(query);
            }

            // playlist is an album
            else if (spotifyUrl.contains("/album/")) {
                // get tracks
                TrackSimplified[] tracks = retrieveAlbum(spotifyUrl);
                // get all names and artists and combine into a query
                // "artist1 artist2 ... songName"
                for (TrackSimplified t : tracks) {
                    String trackName = t.getName();
                    String artistNames = Arrays.stream(t.getArtists())
                            .map(ArtistSimplified::getName)
                            .collect(Collectors.joining(" "));
                    queries.add(artistNames + " " + trackName);
                }
            }

            // neither a playlist or an album
            else {
                throw new IllegalArgumentException("Spotify URL is not a playlist or an album");
            }
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Encountered an error while retrieving Spotify resource");
            logger.error(e.getMessage());
        }
        // return the queries
        return queries;
    }

    // retrieves a Spotify Album given its URL
    // album link example:
    // https://open.spotify.com/album/6pZ0SrZCP8Bm28L6JhMtBy?si=x3lfgnj7QISxdjHFcqmLvA
    private TrackSimplified[] retrieveAlbum(String albumUrl) throws IOException, SpotifyWebApiException {
        String id = albumUrl.substring("https://open.spotify.com/album/".length()).substring(0, 22);
        Album album = spotify.getAlbum(id).build().execute();
        TrackSimplified[] tracks = album.getTracks().getItems();
        return tracks;
    }

    // retrieves a Spotify Playlist given its URL
    // playlist link example:
    // https://open.spotify.com/playlist/0CFuMybe6s77w6QQrJjW7d?si=6xuVtTV3SQSpm7wEyQsuDQ
    private Track[] retrievePlaylist(String playlistUrl) throws IOException, SpotifyWebApiException {
        String id = playlistUrl.substring("https://open.spotify.com/playlist/".length()).substring(0, 22);
        Playlist playlist = spotify.getPlaylist(id).build().execute();
        PlaylistTrack[] playlistTracks = playlist.getTracks().getItems();
        return Arrays.stream(playlistTracks).map(PlaylistTrack::getTrack).toArray(Track[]::new);
    }

    // returns a new instance of the Spotify API
    // clientId: the clientId, provided by Spotify API Console
    // clientSecret: the clientSecret, provided by Spotify API Console
    private SpotifyApi getSpotify(String clientId, String clientSecret) throws IOException, SpotifyWebApiException {
        // build new API
        SpotifyApi api = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        // build auth request
        ClientCredentials credentials = api.clientCredentials().build().execute();
        api.setAccessToken(credentials.getAccessToken());
        lastRefresh = System.currentTimeMillis();

        // return the instance
        return api;
    }

    // returns the instance of SpotifyHelper
    public static SpotifyHelper get() {
        if (instance == null) {
            AssetManager assets = AssetManager.get();
            String id = assets.getProperty("spotify-client-id");
            String secret = assets.getProperty("spotify-client-secret");
            instance = new SpotifyHelper(id, secret);
        }
        return instance;
    }
}
