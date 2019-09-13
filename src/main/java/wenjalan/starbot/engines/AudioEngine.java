package wenjalan.starbot.engines;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.LinkedList;
import java.util.Queue;

public class AudioEngine {

    // the default volume
    public static final int DEFAULT_VOLUME = 50;

    // the default timeout, in seconds
    public static final int DEFAULT_TIMEOUT = 15 * 60;

    // the Queue of Tracks to play
    protected Queue<AudioTrack> queue;

    // if something's playing
    protected boolean isPlaying;

    // the AudioPlayerManager
    protected AudioPlayerManager audioPlayerManager;

    // the AudioPlayer
    protected AudioPlayer audioPlayer;

    // the TrackScheduler
    protected TrackScheduler trackScheduler;

    // the SendHandler
    protected SendHandler sendHandler;

    // the Guild this instance of AudioManager is playing in
    protected final Guild guild;

    // the timeout thread
    protected Thread timeout;

    // constructor
    public AudioEngine(Guild g) {
        this.guild = g;
        init();
    }

    // initialization
    protected void init() {
        // set playing false
        this.isPlaying = false;

        // init queue
        this.queue = new LinkedList<>();

        // setup audio manager and player
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        this.audioPlayer = audioPlayerManager.createPlayer();

        // setup scheduler
        this.trackScheduler = new TrackScheduler(audioPlayer);
        this.audioPlayer.addListener(trackScheduler);

        // setup send handler
        this.sendHandler = new SendHandler(audioPlayer);

        // set default volume
        this.sendHandler.setVolume(DEFAULT_VOLUME);
    }

    // accessors
    public SendHandler sendHandler() {
        return this.sendHandler;
    }

    // TrackScheduler
    protected class TrackScheduler extends AudioEventAdapter {

        // the AudioPlayer this TrackScheduler schedules
        protected AudioPlayer audioPlayer;

        // constructor
        public TrackScheduler(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            System.out.println("now playing " + track.getInfo().title + " by " + track.getInfo().author + " from " + track.getInfo().uri);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            isPlaying = false;

            // sout
            System.out.println("track " + track.getInfo().title + " ended: " + endReason.name());

            // start the next track
            if (endReason.mayStartNext) {
                sendHandler.next();
            }

            // if there are no more songs in the queue, start counting down
            if (queue.isEmpty()) {
                // sout
                // System.out.println("queue is empty, starting countdown...");
                // wait literally 5 seconds
                timeout = new Thread((() -> {
                    try {
                        // wait DEFAULT_TIMEOUT seconds
                        Thread.sleep(DEFAULT_TIMEOUT * 1000);
                        // check if anything was added or is playing
                        if (!isPlaying && queue.isEmpty()) {
                            // quit the voice channel
                            guild.getAudioManager().closeAudioConnection();
                            // sout
                            System.out.println("audio playback timed out in " + guild.getName());
                        }
                    } catch (InterruptedException e) {
                        // do nothing I guess
                        // System.err.println("timeout interrupted");
                        // e.printStackTrace();
                    }
                }));
                timeout.start();
            }
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            System.err.println("threw an exception while playing a track:");
            exception.printStackTrace();
        }

    }

    // LoadHandler
    public class ResultHandler implements AudioLoadResultHandler {

        // whether we were searching or not
        final boolean isSearching;

        protected ResultHandler(boolean isSearching) {
            this.isSearching = isSearching;
        }

        protected ResultHandler() {
            this.isSearching = false;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            // add the track to the queue
            queue.add(track);

            // play it
            if (!isPlaying) {
                sendHandler.next();
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            // if we weren't searching, add all the tracks to the queue
            if (!isSearching) {
                queue.addAll(playlist.getTracks());
                System.out.println("loaded playlist" + playlist.getName());
            }
            else {
                // otherwise, just add the first result
                AudioTrack firstResult = playlist.getTracks().get(0);
                System.out.println("found track " + firstResult.getInfo().title + " by " + firstResult.getInfo().author + "on YouTube");
                queue.add(firstResult);
            }

            // play
            if (!isPlaying) {
                sendHandler.next();
            }
        }

        @Override
        public void noMatches() {

        }

        @Override
        public void loadFailed(FriendlyException exception) {
            System.err.println(exception);
        }

    }

    // SendHandler
    public class SendHandler implements AudioSendHandler {

        // the AudioPlayer this SendHandler sends to
        protected final AudioPlayer audioPlayer;

        // the last frame sent
        protected AudioFrame lastFrame;

        // constructor
        public SendHandler(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean canProvide() {
            this.lastFrame = audioPlayer.provide();
            return this.lastFrame != null;
        }

        @Override
        public byte[] provide20MsAudio() {
            return lastFrame.getData();
        }

        @Override
        public boolean isOpus() {
            return true;
        }

        // plays a track
        public void play(String query) {
            stopTimeout();
            clearQueue();
            skip();
            queue(query);
        }

        // pauses a track
        public void pause() {
            isPlaying = false;
            audioPlayer.setPaused(true);
        }

        // resumes a track
        public void resume() {
            isPlaying = true;
            audioPlayer.setPaused(false);
        }

        // clears the queue
        public void clearQueue() {
            queue.clear();
        }

        // queues a track given a url
        public void queue(String query) {
            stopTimeout();
            // if it's a URL, just play it raw
            if (query.startsWith("https")) {
                audioPlayerManager.loadItem(query, new ResultHandler());
            }
            // otherwise, search for the song on youtube
            else {
                search(query);
            }
        }

        // searches for a track given a query
        protected void search(String query) {
            audioPlayerManager.loadItem("ytsearch:" + query, new ResultHandler(true));
        }

        // skips a track
        public void skip() {
            next();
        }

        // sets volume
        public void setVolume(int volume) {
            audioPlayer.setVolume(volume);
        }

        // seeks to a certain part of the track, in milliseconds
        public void seekTo(int time) {
            // get the track
            AudioTrack track = audioPlayer.getPlayingTrack();

            // if it supports it, seek to the time
            if (track.isSeekable()) {
                track.setPosition(time);
            }
        }

        // returns if we're playing something
        public boolean isPlaying() {
            return isPlaying;
        }

        // returns the info of the track that's playing
        public String playing() {
            stopTimeout();
            AudioTrack track = audioPlayer.getPlayingTrack();
            if (track == null) {
                return "nothing.";
            }
            else {
                AudioTrackInfo info = track.getInfo();
                return info.title + " by " + info.author;
            }
        }

        // plays the next track
        protected void next() {
            // stop playing this track
            audioPlayer.stopTrack();
            // play the next track if there is one
            if (!queue.isEmpty()) {
                AudioTrack track = queue.remove();
                audioPlayer.startTrack(track, false);
                isPlaying = true;
            }
        }

        // stops the timeout
        public void stopTimeout() {
            if (timeout != null) {
                timeout.interrupt();
            }
        }

        // gets the Queue as a String
        public String getQueueToString() {
            if (queue.isEmpty()) return "nothing's queued.";
            else {
                LinkedList<AudioTrack> tracks = new LinkedList<>(queue);
                String ret = "";
                for (int i = 0; i < tracks.size() && i < 20; i++) {
                    AudioTrackInfo info = tracks.get(i).getInfo();
                    ret += (i + 1) + ": " + info.title + " by " + info.author + "\n";
                }
                if (tracks.size() > 20) ret += "+ " + (tracks.size() - 20) + " more";
                return ret;
            }
        }

    }

}
