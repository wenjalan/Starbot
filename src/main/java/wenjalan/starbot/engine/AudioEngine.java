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
import java.util.LinkedList;
import java.util.Queue;

// handles all the audio playback capability of Starbot, owns the RadioEngine and MusicEngine
public class AudioEngine {

    // AudioEngine Commands
    public enum AudioCommand {

        // plays a track given a query
        play {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // if we're not already connected, connect
                if (!event.getGuild().getAudioManager().isConnected()) {
                    // find the VoiceChannel
                    VoiceChannel channel = event.getMember().getVoiceState().getChannel();

                    // if null, complain
                    if (channel == null) {
                        event.getChannel().sendMessage("fucking where").queue();
                        return;
                    }

                    // connect
                    Player p = connect(channel);

                    // play the query
                    String[] query = CommandEngine.parseArgs(event.getMessage());
                    p.load(query[1], event.getChannel());
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
                    // disconnect
                    manager.closeAudioConnection();
                }
            }
        };

        public abstract void execute(GuildMessageReceivedEvent event);

    }

    // Player represents the LavaPlayer instance, one per guild
    protected static class Player {

        // the default volume
        public static final int DEFAULT_VOLUME = 50;

        // the default timeout, in seconds
        public static final int DEFAULT_PLAYBACK_TIMEOUT = 15 * 60; // 15 minutes

        // the AudioPlayerManager
        protected AudioPlayerManager audioPlayerManager;

        // the AudioPlayer
        final protected AudioPlayer audioPlayer;

        // the Scheduler
        final protected Scheduler scheduler;

        // the Guild this Player is assigned to
        protected final Guild guild;

        // the timeout thread
        protected Thread timeoutThread;

        // the SendHandler
        protected SendHandler sendHandler;

        // the last TextChannel we received a command from
        protected TextChannel lastFeedbackChannel = null;

        // returns the SendHandler for JDA
        protected AudioSendHandler sendHandler() {
            return this.sendHandler;
        }

        // constructor
        protected Player(Guild guild) {
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

            // create a SendHandler
            this.sendHandler = new SendHandler(this.audioPlayer);
        }

        // the TrackScheduler, handles the adding and removing of tracks from the queue
        protected class Scheduler extends AudioEventAdapter {

            // the queue of tracks
            protected Queue<AudioTrack> queue;

            // constructor
            protected Scheduler() {
                this.queue = new LinkedList<>();
            }

            // adds an AudioTrack to the queue
            protected void queue(AudioTrack track) {
                // if nothing else is playing, start playing
                if (queue.isEmpty() && audioPlayer.getPlayingTrack() == null) {
                    audioPlayer.playTrack(track);
                    sendInfo(track);
                }
                // otherwise add to the queuequeue.add(track);
                else {
                    queue.add(track);
                }
            }

            @Override
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                // interrupt the timeout
                timeoutThread.interrupt();
            }

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                // if we can start the next track and there is more in the queue, play the next track
                if (endReason.mayStartNext && !queue.isEmpty()) {
                    AudioTrack nextTrack = queue.remove();
                    player.playTrack(nextTrack);
                    sendInfo(nextTrack);
                }

                // start counting down to timeout
                timeoutThread = new Thread((() -> {
                    try {
                        // wait for the timeout
                        Thread.sleep(DEFAULT_PLAYBACK_TIMEOUT);

                        // if nothing plays after the timeout and nothing's queued, quit
                        if (player.isPaused() && queue.isEmpty()) {
                            // shut down the manager
                            audioPlayerManager.shutdown();
                            // close the connection
                            guild.getAudioManager().closeAudioConnection();
                        }
                    } catch (InterruptedException e) {} // do nothing I guess
                }));
                timeoutThread.start();
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                lastFeedbackChannel.sendMessage("uhhhhhhhh there was a problem").queue();
            }

        }

        // the SendHandler, for JDA
        protected class SendHandler implements AudioSendHandler {

            // the AudioPlayer this SendHandler is for
            protected final AudioPlayer player;

            // the last frame we sent
            protected AudioFrame lastFrame;

            // constructor
            protected SendHandler(AudioPlayer p) {
                this.player = p;
            }

            @Override
            public boolean canProvide() {
                lastFrame = player.provide();
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

        }

        // loads a track to play given a String query and a channel to send the results to
        public void load(String query, final TextChannel feedbackChannel) {
            // set the feedback channel so we have somewhere to complain to
            this.lastFeedbackChannel = feedbackChannel;

            // load the query
            this.audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    // queue the track
                    scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    // queue all the tracks from the playlist
                    for (AudioTrack track : playlist.getTracks()) {
                        scheduler.queue(track);
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

        // sends information about a track into the feedbackChannel
        protected void sendInfo(AudioTrack track) {
            // send some info about the track
            AudioTrackInfo info = track.getInfo();
            long minutes = (info.length / 1000) / 60;
            long seconds = (info.length / 1000) % 60;
            lastFeedbackChannel.sendMessage(
                    info.title + " by " + info.author + " (" + minutes + ":" + seconds + ")"
            ).queue();
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

}
