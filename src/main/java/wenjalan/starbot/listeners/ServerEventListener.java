package wenjalan.starbot.listeners;

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wenjalan.starbot.Starbot;

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

}
