package wenjalan.starbot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.AudioEngine;
import wenjalan.starbot.engine.command.StopCommand;

import java.util.List;

public class GuildEventListener extends ListenerAdapter {

    // onGuildVoiceLeave
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        // if we were connected to the voice channel
        Guild g = event.getGuild();
        AudioManager audioManager = g.getAudioManager();
        VoiceChannel channel = event.getChannelLeft();
        if (audioManager.isConnected()) {
            // check if we we're the only one in the channel now
            List<Member> connected = channel.getMembers();
            if (connected.size() <= 1 && connected.contains(event.getGuild().getSelfMember())) {
                // leave if we're the only member in the channel
                AudioEngine audio = AudioEngine.getInstance();
                audio.stopPlayback(g);
            }
        }
    }

}
