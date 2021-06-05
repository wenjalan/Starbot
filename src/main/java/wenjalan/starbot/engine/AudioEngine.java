package wenjalan.starbot.engine;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MusicHandler;

// handles the audio functions
public class AudioEngine {

    // singleton
    private static AudioEngine instance = null;

    // private constructor
    private AudioEngine() {
        if (instance != null) {
            throw new IllegalStateException("New instances of AudioEngine cannot be instantiated");
        }
    }

    // instance accessor
    public static AudioEngine getInstance() {
        if (instance == null) {
            instance = new AudioEngine();
        }
        return instance;
    }

    // joins the bot to a voice channel and begins playing a track
    // msg: the Message which asked to play
    public void startPlayback(Message msg) {
        // get guild items
        Member member = msg.getMember();
        Guild g = msg.getGuild();
        AudioManager m = g.getAudioManager();
        TextChannel textChannel = msg.getTextChannel();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();

        // check that the user is in a voice channel
        if (voiceChannel == null) {
            textChannel.sendMessage("You aren't in a voice channel.").queue();
            return;
        }

        // if Starbot isn't connected yet, connect first
        // if the sendhandler isn't a musichandler, reassign send and receive handlers
        AudioSendHandler sendHandler = m.getSendingHandler();
        if (!m.isConnected() || !(sendHandler instanceof MusicHandler)) {
            MusicHandler handler = new MusicHandler();
            m.setSendingHandler(handler);
            m.setReceivingHandler(null);
            m.openAudioConnection(voiceChannel);

            // create the controller channel
            handler.createController(g, msg.getTextChannel());

            // if there was a query to play, play
            handler.playTrack(msg);
        }
        // if he is, play the track needed
        else {
            MusicHandler musicHandler = (MusicHandler) m.getSendingHandler();
            musicHandler.playTrack(msg);
        }

    }

    // disconnects the bot from the voice channel
    // msg: the Message which asked to play
    public void stopPlayback(Guild g) {
        // get guild items
        AudioManager m = g.getAudioManager();

        // delete the controller
        MusicHandler handler = (MusicHandler) m.getSendingHandler();
        handler.destroyController(g);

        // force disconnect
        m.closeAudioConnection();
    }
}
