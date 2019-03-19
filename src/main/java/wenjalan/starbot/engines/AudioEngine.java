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
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import wenjalan.starbot.listeners.MessageListener;

import java.util.LinkedList;
import java.util.Queue;

public class AudioEngine {

    // the Queue of Tracks to play
    protected Queue<AudioTrack> queue;

    // if something's playing
    protected boolean isPlaying;

    // the MessageListener this AudioEngine is being used by
    protected MessageListener messageListener;

    // the AudioPlayerManager
    protected AudioPlayerManager audioPlayerManager;

    // the AudioPlayer
    protected AudioPlayer audioPlayer;

    // the TrackScheduler
    protected TrackScheduler trackScheduler;

    // constructor
    public AudioEngine(MessageListener messageListener) {
        this.messageListener = messageListener;
        init();
    }

    // initialization
    protected void init() {
        // set playing false
        this.isPlaying = false;

        // init queue
        this.queue = new LinkedList<>();

        // setup audio manager and player
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        this.audioPlayer = audioPlayerManager.createPlayer();

        // setup scheduler
        this.trackScheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(trackScheduler);
    }

    // resumes playback
    public void play() {
        // if paused, resume
        if (audioPlayer.isPaused()) {
            audioPlayer.setPaused(false);
        }
    }

    // plays a track
    public void play(String url) {
        queue(url);
        next();
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

    // plays the next track
    protected void next() {
        // play the next track if there is one
        if (!queue.isEmpty()) {
            AudioTrack track = queue.remove();
            audioPlayer.startTrack(track, false);
        }
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
            // start the next track
            if (endReason.mayStartNext) {
                next();
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
                next();
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

    }

}
