import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Audio {

    AudioPlayerManager playerManager;

    Queue<AudioTrack> playQueue = new LinkedList<>();

    public String testSongUrl = "https://www.youtube.com/watch?v=4mAiofhaebE";

    public AudioPlayer player;

    public Audio() {
        // get player manager
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        // get player
        player = playerManager.createPlayer();

        // get track scheduler
        TrackScheduler trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
    }

    boolean canInterrupt = false;

    private void playNext() {
        // play item
        AudioTrack track = playQueue.poll();
        player.startTrack(track, false);
    }

    public void play(String query) {
        canInterrupt = true;
        playerManager.loadItem(query, new ResultHandler());
    }

    public void queue(String query) {
        canInterrupt = false;
        playerManager.loadItem(query, new ResultHandler());
    }

    public void skip() {
        canInterrupt = true;
        playNext();
    }

    public class TrackScheduler extends AudioEventAdapter {

        AudioPlayer player;

        TrackScheduler(AudioPlayer player) {
            this.player = player;
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            System.out.println("Playing " + track.getInfo().title);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            System.out.println("Track " + track.getInfo().title + " finished");
            if (endReason.mayStartNext) {
                playNext();
            }
        }

    }

    public class ResultHandler implements AudioLoadResultHandler {
        @Override
        public void trackLoaded(AudioTrack track) {
            playQueue.offer(track);
            System.out.println("Loaded track " + track.getIdentifier());
            if (canInterrupt) {
                playNext();
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            playQueue.addAll(playlist.getTracks());
            System.out.println("Loaded playlist " + playlist.getName());
        }

        @Override
        public void noMatches() {
            System.out.println("Didn't find track");
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            System.err.println(exception.severity + " exception: " + exception.getStackTrace());
        }
    }

    public class AudioPlayerSendHandler implements AudioSendHandler {

        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        public AudioPlayerSendHandler() {
            this(player);
        }

        public AudioPlayerSendHandler(AudioPlayer player) {
            this.audioPlayer = player;
        }

        @Override
        public boolean canProvide() {
            lastFrame = audioPlayer.provide();
            return lastFrame != null;
        }

        @Override
        public byte[] provide20MsAudio() {
            return lastFrame.getData();
        }

        @Override
        public boolean isOpus() {
            return true;
        }

        public void aPlay() {
            skip();
        }

        public void aPlay(String query) {
            play(query);
        }

        public void aQueue(String query) {
            queue(query);
        }

        public String aGetQueueString() {
            return getQueueString();
        }

    }

    public String getQueueString() {
        String s = "";
        List<AudioTrack> tracks = new LinkedList<>(playQueue);
        for (AudioTrack t : tracks) {
            s += t.getInfo().title + "\n";
        }
        return s;
    }

}
