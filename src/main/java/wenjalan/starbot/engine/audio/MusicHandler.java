package wenjalan.starbot.engine.audio;

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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import wenjalan.starbot.Starbot;

import javax.annotation.Nullable;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

// handles the sending of music
public class MusicHandler implements AudioSendHandler {

    // the default volume of an AudioPlayer, out of 100
    public static final int AUDIO_PLAYER_DEFAULT_VOLUME = 30;

    // the AudioManager being used to look up tracks
    private final AudioPlayerManager playerManager;

    // the AudioPlayer being used to play music
    private final AudioPlayer audioPlayer;

    // the LastFrame for sending
    private AudioFrame lastFrame;

    // the id of the channel which the controller is residing
    private long controllerChannelId = -1;

    // the id of the message which the controller is residing
    private long controllerMessageId = -1;

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
        audioPlayer.setVolume(AUDIO_PLAYER_DEFAULT_VOLUME);

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
                updateController(track);
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
        String rawContent = msg.getContentRaw();
        String[] args = rawContent.split("\\s+");

        // verify that they actually asked to play something
        if (args.length <= 1) {
            channel.sendMessage("Play what.").queue();
            return;
        }

        // process query
        String query = rawContent.substring("!play ".length());
        if (!query.startsWith("http")) {
            query = "ytsearch:" + query;
        }

        // load the track
        // todo: find out why wake-up-and-play commands both play and execute the playlistLoaded twice
        String finalQuery = query;
        playerManager.loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                // if the player is playing something, add it to the queue
                if (audioPlayer.getPlayingTrack() != null) {
                    queue.add(track);
                }
                // otherwise, play the track immediately
                else {
                    audioPlayer.playTrack(track);
                }
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
                channel.sendMessage("No results for " + finalQuery).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Failed to load track " + finalQuery).queue();
                channel.sendMessage(exception.getMessage()).queue();
            }
        });
    }

    // creates a controller channel for a guild
    public void createController(Guild g) {
        // create the channel and create an embed as a controller
        g.createTextChannel("music").queue(channel -> {
            controllerChannelId = channel.getIdLong();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setTitle("N/A");
            embed.setDescription("N/A");
            channel.sendMessage(embed.build()).queue(msg -> {
                controllerMessageId = msg.getIdLong();
                // todo: add reaction buttons here
            });
        });
    }

    // updates the controller with track info
    private void updateController(AudioTrack track) {
        // get JDA from Starbot and the channel
        JDA jda = Starbot.getJda();
        AudioTrackInfo info = track.getInfo();
        String trackTitle = info.title;
        String trackAuthor = info.author;
        jda.getTextChannelById(controllerChannelId).retrieveMessageById(controllerMessageId).queue(controller -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setTitle(trackTitle);
            embedBuilder.setDescription(trackAuthor);
            controller.editMessage(embedBuilder.build()).queue();
        });
    }

    // destroys a controller channel for a guild
    public void destoryController(Guild g) {
        // if no controller, return
        if (controllerMessageId == -1) {
            return;
        }
        // find the channel
        JDA jda = g.getJDA();
        TextChannel controllerChannel = jda.getTextChannelById(controllerChannelId);
        if (controllerChannel != null) {
            controllerChannel.delete().queue();
        }
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
