package wenjalan.starbot.listener;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.CommandEngine;

// listens for all Messages, sends data to corresponding engines
public class MessageListener extends ListenerAdapter {

    // private message listening method
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        // echo to console
        System.out.println("[DM][" + event.getAuthor().getAsTag() + "]: " + event.getMessage().getContentDisplay());

        // echo if it's a command
        System.out.println("isCommand: " + CommandEngine.isCommand(event.getMessage()));
    }

    // guild message received
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // echo to console
        System.out.println("[" + event.getGuild().getName() + "][" + event.getAuthor().getAsTag() + "]: " + event.getMessage().getContentDisplay());
    }

}
