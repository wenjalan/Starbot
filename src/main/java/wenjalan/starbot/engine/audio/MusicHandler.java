package wenjalan.starbot.engine.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
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
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.engine.AudioEngine;
import wenjalan.starbot.engine.command.SeekCommand;
import wenjalan.starbot.utils.ReactionControllerManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    // whether the player is currently repeating itself
    private boolean isRepeating = false;

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
                // if we're repeating repeat
                if (isRepeating) {
                    // replay the track that ended
                    player.playTrack(track.makeClone());
                }
                // if there are more tracks in the queue, start the next
                else if (endReason.mayStartNext && !queue.isEmpty()) {
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
        if (rawContent.length() <= "!play ".length()) return;
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
    public void createController(Guild g, TextChannel channel) {
        // create the channel and create an embed as a controller
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
            final String STOP = "\u23F9";
            List<String> buttons = new ArrayList<>();
            buttons.add(PAUSE_PLAY);
            buttons.add(SKIP_BUTTON);
            buttons.add(STOP);

            // add to controller manager
            ReactionControllerManager.addController(msg, buttons, emojiRegex -> {
                if (emojiRegex.equalsIgnoreCase(PAUSE_PLAY)) {
                    togglePlayback();
                }
                else if (emojiRegex.equalsIgnoreCase(SKIP_BUTTON)) {
                    skipTrack();
                }
                else if (emojiRegex.equalsIgnoreCase(STOP)) {
                    AudioEngine audio = AudioEngine.getInstance();
                    audio.stopPlayback(g);
                }
            });

            // update the controller
            updateController();
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

    // clears the queue
    public void clearQueue(Message msg) {
        queue.clear();
        updateController();
        msg.delete().queue();
    }

    // updates the controller with track info
    private void updateController() {
        // get JDA from Starbot and the channel
        JDA jda = Starbot.getJda();
        AudioTrack track = audioPlayer.getPlayingTrack();

        // fixme: clean this up
        // get track info
        String trackTitle = "N/A";
        String trackAuthor = "N/A";
        String queueText = "Nothing";
        String uri = null;
        String timestamp = "0:00/0:00";
        if (track != null) {
            AudioTrackInfo info = track.getInfo();
            trackTitle = info.title;
            trackAuthor = info.author;
            uri = info.uri;
            queueText = getQueueAsString();
            timestamp = millisToTimestamp(track.getPosition()) + "/" + millisToTimestamp(track.getDuration());
        }
        String finalTrackTitle = trackTitle;
        String finalTrackAuthor = trackAuthor;
        String finalQueueText = queueText;
        String finalUri = uri;
        String finalTimestamp = timestamp;

        // update info
        jda.getTextChannelById(controllerChannelId).retrieveMessageById(controllerMessageId).queue(controller -> {
            if (controller == null) {
                // there is no controller, return
                return;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setAuthor(finalTrackAuthor);
            embedBuilder.setTitle(isRepeating ? "(Repeat) " + finalTrackTitle : finalTrackTitle, finalUri);
            embedBuilder.setDescription("[" + finalTimestamp + "]");
            if (!queue.isEmpty()) embedBuilder.addField("Queue (" + queue.size() + ")", finalQueueText, false);
            controller.editMessage(embedBuilder.build()).queue();
        });
    }

    // converts a number of milliseconds to a timestamp of the format hh:mm:ss
    private String millisToTimestamp(long millis) {
        // convert to minutes
        long minutes = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS);

        // find leftover seconds
        long leftOverMillis = millis - TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
        long seconds = TimeUnit.SECONDS.convert(leftOverMillis, TimeUnit.MILLISECONDS);

        // return the timestamp
        String timestamp = "" + minutes + ":" + (seconds < 10L ? "0" + seconds : seconds);
        return timestamp;
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

        // delete the message
        JDA jda = g.getJDA();
        TextChannel channel = jda.getTextChannelById(controllerChannelId);
        if (channel != null) {
            channel.retrieveMessageById(controllerMessageId)
                    .onErrorFlatMap(err -> {
                        // do nothing lmao
                        return null;
                    })
                    .queue(msg -> {
                        if (msg != null) {
                            msg.delete().queue();
                        }
                    });
        }

        // set controller message id to -1 to prevent redeletion
        controllerMessageId = -1;
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        // update the controller, mostly for timestamp reasons, if a whole 5 seconds have passed
        // we can't do second-to-second updates because Discord doesn't handle updates that fast
        if (audioPlayer.getPlayingTrack().getPosition() % 2000 == 0) {
            updateController();
        }
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    // sets the volume of the player
    public void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Invalid volume passed to MusicHandler: " + volume);
        }
        audioPlayer.setVolume(volume);
    }

    // seeks to a certain position in the currently playing track
    public void seekTo(long pos) {
        audioPlayer.getPlayingTrack().setPosition(pos);
    }

    // shuffles the currently queued tracks
    public void shuffle() {
        if (queue.isEmpty()) {
            return;
        }
        List<AudioTrack> items = new LinkedList<>(queue);
        Collections.shuffle(items);
        queue.clear();
        queue.addAll(items);
        updateController();
    }

    // returns whether the player is repeating
    public boolean isRepeat() {
        return isRepeating;
    }

    // sets whether the player is repeating
    public void setRepeating(boolean repeating) {
        this.isRepeating = repeating;
        updateController();
    }

    // recreates the controller and sends it to the channel again
    public void recreateController(TextChannel channel) {
        destroyController(channel.getGuild());
        createController(channel.getGuild(), channel);
    }
}
