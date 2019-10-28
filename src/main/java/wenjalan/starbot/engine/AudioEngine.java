package wenjalan.starbot.engine;

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
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.ByteBuffer;
import java.util.*;

// handles all the audio playback capability of Starbot, owns the RadioEngine and MusicEngine
public class AudioEngine {

    // AudioEngine Commands
    public enum AudioCommand {

        // plays a track given a query
        play {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // if we're not already connected, connect
                Player p = null;
                if (!event.getGuild().getAudioManager().isConnected()) {
                    // find the VoiceChannel
                    VoiceChannel channel = event.getMember().getVoiceState().getChannel();

                    // if null, complain
                    if (channel == null) {
                        event.getChannel().sendMessage("fucking where").queue();
                        return;
                    }

                    // connect
                    p = connect(channel);

                    // play the query
                    AudioCommand.playQuery(p, event);
                }
                else {
                    // get the player
                    p = AudioCommand.getSendHandler(event).player();

                    // play the query
                    AudioCommand.playQuery(p, event);
                }
            }
        },

        // sends the currently playing track
        playing {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                Player.SendHandler handler = AudioCommand.getSendHandler(event);

                // if the handler's not null
                if (handler != null) {
                    // get the currently playing track
                    AudioTrack playing = handler.player().audioPlayer.getPlayingTrack();

                    // if it's not null, print the info
                    if (playing != null) {
                        handler.player().sendInfo(playing);
                    }
                    else {
                        event.getChannel().sendMessage("nothing right now").queue();
                    }
                }
            }
        },

        // pauses the player
        pause {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the SendHandler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);
                // if we're playing something, pause it
                if (handler != null && !handler.player().audioPlayer.isPaused()) {
                    handler.player().audioPlayer.setPaused(true);
                }
            }
        },

        // resumes the player
        resume {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the SendHandler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);
                // if we're paused, unpause
                if (handler != null && handler.player().audioPlayer.isPaused()) {
                    handler.player().audioPlayer.setPaused(false);
                }
            }
        },

        // skips the current track
        skip {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get handler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);

                // skip the currently playing track if there is one
                if (handler != null) {
                    handler.player().skip();
                }
            }
        },

        // sets the volume
        volume {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the SendHandler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);
                // if the player exists, set the volume
                if (handler != null) {
                    try {
                        // find what volume they want
                        int newVol = Integer.parseInt(CommandEngine.parseArgs(event.getMessage())[1]);
                        if (newVol > 100 || newVol < 0) {
                            throw new IllegalArgumentException("volume is from 0 to 100");
                        }

                        // actually set it
                        handler.player().audioPlayer.setVolume(newVol);
                    } catch (IllegalArgumentException e) {
                        // complain
                        event.getChannel().sendMessage(e.getMessage()).queue();
                    }
                }
            }
        },

        // sends the currently queued tracks
        queue {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get handler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);

                // if not null, print the queue
                if (handler != null) {
                    // send the queue
                    event.getChannel().sendMessage(handler.player().getQueueAsString()).queue();
                }
            }
        },

        // removes all tracks from the queue
        clearqueue {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // handler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);

                // if not null, remove all queued tracks
                if (handler != null) {
                    handler.player().scheduler.queue().clear();
                }
            }
        },

        // shuffles the queue
        shuffle {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // handler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);

                // if it exists, get the queue and shuffle it
                Queue<AudioTrack> queue = shuffleQueue(handler.player().scheduler.queue());
                handler.player().scheduler.queue = queue;
            }
        },

        // seeks to a certain position
        seek {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the handler
                Player.SendHandler handler = AudioCommand.getSendHandler(event);
                // if the player exists, seek to their desired timestamp
                if (handler != null && handler.player().audioPlayer.getPlayingTrack() != null) {
                    // get their desired timestamp, in milliseconds
                    // format: mm:ss
                    String[] query = CommandEngine.parseArgs(event.getMessage());

                    // if a timestamp wasn't specified, complain
                    if (query.length < 2) {
                        event.getChannel().sendMessage("to what, idiot").queue();
                        return;
                    }

                    // split the timestamp into minutes:seconds
                    // todo: make this more elegant
                    String[] timestamp;
                    try {
                        if (!query[1].contains(":")) {
                            throw new IllegalArgumentException();
                        }
                        timestamp = query[1].split(":");
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                        event.getChannel().sendMessage("[min]:[sec]").queue();
                        return;
                    }

                    // verify that both are positive numbers
                    long minutes;
                    long seconds;
                    try {
                        minutes = Integer.parseInt(timestamp[0]);
                        seconds = Integer.parseInt(timestamp[1]);
                        if (minutes < 0 || seconds < 0) {
                            throw new IllegalArgumentException("gimme better numbers");
                        }
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage("what").queue();
                        return;
                    }

                    // calculate the timestamp in milliseconds
                    long milliseconds = (minutes * 60 * 1000) + (seconds * 1000);

                    // seek to that time
                    handler.seek(milliseconds);
                }
            }
        },

        // disconnects the bot from a voice call
        getout {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the AudioManager
                AudioManager manager = event.getGuild().getAudioManager();
                // if we're connected
                if (manager.isConnected()) {
                    // clear the queue
                    AudioCommand.clearqueue.execute(event);

                    // disconnect
                    manager.closeAudioConnection();
                }
            }
        };

        public abstract void execute(GuildMessageReceivedEvent event);

        // gets the SendHandler of a guild
        private static Player.SendHandler getSendHandler(GuildMessageReceivedEvent event) {
            return (Player.SendHandler) event.getGuild().getAudioManager().getSendingHandler();
        }

        // plays a query on a given Player
        private static void playQuery(Player p, GuildMessageReceivedEvent event) {
            String rawQuery = event.getMessage().getContentRaw();
            // if there was none
            if (rawQuery.split(" ").length < 2) {
                // complain
                event.getChannel().sendMessage("play what").queue();
                return;
            }
            // remove the !play command from the query
            rawQuery = rawQuery.substring(6);
            p.load(rawQuery, event.getChannel());
        }

        // shuffles a Queue of AudioTracks
        private static Queue<AudioTrack> shuffleQueue(Queue<AudioTrack> queue) {
            ArrayList<AudioTrack> trackList = new ArrayList<>(queue);
            Queue<AudioTrack> shuffledQueue = new LinkedList<>();
            Random r = new Random();
            while (trackList.size() > 0) {
                shuffledQueue.add(trackList.remove(r.nextInt(trackList.size())));
            }
            return shuffledQueue;
        }

    }

    // Player represents the LavaPlayer instance, one per guild
    protected static class Player {

        // the default volume
        public static final int DEFAULT_VOLUME = 50;

        // the default timeout, in seconds
        public static final int DEFAULT_PLAYBACK_TIMEOUT = 1000 * 15 * 60; // 15 minutes

        // the AudioPlayerManager
        private AudioPlayerManager audioPlayerManager;

        // the AudioPlayer
        final private AudioPlayer audioPlayer;

        // the Scheduler
        final private Scheduler scheduler;

        // the Guild this Player is assigned to
        private final Guild guild;

        // the timeout thread
        private Thread timeoutThread;

        // the SendHandler
        private SendHandler sendHandler;

        // the last TextChannel we received a command from
        private TextChannel lastFeedbackChannel = null;

        // returns the SendHandler for JDA
        private AudioSendHandler sendHandler() {
            return this.sendHandler;
        }

        // constructor
        private Player(Guild guild) {
            this.guild = guild;

            // create a manager
            this.audioPlayerManager = new DefaultAudioPlayerManager();
            this.audioPlayerManager.setFrameBufferDuration(500); // 500 ms buffer
            AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);

            // create a player
            this.audioPlayer = this.audioPlayerManager.createPlayer();
            this.audioPlayer.setVolume(DEFAULT_VOLUME);

            // create a Scheduler
            this.scheduler = new Scheduler();
            audioPlayer.addListener(this.scheduler);

            // create a SendHandler
            this.sendHandler = new SendHandler(this);
        }

        // the TrackScheduler, handles the adding and removing of tracks from the queue
        protected class Scheduler extends AudioEventAdapter {

            // the queue of tracks
            private Queue<AudioTrack> queue;

            // constructor
            private Scheduler() {
                this.queue = new LinkedList<>();
            }

            // adds an AudioTrack to the queue
            private void queue(AudioTrack track) {
                // if nothing else is playing, start playing
                if (queue.isEmpty() && audioPlayer.getPlayingTrack() == null) {
                    audioPlayer.startTrack(track, false);
                }
                // otherwise add to the queue
                else {
                    // queue the track
                    queue.add(track);

                    // print the queue to the chat
                    lastFeedbackChannel.sendMessage(getQueueAsString()).queue();
                }
            }

            @Override
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                // interrupt the timeout if it exists
                if (timeoutThread != null) {
                    timeoutThread.interrupt();
                }

                // print some info
                sendInfo(track);
            }

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                // if we can start the next track and there is more in the queue, play the next track
                if (endReason.mayStartNext && !queue.isEmpty()) {
                    AudioTrack nextTrack = queue.remove();
                    player.startTrack(nextTrack, false);
                }
                else {
                    // start counting down to timeout
                    timeoutThread = new Thread((() -> {
                        try {
                            // wait for the timeout
                            Thread.sleep(DEFAULT_PLAYBACK_TIMEOUT);

                            // if nothing plays after the timeout and nothing's queued, quit
                            if (player.getPlayingTrack() == null && queue.isEmpty()) {
                                // shut down the manager
                                audioPlayerManager.shutdown();
                                // close the connection
                                guild.getAudioManager().closeAudioConnection();
                            }
                        } catch (InterruptedException e) {} // do nothing I guess
                    }));
                    timeoutThread.start();
                }
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                lastFeedbackChannel.sendMessage("uhhhhhhhh there was a problem").queue();
            }

            // returns the Queue of tracks
            public Queue<AudioTrack> queue() {
                return this.queue;
            }

        }

        // the SendHandler, for JDA
        protected class SendHandler implements AudioSendHandler {

            // the Player this SendHandler is sending for
            private Player player;

            // the last frame we sent
            private AudioFrame lastFrame;

            // constructor
            private SendHandler(Player p) {
                this.player = p;
            }

            @Override
            public boolean canProvide() {
                lastFrame = player.audioPlayer.provide();
                return lastFrame != null;
            }

            @Override
            public ByteBuffer provide20MsAudio() {
                return ByteBuffer.wrap(lastFrame.getData());
            }

            @Override
            public boolean isOpus() {
                return true;
            }

            // seeks to a position in a Track if it support sit
            protected void seek(long milliseconds) {
                // get the playing track
                AudioTrack t = player.audioPlayer.getPlayingTrack();

                // if it exists and if it's seekable, seek
                if (t != null && t.isSeekable()) {
                    t.setPosition(milliseconds);
                }
            }

            // returns the AudioPlayer this SendHandler is sending for
            protected Player player() {
                return this.player;
            }

        }

        // loads a track to play given a String query and a channel to send the results to
        protected void load(String query, final TextChannel feedbackChannel) {
            // set the feedback channel so we have somewhere to complain to
            this.lastFeedbackChannel = feedbackChannel;

            // if it's not a url, append the search tag
            final boolean isSearch;
            if (!query.startsWith("https")) {
                query = "ytsearch:" + query;
                isSearch = true;
            }
            else {
                isSearch = false;
            }

            // load the query
            this.audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    // queue the track
                    scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    // if it was a search, only add the first track
                    if (isSearch) {
                        scheduler.queue(playlist.getTracks().get(0));
                    }
                    // otherwise, add all of the tracks
                    else {
                        // queue all the tracks from the playlist
                        for (AudioTrack track : playlist.getTracks()) {
                            scheduler.queue(track);
                        }
                    }
                }

                @Override
                public void noMatches() {
                    // complain
                    feedbackChannel.sendMessage("nothing came up").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    // complain
                    feedbackChannel.sendMessage("what").queue();
                }
            });
        }

        // skips the currently playing track
        public void skip() {
            // stop the current track
            audioPlayer.stopTrack();
            // play the next one if there is one
            if (!this.scheduler.queue().isEmpty()) {
                AudioTrack t = scheduler.queue.remove();
                audioPlayer.startTrack(t, false);
            }
        }

        // sends information about a track into the feedbackChannel
        protected void sendInfo(AudioTrack track) {
            // send some info about the track
            AudioTrackInfo info = track.getInfo();
            long minutes = (info.length / 1000) / 60;
            long seconds = (info.length / 1000) % 60;

            // if seconds are a single digit number, add a 0
            String leadingZero = "";
            if (seconds < 10) {
                leadingZero = "0";
            }

            lastFeedbackChannel.sendMessage(
                    "> Now Playing: " + info.title + " by " + info.author + " (" + minutes + ":" + leadingZero + seconds + ")"
            ).queue();
        }

        // gets the Queue as a String
        protected String getQueueAsString() {
            if (scheduler.queue().isEmpty()) {
                return "nothing's queued";
            }
            else {
                List<AudioTrack> tracks = new LinkedList<>(scheduler.queue);
                StringBuilder builder = new StringBuilder();
                builder.append(tracks.size() + " tracks queued:\n");
                int pos = 1;
                for (AudioTrack t : tracks) {
                    AudioTrackInfo info = t.getInfo();
                    builder.append("> " + pos + ": " + info.title + " by " + info.author + "\n");
                    pos++;
                }
                return builder.toString();
            }
        }

    }

    // connects Starbot to a VoiceChannel, ready to play music
    protected static Player connect(VoiceChannel channel) {
        AudioManager guildAudioManager = channel.getGuild().getAudioManager();
        Player player = new Player(guildAudioManager.getGuild());
        guildAudioManager.setSendingHandler(player.sendHandler());
        guildAudioManager.openAudioConnection(channel);
        return player;
    }

    // returns a Player given a specific Guild, or null if one wasn't created
    public static Player getPlayer(Guild g) {
        Player.SendHandler handler = (Player.SendHandler) g.getAudioManager().getSendingHandler();
        return handler == null ? null : handler.player();
    }

}
