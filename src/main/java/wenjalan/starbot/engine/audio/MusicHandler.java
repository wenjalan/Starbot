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
import wenjalan.starbot.utils.ReactionControllerManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    public MusicHandler() {
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
                updateController();
            }

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                // if there are more tracks in the queue, start the next
                if (endReason.mayStartNext && !queue.isEmpty()) {
                    player.playTrack(queue.poll());
                }
                updateController();
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                super.onTrackException(player, track, exception);
            }
        });

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
        final String query = rawContent.substring("!play ".length());

        // result handler
        AudioLoadResultHandler resultHandler = new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                // if the player is not playing something, start playing this track
                if (audioPlayer.getPlayingTrack() == null) {
                    audioPlayer.playTrack(track);
                }
                // otherwise, queue the track for later playback
                else {
                    queue.add(track);
                }
                updateController();
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
        };

        // verify that they actually asked to play something
        if (args.length <= 1) {
            channel.sendMessage("Play what.").queue();
            return;
        }

        // process query
        // if Spotify link, load all items and queue each
        if (query.startsWith("https://open.spotify.com/")) {
            // get the queries
            SpotifyHelper spotifyHelper = SpotifyHelper.get();
            List<String> queries = spotifyHelper.getSearchQueries(query);
            for (String spotifyQuery : queries) {
                playerManager.loadItem("ytsearch:" + spotifyQuery, resultHandler);
            }
        }
        // is direct link
        else if (query.startsWith("http")) {
            // load the track
            playerManager.loadItem(query, resultHandler);
        }
        // is search
        else {
            // search
            playerManager.loadItem("ytsearch:" + query, resultHandler);
        }

        // delete the message
        msg.delete().queue();
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
                // add the reaction controller
                controllerMessageId = msg.getIdLong();
                final String PAUSE_PLAY = "\u23EF";
                final String SKIP_BUTTON = "\u23ED";
                List<String> buttons = new ArrayList<>();
                buttons.add(PAUSE_PLAY);
                buttons.add(SKIP_BUTTON);

                // add to controller manager
                ReactionControllerManager.addController(msg, buttons, emojiRegex -> {
                    if (emojiRegex.equalsIgnoreCase(PAUSE_PLAY)) {
                        togglePlayback();
                    }
                    else if (emojiRegex.equalsIgnoreCase(SKIP_BUTTON)) {
                        skipTrack();
                    }
                });
            });
        });
    }

    // toggles the pause-play status of the player
    public void togglePlayback() {
        audioPlayer.setPaused(!audioPlayer.isPaused());
    }

    // skips the currently playing track
    public void skipTrack() {
        AudioTrack t = audioPlayer.getPlayingTrack();
        t.setPosition(t.getDuration());
    }

    // updates the controller with track info
    private void updateController() {
        // get JDA from Starbot and the channel
        JDA jda = Starbot.getJda();
        AudioTrack track = audioPlayer.getPlayingTrack();

        // get track info
        String trackTitle = "N/A";
        String trackAuthor = "N/A";
        String queueText = "Nothing";
        if (track != null) {
            AudioTrackInfo info = track.getInfo();
            trackTitle = info.title;
            trackAuthor = info.author;
            queueText = getQueueAsString();
        }
        String finalTrackTitle = trackTitle;
        String finalTrackAuthor = trackAuthor;
        String finalQueueText = queueText;

        // update info
        jda.getTextChannelById(controllerChannelId).retrieveMessageById(controllerMessageId).queue(controller -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setTitle(finalTrackTitle);
            embedBuilder.setDescription(finalTrackAuthor);
            embedBuilder.addField("Queue (" + queue.size() + ")", finalQueueText, false);
            controller.editMessage(embedBuilder.build()).queue();
        });
    }

    // returns a String representing the current queue
    private String getQueueAsString() {
        if (queue.isEmpty()) {
            return "Nothing";
        }
        StringBuilder str = new StringBuilder();
        for (AudioTrack audioTrack : queue) {
            String nextTitle = audioTrack.getInfo().title;
            // if adding this title would put us over th charlimit (1024, stop adding)
            if (nextTitle.length() + str.length() >= 1021) {
                str.append("...\n");
                break;
            }
            str.append(audioTrack.getInfo().title).append("\n");
        }
        return str.substring(0, str.length() - 1);
    }

    // destroys a controller channel for a guild
    public void destroyController(Guild g) {
        // if no controller, return
        if (controllerMessageId == -1) {
            return;
        }

        // delist the controller
        ReactionControllerManager.removeController(controllerMessageId);

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
