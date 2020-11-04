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
        if (!m.isConnected() || !member.getVoiceState().inVoiceChannel()) {
            textChannel.sendMessage("You aren't in a voice channel.").queue();
            return;
        }

        // if Starbot isn't connected yet, connect first
        if (!m.isConnected()) {
            m.setSendingHandler(new MusicHandler(msg));
            m.openAudioConnection(voiceChannel);
        }
        // if he is, play the track needed
        else {
            MusicHandler musicHandler = (MusicHandler) m.getSendingHandler();
            musicHandler.playTrack(msg);
        }

    }

    // disconnects the bot from the voice channel
    // msg: the Message which asked to play
    public void stopPlayback(Message msg) {
        // get guild items
        Guild g = msg.getGuild();
        AudioManager m = g.getAudioManager();

        // force disconnect
        m.closeAudioConnection();
    }

}
