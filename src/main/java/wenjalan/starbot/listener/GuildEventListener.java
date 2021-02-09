package wenjalan.starbot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.AudioEngine;
import wenjalan.starbot.engine.command.StopCommand;

import javax.annotation.Nonnull;
import java.util.List;

public class GuildEventListener extends ListenerAdapter {

//    // onGuildVoiceLeave
//    @Override
//    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
//        // if the member was us (meaning we were forcibly disconnected)
//        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
//            // disconnect any active handlers?
//            AudioEngine audio = AudioEngine.getInstance();
//            audio.stopPlayback(event.getGuild());
//        }
//
//        // if we were connected to the voice channel
//        Guild g = event.getGuild();
//        AudioManager audioManager = g.getAudioManager();
//        VoiceChannel channel = event.getChannelLeft();
//        if (audioManager.isConnected()) {
//            // check if we we're the only one in the channel now
//            List<Member> connected = channel.getMembers();
//            if (connected.size() <= 1 && connected.contains(event.getGuild().getSelfMember())) {
//                // leave if we're the only member in the channel
//                AudioEngine audio = AudioEngine.getInstance();
//                audio.stopPlayback(g);
//            }
//        }
//    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        // if someone left a voice channel
        VoiceChannel channelLeft = event.getChannelLeft();
        VoiceChannel channelJoined = event.getChannelJoined();
        if (channelLeft != null && channelJoined == null) {
            Guild g = channelLeft.getGuild();

            // if the member was Starbot, close audio resources
            if (event.getEntity().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                AudioEngine audio = AudioEngine.getInstance();
                audio.stopPlayback(g);
            }

            // otherwise, check if we are the last one in the voice channel
            List<Member> connected = channelLeft.getMembers();
            if (connected.size() == 1 && connected.contains(g.getSelfMember())) {
                // disconnect
                AudioEngine audio = AudioEngine.getInstance();
                audio.stopPlayback(g);
            }
        }
    }

}
