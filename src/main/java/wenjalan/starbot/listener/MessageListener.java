package wenjalan.starbot.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.CommandEngine;

// listens for all Messages, sends data to corresponding engines
public class MessageListener extends ListenerAdapter {

    // private message listening method
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        // if it's a command, run it
        if (CommandEngine.isCommand(event.getMessage())) {
            CommandEngine.parseDMCommand(event);
        }
    }

    // guild message received
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // if it's a command, run it
        if (CommandEngine.isCommand(event.getMessage())) {
            CommandEngine.parseGuildCommand(event);
        }
    }

}
