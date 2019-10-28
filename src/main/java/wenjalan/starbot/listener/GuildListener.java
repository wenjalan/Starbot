package wenjalan.starbot.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.AudioEngine;

import java.util.List;

// listener for guild-related events
public class GuildListener extends ListenerAdapter {

    // when someone leaves a voice channel
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
        // if we were playing music
        if (e.getVoiceState().inVoiceChannel()) {
            // if we are the last one in this channel now, shut down the player and leave
            List<Member> connected = e.getChannelLeft().getMembers();
            if (connected.size() == 1 && connected.contains(e.getGuild().getSelfMember())) {
                // get out
                e.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

}
