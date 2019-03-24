package wenjalan.starbot.engines;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.LinkedList;
import java.util.Queue;

public class AudioEngine {

    // the default volume
    public static final int DEFAULT_VOLUME = 30;

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

    // constructor
    public AudioEngine() {
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
            System.out.println("now playing " + track.getInfo().title + " by " + track.getInfo().author);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            isPlaying = false;

            // sout
            System.out.println("track " + track.getInfo().title + " ended");

            // start the next track
            if (endReason.mayStartNext) {
                sendHandler.next();
            }
        }

    }

    // LoadHandler
    public class ResultHandler implements AudioLoadResultHandler {

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
            // add all the tracks to the queue
            queue.addAll(playlist.getTracks());
            System.out.println("loaded playlist" + playlist.getName());
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
        public void play(String url) {
            queue(url);
            next();
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
        public void queue(String url) {
            audioPlayerManager.loadItem(url, new ResultHandler());
        }

        // skips a track
        public void skip() {
            next();
        }

        // sets volume
        public void setVolume(int volume) {
            audioPlayer.setVolume(volume);
        }

        // returns if we're playing something
        public boolean isPlaying() {
            return isPlaying;
        }

        // returns the info of the track that's playing
        public String playing() {
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
