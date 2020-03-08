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

        // starts a radio
        radio {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                String radioName = event.getMessage().getContentRaw().split("\\s+")[1].toLowerCase();
                // if it was a valid radio
                if (DataEngine.getRadioNames().contains(radioName)) {
                    // find the radio they wanted to play
                    String radioUrl = DataEngine.getRadioUrl(radioName);

                    // play it
                    Player p = null;
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
                        // if the author isn't ocnnected, complain
                        if (vc == null) {
                            event.getChannel().sendMessage("fucking where").queue();
                        }
                        p = connect(vc);
                    }
                    else {
                        p = AudioCommand.getSendHandler(event).player();
                    }
                    AudioCommand.playQuery(p, event.getChannel(), radioUrl);
                }
                else {
                    // complain
                    event.getChannel().sendMessage("didn't find that radio").queue();
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

        // sets whether the current track should be looped infinitely
        repeat {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get SendHandler
                Player p = getPlayer(event.getGuild());

                // if the player exists
                if (p != null) {
                    // announce
                    event.getChannel().sendMessage(p.isLooping() ? "no longer on repeat" : "now on repeat").queue();
                    // toggle repeat
                    p.setLooping(!p.isLooping());
                }
            }
        },

        // toggles the automatic playback of videos based on related videos
        autoplay {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get handler
                Player p = getPlayer(event.getGuild());

                // if it exists
                if (p != null) {
                    // announce
                    event.getChannel().sendMessage(p.isAutoPlaying() ? "autoplay off" : "autoplay on").queue();
                    // toggle autoplay
                    p.setAutoPlay(!p.isAutoPlaying());
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

        // lists the radios
        radios {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // check me
                StringBuilder sb = new StringBuilder();
                sb.append("Radios:\n");
                for (String s : DataEngine.getRadioNames()) {
                    sb.append("> " + s + "\n");
                }
                event.getChannel().sendMessage(sb.toString()).queue();
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

        // plays a query on a given Player and an Event
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
            playQuery(p, event.getChannel(), rawQuery);
        }

        // plays a query given a Player and a String
        private static void playQuery(Player p, TextChannel feedbackChannel, String query) {
            p.load(query, feedbackChannel);
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
    public static class Player {

        // the default volume
        public static final int DEFAULT_VOLUME = 50;

        // the default timeout, in seconds
        public static final int DEFAULT_PLAYBACK_TIMEOUT = 1000 * (60 * 10); // 10 minutes

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

        // whether or not we're looping the current track
        private boolean isLooping = false;

        // whether or not we're auto playing tracks
        private boolean isAutoPlay = false;

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

                    // announce that we added the track to the queue
                    lastFeedbackChannel.sendMessage("> Queued " + track.getInfo().title ).queue();
                }
            }

            // adds a series of AudioTracks to the queue
            private void queue(List<AudioTrack> tracks) {
                int size = tracks.size();
                // if nothing is playing, play the first track
                if (queue.isEmpty() && audioPlayer.getPlayingTrack() == null) {
                    audioPlayer.startTrack(tracks.remove(0), false);
                }
                // enqueue the rest of the songs
                queue.addAll(tracks);
                // announce that we added the songs
                lastFeedbackChannel.sendMessage("> Queued " + size + " tracks to the queue").queue();
            }

            // adds an AudioTrack retrieved from a Spotify playlist
            private void spotifyQueue(AudioTrack t, int finalSize) {
                // if nothing is playing, play this song
                if (queue.isEmpty() && audioPlayer.getPlayingTrack() == null) {
                    audioPlayer.startTrack(t, false);
                }
                // enqueue the track otherwise
                queue.add(t);
                // if this was the last track we needed to add, announce that we're done
                // minus one in case we played the first song
                if (queue.size() == finalSize - 1) {
                    lastFeedbackChannel.sendMessage("> Queued " + finalSize + " tracks to the queue").queue();
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
                // if we're supposed to be looping the track, start playing it again
                if (endReason.mayStartNext && isLooping) {
                    player.startTrack(track.makeClone(), false);
                }
                // if we can start the next track and there is more in the queue, play the next track
                else if (endReason.mayStartNext && !queue.isEmpty()) {
                    AudioTrack nextTrack = queue.remove();
                    player.startTrack(nextTrack, false);
                }
                // if auto play is turned on, play a recommended track
                else if (endReason.mayStartNext && isAutoPlay) {
                    String id = YouTubeEngine.getRecommendation(track.getInfo().uri);
                    String recommendation = "https://youtube.com/watch?v=" + id;
                    forcePlay(recommendation, lastFeedbackChannel);
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
        public class SendHandler implements AudioSendHandler {

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

        // plays a track immediately
        public void forcePlay(String query, final TextChannel feedbackChannel) {
            // change the last feedback channel
            lastFeedbackChannel = feedbackChannel;
            // generate the query
            if (!query.startsWith("http")) {
                query = "ytsearch:" + query;
            }
            // load and play it
            this.audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    audioPlayer.startTrack(track, false);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    audioPlayer.startTrack(playlist.getTracks().get(0), false);
                }

                @Override
                public void noMatches() {
                    lastFeedbackChannel.sendMessage("nothing came up").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    lastFeedbackChannel.sendMessage("what").queue();
                }
            });
        }

        // loads a track to play given a String query and a channel to send the results to
        public void load(String query, final TextChannel feedbackChannel) {
            // set the feedback channel so we have somewhere to complain to
            this.lastFeedbackChannel = feedbackChannel;

            // the size of the playlist to use if it's a spotify playlist
            final int playlistSize;

            // if it's not a url, append the search tag
            final boolean isSearch;
            final boolean isPlaylist;
            List<String> queries = null;
            final int queriesSize;
            if (!query.startsWith("https")) {
                query = "ytsearch:" + query;
                isSearch = true;
                isPlaylist = false;
                queriesSize = -1;
            }
            // spotify playlist
            else if (query.startsWith("https://open.spotify.com/playlist/")) {
                queries = SpotifyEngine.getNamesOfTracks(query);
                if (queries == null) {
                    feedbackChannel.sendMessage("there was a problem, go tell alan").queue();
                    return;
                }
                queriesSize = queries.size();
                isSearch = true;
                isPlaylist = true;
            }
            else {
                queriesSize = -1;
                isSearch = false;
                isPlaylist = false;
            }

            // create an AudioResultHandler
            AudioLoadResultHandler resultHandler = new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    // queue the track
                    scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    // if it was a search, only add the first track
                    if (isSearch) {
                        AudioTrack t = playlist.getTracks().get(0);
                        if (isPlaylist) {
                            scheduler.spotifyQueue(t, queriesSize);
                        }
                        else {
                            scheduler.queue(t);
                        }
                    }
                    // otherwise, add all of the tracks
                    else {
                        List<AudioTrack> tracks = playlist.getTracks();
                        scheduler.queue(tracks);
                    }
                }

                @Override
                public void noMatches() {
                    // complain
                    lastFeedbackChannel.sendMessage("nothing came up").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    // complain
                    lastFeedbackChannel.sendMessage("what").queue();
                    System.err.println("error loading audio track:");
                    exception.printStackTrace();
                }
            };

            // if this was a spotify playlist
            if (!isPlaylist) {
                this.audioPlayerManager.loadItem(query, resultHandler);
            }
            else {
                for (String q : queries) {
                    this.audioPlayerManager.loadItem("ytsearch:" + q, resultHandler);
                }
            }
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

        // sets whether we should auto play tracks
        public void setAutoPlay(boolean autoPlay) {
            this.isAutoPlay = autoPlay;
            // if auto play is turned on while looping is on, turn off looping
            if (autoPlay && isLooping) {
                isLooping = false;
            }
        }

        // returns whether we're autoplaying
        public boolean isAutoPlaying() {
            return this.isAutoPlay;
        }

        // sets whether we should loop the current track
        public void setLooping(boolean loop) {
            this.isLooping = loop;
            // if repeat mode is turned on while auto play is on, turn off auto play
            if (loop && isAutoPlay) {
                isAutoPlay = false;
            }
        }

        // gets whether we're looping
        public boolean isLooping() {
            return this.isLooping;
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
                    // if we're about to hit the character limit, truncate the queue
                    if (builder.length() > 1900) {
                        builder.append("> ... plus " + (tracks.size() - pos) + " more");
                        break;
                    }
                    AudioTrackInfo info = t.getInfo();
                    builder.append("> " + pos + ": " + info.title + " by " + info.author + "\n");
                    pos++;
                }
                return builder.toString();
            }
        }

    }

    // connects Starbot to a VoiceChannel, ready to play music
    public static Player connect(VoiceChannel channel) {
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
