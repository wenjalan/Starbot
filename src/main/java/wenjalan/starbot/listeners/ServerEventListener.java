package wenjalan.starbot.listeners;

import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.Users;

public class ServerEventListener extends ListenerAdapter {

    // the Starbot this listener belongs to
    protected Starbot starbot;

    // constructor
    public ServerEventListener(Starbot starbot) {
        this.starbot = starbot;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
        // if we're playing something in the guild
        if (e.getGuild().getAudioManager().isConnected()) {
            // check if we're the only one in the channel
            VoiceChannel channel = e.getChannelLeft();
            int membersCount = channel.getMembers().size();
            // check if that last member is Starbot
            if (membersCount == 1 && channel.getMembers().get(0).getUser().getIdLong() == Users.STARBOT) {
                // disconnect
                e.getGuild().getAudioManager().closeAudioConnection();
                // sout
                System.out.println("disconnected from " + channel.getName() + " in " + e.getGuild().getName() + ", no more users in channel");
            }
        }
    }

}
