package wenjalan.starbot.engine.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

// handles the sending of music
public class MusicHandler implements AudioSendHandler {

    // the AudioManager being used to look up tracks
    private final AudioPlayerManager playerManager;

    // the AudioPlayer being used to play music
    private final AudioPlayer audioPlayer;

    // the LastFrame for sending
    private AudioFrame lastFrame;

    // the Queue of tracks
    private final Queue<AudioTrack> queue;

    // constructor
    // msg: the Message object that invoked the creation of this MusicHandler
    public MusicHandler(Message msg) {
        // create AudioManager
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        // create AudioPlayer
        audioPlayer = playerManager.createPlayer();

        // create event listener
        audioPlayer.addListener(new AudioEventAdapter() {
            @Override
            public void onPlayerPause(AudioPlayer player) {
                super.onPlayerPause(player);
            }

            @Override
            public void onPlayerResume(AudioPlayer player) {
                super.onPlayerResume(player);
            }

            @Override
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                super.onTrackStart(player, track);
            }

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                // if there are more tracks in the queue, start the next
                if (endReason.mayStartNext && !queue.isEmpty()) {
                    player.playTrack(queue.poll());
                }
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                super.onTrackException(player, track, exception);
            }
        });

        // if there was a request to play something, play something
        String[] args = msg.getContentRaw().split("\\s+");
        if (args.length > 1) {
            playTrack(msg);
        }

        // create Queue
        queue = new LinkedList<>();
    }

    // handles the request to play a track
    // msg: the Message object that invoked this request
    public void playTrack(Message msg) {
        // get info
        TextChannel channel = msg.getTextChannel();
        String[] args = msg.getContentRaw().split("\\s+");

        // verify that they actually asked to play something
        if (args.length <= 1) {
            channel.sendMessage("Play what.").queue();
        }

        // get query and load the track
        String query = args[1];
        playerManager.loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                queue.add(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // if it was a Search Result, load only the first track
                if (playlist.isSearchResult()) {
                    AudioTrack firstResult = playlist.getTracks().get(0);
                    trackLoaded(firstResult);
                }
                // otherwise load all tracks
                else {
                    for (AudioTrack track : playlist.getTracks()) {
                        trackLoaded(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("No results for " + query).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Failed to load track " + query).queue();
                channel.sendMessage(exception.getMessage()).queue();
            }
        });
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
